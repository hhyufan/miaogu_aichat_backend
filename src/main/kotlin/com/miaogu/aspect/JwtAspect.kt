package com.miaogu.aspect

import com.miaogu.response.R
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

@Aspect
@Component
@RequestScope // 确保每个请求都有自己的实例
class JwtAspect @Autowired constructor(
    private val jwtService: JwtService,
    private val redisTemplate: RedisTemplate<String, String>
) {
    @Value("\${jwt.refresh-expire}")
    private val refreshExpiration: Long = 86400000 // 1天

    @Around("@within(com.miaogu.annotation.RequireJwt) || @annotation(com.miaogu.annotation.RequireJwt)")
    fun checkJwt(joinPoint: ProceedingJoinPoint): Any? {
        // 获取当前请求的 HttpServletRequest 和 HttpServletResponse
        val requestAttributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
        val request = requestAttributes.request

        // 获取 Authorization 头
        val authorizationHeader = request.getHeader("Authorization")

        // 验证 JWT
        if (authorizationHeader.isNullOrEmpty() || !authorizationHeader.startsWith("Bearer ")) {
            throw RuntimeException("缺少或无效的 JWT")
        }

        val token = authorizationHeader.substring(7) // 去掉 "Bearer " 前缀
        val username = jwtService.extractUsername(token)

        if (!jwtService.validateToken(token, username)) {
            throw RuntimeException("无效的 JWT")
        }

        // 存储用户名到 Redis
        redisTemplate.opsForValue().set("username", username, refreshExpiration, TimeUnit.MILLISECONDS)

        // 执行目标方法
        val result = joinPoint.proceed()

        val extraInfo = mapOf(
            "username" to username,
            "isTokenUpdated" to redisTemplate.opsForValue().get("jwt:isTokenUpdated").toString()
        )

        if (result is R<*>) {
            return result.withExtra(extraInfo)
        }
        return result // 如果没有响应对象，则返回原始结果
    }
}
