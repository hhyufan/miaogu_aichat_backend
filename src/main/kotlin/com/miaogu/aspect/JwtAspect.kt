package com.miaogu.aspect

import com.miaogu.service.JwtService
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@Aspect
@Component
@RequestScope // 确保每个请求都有自己的实例
class JwtAspect @Autowired constructor(
    private val request: HttpServletRequest, // 注入 HttpServletRequest
    private val jwtService: JwtService
) {

    @Before("@within(com.miaogu.annotation.RequireJwt) || @annotation(com.miaogu.annotation.RequireJwt)")
    fun checkJwt() {
        // 使用注入的请求对象
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
    }
}
