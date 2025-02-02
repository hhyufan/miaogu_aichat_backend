package com.miaogu.controller

import com.miaogu.dto.RefreshTokenDTO
import com.miaogu.service.UserService
import com.miaogu.dto.UserDTO
import com.miaogu.entity.User
import com.miaogu.response.ApiResponse
import com.miaogu.service.JwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService,
                     private val jwtService: JwtService,
                     private val redisTemplate: RedisTemplate<String, String>) {
    private val username: String?
        get() = redisTemplate.opsForValue().get("username")

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

    @PostMapping("/logout")
    fun logout(): ApiResponse<Map<String, Any?>> {
        username?.let { jwtService.logout(it) }
        return ApiResponse(HttpStatus.OK, "Logout successful")
    }

    private fun handleUserAuthentication(user: User, authPair: Pair<HttpStatus, String?>): ApiResponse<Map<String, Any?>> {
        if (authPair.first != HttpStatus.OK) {
            return ApiResponse(authPair.first, authPair.second)
        }

        // 明确处理 completedUser 为 null 的情况
        val completedUser = userService.getCompleteUser(user)
            ?: return ApiResponse(HttpStatus.NOT_FOUND, "User not found")

        // 确保 user.username 非空（根据业务逻辑）
        val username = completedUser.username
            ?: return ApiResponse(HttpStatus.BAD_REQUEST, "Username is missing")

        // 生成 token 和 refreshToken
        val token = jwtService.generateToken(username)
        val refreshToken = jwtService.generateRefreshToken(username)

        val data = mapOf(
            "refreshToken" to refreshToken,
            "token" to token,
            "expiresIn" to expirationTime.toString(),
            "user" to UserDTO(
                username = completedUser.username,
                email = completedUser.email
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
