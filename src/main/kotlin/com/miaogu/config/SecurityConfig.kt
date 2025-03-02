package com.miaogu.config

import com.miaogu.filter.JwtAuthenticationFilter
import com.miaogu.handler.JwtAccessDeniedHandler
import com.miaogu.point.JwtAuthenticationEntryPoint
import com.miaogu.service.JwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // 启用方法级安全控制
class SecurityConfig(
    private val jwtService: JwtService
) {
    @Value("\${ngrok.ngrok-url}")
    private var ngrokUrl: String = ""
    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()

    private fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            // 生成从 5173 到 5200 的端口列表
            val allowedPorts = (5173..5200).map { "http://localhost:$it" }

            // 将其他允许的源添加到列表中
            allowedOrigins = listOf("https://www.miaogu.top", "https://app.miaogu.top", "https://api.miaogu.top", ngrokUrl) + allowedPorts
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
            maxAge = 3600L
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() } // 仅针对无状态API禁用CSRF
            .cors { it.configurationSource(corsConfigurationSource()) } // 配置CORS
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(
                    "/user/login",
                    "/user/register",
                    "/user/refresh",
                    "/github/**",
                    "/edge-config/**",
                    "/error",
                    "/exceptions/*",
                ).permitAll()
                    .anyRequest().authenticated()
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilterBefore(
                JwtAuthenticationFilter(jwtService),
                UsernamePasswordAuthenticationFilter::class.java
            )
            .exceptionHandling {
                it.authenticationEntryPoint(JwtAuthenticationEntryPoint())
                it.accessDeniedHandler(JwtAccessDeniedHandler())
            }
            .headers {
                it.httpStrictTransportSecurity { hsts ->
                    hsts.includeSubDomains(true)
                        .maxAgeInSeconds(31536000) // 1年HSTS
                }
            }
        return http.build()
    }


}

