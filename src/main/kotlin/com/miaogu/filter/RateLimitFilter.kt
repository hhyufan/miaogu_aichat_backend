package com.miaogu.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.TimeUnit
import org.springframework.beans.factory.annotation.Value
@Component
class RateLimitFilter(
    private val redisTemplate: StringRedisTemplate
) : OncePerRequestFilter() {
    @Value("\${rate-limit.max-attempts}")
    private val maxAttempts = 5
    private val blockDuration = 1L

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (request.servletPath == "/user/login") {
            val ip = request.remoteAddr
            val key = "login:attempts:$ip"

            val attempts = redisTemplate.opsForValue().increment(key) ?: 0
            if (attempts == 1L) {
                redisTemplate.expire(key, blockDuration, TimeUnit.HOURS)
            }

            if (attempts > maxAttempts) {
                response.sendError(
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    "尝试次数过多，请1小时后再试"
                )
                return
            }
        }
        filterChain.doFilter(request, response)
    }
}
