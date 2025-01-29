package com.miaogu.aspect

import com.miaogu.exception.JwtValidationException
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

        val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            ?: throw IllegalStateException("无法获取请求上下文")

        val request = requestAttributes.request

        val authorizationHeader = request.getHeader("Authorization")?.takeIf { it.isNotBlank() }
            ?: throw JwtValidationException("缺少授权凭证")

        val (type, token) = authorizationHeader.split(" ").takeIf { it.size == 2 }
            ?: throw JwtValidationException("无效的令牌格式")

        if (type != "Bearer") {
            throw JwtValidationException("不支持的认证类型")
        }

        val username = try {
            jwtService.extractUsername(token)
        } catch (_: JwtValidationException) {
            throw JwtValidationException("令牌无效")
        }

        if (!jwtService.validateToken(token, username)) {
            throw JwtValidationException("令牌已失效")
        }

        redisTemplate.opsForValue().set("username", username, refreshExpiration, TimeUnit.MILLISECONDS)

        // 安全处理响应
        return when (val result = joinPoint.proceed()) {
            is ApiResponse<*> -> {
                result.apply {
                    extra = buildMap {
                        put("username", username)
                        put("isTokenUpdated", redisTemplate.opsForValue().get("jwt:isTokenUpdated") ?: "false")
                    }
                }
            }
            else -> result
        }
    }
}
