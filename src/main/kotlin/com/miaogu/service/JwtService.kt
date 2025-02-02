package com.miaogu.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.miaogu.exception.JwtValidationException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.security.SignatureException
import java.util.Base64
import java.util.Date
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Service
class JwtService(private val redisTemplate: StringRedisTemplate) {
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    @Value("\${jwt.expire}")
    private val expirationTime: Long = 3600000 // 1小时
    @Value("\${jwt.refresh-expire}")
    private val refreshExpiration: Long = 86400000 // 1天
    @Value("\${jwt.fixed-rate}")
    private val fixedRate: Long = 8 // 每8小时刷新一次

    private val gson = Gson() // 创建 Gson 实例
    private val secretKeysKey = "jwt:secretKeys" // Redis 中存储密钥栈的键
    private val secretKeys = mutableListOf<String>() // 使用 List 存储密钥
    private val isTokenUpdatedKey = "jwt:isTokenUpdated"
    private var refreshCount = 0 // 用于跟踪刷新次数

    init {
        loadSecretKeysFromRedis() // 从 Redis 加载密钥栈
        scheduleKeyRefresh() // 调度密钥刷新任务
    }

    private fun generateSecretKey(): String {
        val random = SecureRandom()
        val keyBytes = ByteArray(32) // 256位 = 32字节
        random.nextBytes(keyBytes)
        val secretKey = Base64.getEncoder().encodeToString(keyBytes)
        return secretKey // 转换为Base64字符串
    }

    private fun loadSecretKeysFromRedis() {
        val secretKeysJson = redisTemplate.opsForValue().get(secretKeysKey)
        if (secretKeysJson != null) {
            val type = object : TypeToken<List<String>>() {}.type // 创建 TypeToken
            val loadedKeys: List<String> = gson.fromJson(secretKeysJson, type)
            secretKeys.addAll(loadedKeys) // 将加载的密钥添加到列表中
        }
    }

    private fun saveSecretKeysToRedis() {
        val secretKeysJson = gson.toJson(secretKeys) // 将密钥栈序列化为 JSON
        redisTemplate.opsForValue().set(secretKeysKey, secretKeysJson) // 存储到 Redis
    }

    private fun addSecretKey(secretKey: String) {
        if (secretKeys.size >= 2) {
            secretKeys.removeAt(0) // 移除最旧的密钥
        }
        secretKeys.add(secretKey) // 添加新密钥
        saveSecretKeysToRedis() // 更新 Redis 中的密钥栈
    }

    fun generateToken(username: String): String {
        val claims: Claims = Jwts.claims().setSubject(username)
        val now = Date()
        val secretKey = secretKeys.lastOrNull() // 使用栈顶密钥
        val token = Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + expirationTime))
            .signWith(SignatureAlgorithm.HS256, secretKey) // 使用栈顶密钥
            .compact()

        // 将生成的 Token 存储到 Redis 中
        redisTemplate.opsForValue().set("jwt:token:$username", token, expirationTime, TimeUnit.MILLISECONDS)
        return token
    }

    fun generateRefreshToken(username: String): String {
        val now = Date()
        val secretKey = secretKeys.lastOrNull() // 使用栈顶密钥

        val refreshToken = Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + refreshExpiration))
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact()
        redisTemplate.opsForValue().set(username, refreshToken, refreshExpiration, TimeUnit.MILLISECONDS)
        return refreshToken
    }

    fun validateRefreshToken(username: String, refreshToken: String): Boolean {
        val storedToken = redisTemplate.opsForValue().get(username)
        return storedToken == refreshToken
    }

    fun validateToken(token: String, username: String): Boolean {
        return try {
            if (isTokenBlacklisted(token)) return false // 检查黑名单
            val claims = extractAllClaims(token)
            claims.subject == username && !isTokenExpired(claims) && checkTokenInRedis(username, token)
        } catch (e: Exception) {
            println("JWT Validation Error: ${e.message}") // 捕获并打印异常
            false
        }
    }

    private fun checkTokenInRedis(username: String, token: String): Boolean {
        val storedToken = redisTemplate.opsForValue().get("jwt:token:$username")
        return storedToken == token
    }
    private fun isTokenExpired(claims: Claims): Boolean {
        return claims.expiration.before(Date())
    }

    private fun extractAllClaims(token: String): Claims {
        for (key in secretKeys) {
            try {
                return Jwts.parser().setSigningKey(key).parseClaimsJws(token).body
            } catch (_: Exception) {
                // Continue to the next key
            }
        }
        throw Exception("JWT signature does not match") // All keys failed
    }

    fun extractUsername(token: String): String {
        return extractAllClaims(token).subject
    }

    private fun scheduleKeyRefresh() {
        scheduler.scheduleAtFixedRate({ refreshSecretKey() }, 0, fixedRate, TimeUnit.HOURS)
    }

    private fun refreshSecretKey() {
        val newKey = generateSecretKey()
        addSecretKey(newKey) // 添加新密钥并更新 Redis
        refreshCount++ // 增加刷新计数
        // 根据刷新次数设置 isTokenUpdated
        if (refreshCount == 1) {
            redisTemplate.opsForValue().set(isTokenUpdatedKey, false.toString())
        } else {
            redisTemplate.opsForValue().set(isTokenUpdatedKey, true.toString())
        }

        refreshAllTokens() // 刷新所有用户的 JWT Token
    }

    private fun refreshAllTokens() {
        // 获取所有用户的 Token
        val keys = redisTemplate.keys("jwt:token:*") // 获取所有 Token 的键
        keys.forEach { key ->
            val username = key.split(":").last() // 提取用户名
            val token = generateToken(username) // 生成新的 Token
            redisTemplate.opsForValue().set(key, token, expirationTime, TimeUnit.MILLISECONDS) // 更新 Redis 中的 Token
        }
    }

    fun getTokenByUsername(): String? {
        return redisTemplate.opsForValue().get("jwt:token:${redisTemplate.opsForValue().get("username")}")
    }

    fun getTokenExpiration(token: String): Date {
        return extractAllClaims(token).expiration
    }

    fun refreshToken(oldToken: String): String {
        val username = extractUsername(oldToken)
        invalidateToken(oldToken) // 使旧令牌失效
        return generateToken(username)
    }

    private fun invalidateToken(token: String) {
        val expiration = getTokenExpiration(token).time - System.currentTimeMillis()
        if (expiration > 0) {
            redisTemplate.opsForValue().set(
                "jwt:invalidated:$token",
                "invalid",
                expiration,
                TimeUnit.MILLISECONDS
            )
        }
    }

    fun isTokenBlacklisted(token: String): Boolean {
        return redisTemplate.hasKey("jwt:invalidated:$token")
    }
}
