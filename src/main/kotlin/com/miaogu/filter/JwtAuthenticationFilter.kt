package com.miaogu.filter

import com.miaogu.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        val path = request.requestURI.substring(request.contextPath.length)
        
        if (path.contains("/exceptions")) {
            filterChain.doFilter(request, response)
            return
        }
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7)
        try {
            val username = jwtService.extractUsername(token)
            if (SecurityContextHolder.getContext().authentication == null) {
                if (jwtService.validateToken(token, username)) {
                    val authToken = UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        listOf(SimpleGrantedAuthority("ROLE_USER"))
                    )
                    SecurityContextHolder.getContext().authentication = authToken
                }
            }
        } catch (_: Exception) {
            SecurityContextHolder.clearContext()
        }
        filterChain.doFilter(request, response)
    }
}
