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
    @Value("\${cors.additional-url:}")
    private lateinit var additionalUrl: String
    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()

    private fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            // 生成动态端口列表（开发环境）
            val devPorts = (5173..5200).map { "http://localhost:$it" }

            // 配置动态域名模式（生产环境）
            allowedOriginPatterns = mutableListOf<String>().apply {
                // 固定域名
                addAll(listOf(
                    "https://www.miaogu.top",
                    "https://app.miaogu.top",
                    "https://api.miaogu.top",
                    "https://yuki.cmyam.net",
                    additionalUrl  // 保留原有 ngrok 配置
                ))

                // 动态子域名模式（支持随机数）
                add("https://miaogu-*-hhyufans-projects.vercel.app")
                add("https://*.vercel.app")  // 兜底匹配所有 Vercel 子域名

                // 开发端口
                addAll(devPorts)
            }

            // 其他配置保持不变
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

