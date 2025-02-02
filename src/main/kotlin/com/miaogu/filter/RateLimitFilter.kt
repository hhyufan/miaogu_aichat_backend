package com.miaogu.filter
import com.miaogu.entity.User
import com.miaogu.extension.fromJsonToObject
import com.miaogu.wrapper.CachedBodyHttpServletRequestWrapper
import java.io.BufferedReader
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
        val httpRequest = request
        // 使用包装后的请求
        val wrappedRequest = CachedBodyHttpServletRequestWrapper(httpRequest)
        if (request.servletPath == "/user/login") {
            val ip = wrappedRequest.remoteAddr
            val requestBody: User = wrappedRequest.reader.use(BufferedReader::readText).fromJsonToObject()
            println("Request Body: $requestBody")
            val key = "login:attempts:$ip"

            val attempts = redisTemplate.opsForValue().increment(key) ?: 0
            if (attempts == 1L) {
                redisTemplate.expire(key, blockDuration, TimeUnit.MINUTES)
            }

            if (attempts > maxAttempts) {
                response.sendError(
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    "尝试次数过多，请1分钟后再试"
                )
                return
            }
        }
        filterChain.doFilter(wrappedRequest, response)
    }
}
