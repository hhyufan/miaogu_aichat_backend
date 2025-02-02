package com.miaogu.filter

import com.miaogu.exception.JwtValidationException
import com.miaogu.global.WhiteListPaths.WHITELIST_PATHS
import com.miaogu.service.JwtService
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.context.support.WebApplicationContextUtils
import java.io.IOException

class ExceptionFilter : Filter  {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        try {
            val request = servletRequest as HttpServletRequest
            val applicationContext = WebApplicationContextUtils.getWebApplicationContext(request.servletContext)
            if (WHITELIST_PATHS.stream().anyMatch { path -> request.requestURI.startsWith(path) }) {
                filterChain.doFilter(servletRequest, servletResponse)
                return
            }
            // 从 ApplicationContext 获取 JwtService 实例
            val jwtService = applicationContext?.getBean(JwtService::class.java)
                ?: throw IllegalStateException("JwtService bean not found")

            val authorizationHeader = request.getHeader("Authorization")?.takeIf { it.isNotBlank() }
                ?: throw JwtValidationException("缺少授权凭证")

            val (type, token) = authorizationHeader.split(" ").takeIf { it.size == 2 }
                ?: throw JwtValidationException("无效的令牌格式")

            if (type != "Bearer") {
                throw JwtValidationException("不支持的认证类型")
            }

            val username = try {
                jwtService.extractUsername(token)
            } catch (_: Exception) {
                throw JwtValidationException("令牌无效")
            }

            if (!jwtService.validateToken(token, username)) {
                throw JwtValidationException("令牌已失效")
            }

            filterChain.doFilter(servletRequest, servletResponse)
        } catch (e: JwtValidationException) {
            servletRequest.setAttribute("filter.exception.message", e.message)
            servletRequest.getRequestDispatcher("/exceptions/JwtValidationException").forward(servletRequest, servletResponse)
        } catch (e: Exception) {
            servletRequest.setAttribute("filter.exception", e)
            servletRequest.getRequestDispatcher("/exceptions/Exception").forward(servletRequest, servletResponse)
        }
    }
}
