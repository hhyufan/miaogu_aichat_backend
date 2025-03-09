package com.miaogu.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.Base64
import java.util.Date
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Service
class JwtService(
    @Qualifier("stringRedisTemplate")
    private val redisTemplate: StringRedisTemplate
) {
    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    @Value("\${jwt.expire}")
    private val expirationTime: Long = 3600000 // 1小时

    @Value("\${jwt.refresh-expire}")
    private val refreshExpiration: Long = 86400000 // 1天

    @Value("\${jwt.fixed-rate}")
    private val fixedRate: Long = 8 // 每8小时刷新一次

    private val gson = Gson()
    private val secretKeysKey = "jwt:secretKeys"
    private val secretKeys = mutableListOf<String>()
    private val isTokenUpdatedKey = "jwt:isTokenUpdated"
    private var refreshCount = 0

    init {
        loadSecretKeysFromRedis()
        scheduleKeyRefresh()
    }

    private fun generateSecretKey(): String {
        val random = SecureRandom()
        val keyBytes = ByteArray(32) // 256位 = 32字节
        random.nextBytes(keyBytes)
        return Base64.getEncoder().encodeToString(keyBytes)
    }

    private fun loadSecretKeysFromRedis() {
        redisTemplate.opsForValue().get(secretKeysKey)?.let { secretKeysJson ->
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(secretKeysJson, type)?.let { loadedKeys ->
                secretKeys.addAll(loadedKeys)
            }
        }
    }

    private fun saveSecretKeysToRedis() {
        val secretKeysJson = gson.toJson(secretKeys)
        redisTemplate.opsForValue().set(secretKeysKey, secretKeysJson)
    }

    private fun addSecretKey(secretKey: String) {
        if (secretKeys.size >= 2) {
            secretKeys.removeAt(0) // 移除最旧的密钥
        }
        secretKeys.add(secretKey)
        saveSecretKeysToRedis()
    }

    fun generateToken(username: String): String {
        val claims = Jwts.claims().setSubject(username)
        val now = Date()
        val secretKey = secretKeys.lastOrNull()

        val token = Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + expirationTime))
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact()

        // 将生成的 Token 存储到 Redis 中
        redisTemplate.opsForValue().set("jwt:token:$username", token, expirationTime, TimeUnit.MILLISECONDS)
        return token
    }

    fun generateRefreshToken(username: String): String {
        val now = Date()
        val secretKey = secretKeys.lastOrNull()

        val refreshToken = Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + refreshExpiration))
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact()

        redisTemplate.opsForValue().set(username, refreshToken, refreshExpiration, TimeUnit.MILLISECONDS)
        return refreshToken
    }

    fun validateRefreshToken(username: String, refreshToken: String): Boolean =
        redisTemplate.opsForValue().get(username) == refreshToken

    fun validateToken(token: String, username: String): Boolean {
        return try {
            if (isTokenBlacklisted(token)) return false

            val claims = extractAllClaims(token)
            claims.subject == username && !isTokenExpired(claims) && checkTokenInRedis(username, token)
        } catch (_: Exception) {
            false
        }
    }

    private fun checkTokenInRedis(username: String, token: String): Boolean =
        redisTemplate.opsForValue().get("jwt:token:$username") == token

    private fun isTokenExpired(claims: Claims): Boolean =
        claims.expiration.before(Date())

    private fun extractAllClaims(token: String): Claims {
        for (key in secretKeys) {
            try {
                return Jwts.parser().setSigningKey(key).parseClaimsJws(token).body
            } catch (_: Exception) {
                // 继续尝试下一个密钥
            }
        }
        throw Exception("JWT signature does not match")
    }

    fun extractUsername(token: String): String =
        extractAllClaims(token).subject

    private fun scheduleKeyRefresh() {
        scheduler.scheduleAtFixedRate({ refreshSecretKey() }, 0, fixedRate, TimeUnit.HOURS)
    }

    private fun refreshSecretKey() {
        val newKey = generateSecretKey()
        addSecretKey(newKey)
        refreshCount++

        // 根据刷新次数设置 isTokenUpdated
        val isTokenUpdated = refreshCount > 1
        redisTemplate.opsForValue().set(isTokenUpdatedKey, isTokenUpdated.toString())

        refreshAllTokens()
    }

    private fun refreshAllTokens() {
        // 获取所有用户的 Token
        redisTemplate.keys("jwt:token:*").forEach { key ->
            val username = key.split(":").last()
            val token = generateToken(username)
            redisTemplate.opsForValue().set(key, token, expirationTime, TimeUnit.MILLISECONDS)
        }
    }

    fun getTokenByUsername(): String? =
        redisTemplate.opsForValue().get("username")?.let { username ->
            redisTemplate.opsForValue().get("jwt:token:$username")
        }

    fun getTokenExpiration(token: String): Date =
        extractAllClaims(token).expiration

    fun refreshToken(oldToken: String): String {
        val username = extractUsername(oldToken)
        invalidateToken(oldToken)
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

    fun logout(username: String) {
        redisTemplate.opsForValue().get("jwt:token:$username")?.let { token ->
            invalidateToken(token)
        }
    }

    fun isTokenBlacklisted(token: String): Boolean =
        redisTemplate.hasKey("jwt:invalidated:$token")
}
