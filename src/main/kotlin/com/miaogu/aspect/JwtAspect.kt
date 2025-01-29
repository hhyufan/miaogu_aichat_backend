package com.miaogu.aspect

import com.miaogu.response.ApiResponse
import com.miaogu.service.JwtService
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
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
        val requestAttributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
        val request = requestAttributes.request

        val authorizationHeader = request.getHeader("Authorization")
            ?: throw RuntimeException("缺少或无效的 JWT")

        if (!authorizationHeader.startsWith("Bearer ")) {
            throw RuntimeException("缺少或无效的 JWT")
        }

        val token = authorizationHeader.substring(7)
        val username = jwtService.extractUsername(token)

        if (!jwtService.validateToken(token, username)) {
            throw RuntimeException("无效的 JWT")
        }

        redisTemplate.opsForValue().set("username", username, refreshExpiration, TimeUnit.MILLISECONDS)

        val result = joinPoint.proceed() as ApiResponse<*>
        result.extra = mapOf(
            "username" to username,
            "isTokenUpdated" to redisTemplate.opsForValue().get("jwt:isTokenUpdated").toString()
        )
        return result
    }
}
