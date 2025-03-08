package com.miaogu.aspect

import com.miaogu.context.UserContext
import com.miaogu.response.ApiResponse
import com.miaogu.service.JwtService
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.concurrent.TimeUnit

/**
 * JWT 相关的切面处理类
 */
@Aspect
@Component
@RequestScope
class JwtAspect @Autowired constructor(
    private val userContext: UserContext,
    private val jwtService: JwtService,
    private val redisTemplate: RedisTemplate<String, String>
) {
    @Value("\${jwt.refresh-expire}")
    private val refreshExpiration: Long = 86400000 // 1天

    /**
     * 检查 JWT 的有效性
     */
    @Around("@within(com.miaogu.annotation.RequireJwt) || @annotation(com.miaogu.annotation.RequireJwt)")
    fun checkJwt(joinPoint: ProceedingJoinPoint): Any? {

        val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            ?: throw IllegalStateException("无法获取请求上下文")

        val request = requestAttributes.request

        val token = request.getHeader("Authorization")?.substringAfter("Bearer ")!!

        val username = jwtService.extractUsername(token)
        userContext.username = username
        val currentToken = redisTemplate.opsForValue().get("jwt:current:$username")
        println("currentToken: $currentToken")
        redisTemplate.opsForValue().set("username", username, refreshExpiration, TimeUnit.MILLISECONDS)

        if (shouldRenewToken(token)) {
            renewToken(token)
        }

        val authHeader = request.getHeader("Authorization")

        try {
            authHeader?.let { header ->
                if (header.startsWith("Bearer ")) {
                    val token = header.substring(7)
                    handleTokenRenewal(token)
                }
            }
        } catch (ex: Exception) {
            // 记录日志但不中断流程
            LoggerFactory.getLogger(javaClass).error("Token renewal error", ex)
        }

        // 安全处理响应
        return when (val result = joinPoint.proceed()) {
            is ApiResponse<*> -> {
                result.apply {
                    extra = buildMap {
                        put("tokenStatus", getTokenStatus(authHeader))
                        put("username", username)
                        put("isTokenUpdated", redisTemplate.opsForValue().get("jwt:isTokenUpdated") ?: "false")
                    }
                }
            }
            else -> result
        }
    }
    private fun handleTokenRenewal(token: String) {
        if (shouldRenewToken(token)) {
            val newToken = jwtService.refreshToken(token)
            redisTemplate.opsForValue().set(
                "jwt:current:${jwtService.extractUsername(token)}",
                newToken,
                jwtService.getTokenExpiration(newToken).time - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS
            )
        }
    }
    private fun shouldRenewToken(token: String): Boolean {
        return try {
            val expiration = jwtService.getTokenExpiration(token)
            val remaining = expiration.time - System.currentTimeMillis()
            remaining in 1..300_000 // 有效期剩余 5 分钟内时续期
        } catch (_: Exception) {
            false
        }
    }
    private fun getTokenStatus(header: String?): String {
        return when {
            header == null -> "missing"
            !header.startsWith("Bearer ") -> "invalid_format"
            else -> "valid"
        }
    }
    private fun renewToken(username: String) {
        jwtService.generateToken(username)
        println("Token renewed for $username")
        jwtService.generateRefreshToken(username)
    }
}
