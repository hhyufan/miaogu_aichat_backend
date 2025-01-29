package com.miaogu.controller

import com.miaogu.dto.RefreshTokenDTO
import com.miaogu.service.UserService
import com.miaogu.dto.UserDTO
import com.miaogu.entity.User
import com.miaogu.response.ApiResponse
import com.miaogu.service.JwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService, private val jwtService: JwtService ) {
    @Value("\${jwt.expire}")
    private val expirationTime: Long = 3600000 // 1小时

    @PostMapping("/login")
    @ResponseBody
    fun login(@RequestBody user: User): ApiResponse<Map<String, Any?>> {
        return handleUserAuthentication(user, userService.login(user))
    }

    @PostMapping("/register")
    fun register(@RequestBody user: User): ApiResponse<Map<String, Any?>> {
        return handleUserAuthentication(user, userService.register(user))
    }

    private fun handleUserAuthentication(user: User, authPair: Pair<HttpStatus, String?>): ApiResponse<Map<String, Any?>> {
        if (authPair.first != HttpStatus.OK) {
            return ApiResponse(authPair.first, authPair.second)
        }

        val token = user.username?.let { jwtService.generateToken(it) }
        val refreshToken = user.username?.let { jwtService.generateRefreshToken(it) }

        val data = mapOf(
            "refreshToken" to refreshToken,
            "token" to token,
            "expiresIn" to expirationTime.toString(),
            "user" to UserDTO(
                username = user.username,
                email = user.email,
            )
        )
        return ApiResponse(authPair.first, authPair.second, data)
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody refreshTokenDTO: RefreshTokenDTO): ApiResponse<Any?> {
        if (jwtService.validateRefreshToken(refreshTokenDTO.username, refreshTokenDTO.refreshToken)) {
            val newToken = jwtService.generateToken(refreshTokenDTO.username)
            val refreshToken = jwtService.generateRefreshToken(refreshTokenDTO.username)
            return ApiResponse(HttpStatus.OK, data = mapOf(
                "token" to newToken,
                "refreshToken" to refreshToken
            ))
        } else {
            return ApiResponse(HttpStatus.UNAUTHORIZED, "无效的刷新令牌")
        }
    }

    @PostMapping("/token")
    fun getToken(): ApiResponse<Map<String, String?>> {
        val token = jwtService.getTokenByUsername()
        return ApiResponse(HttpStatus.OK, data = mapOf(
            "token" to token
        ))
    }
}
