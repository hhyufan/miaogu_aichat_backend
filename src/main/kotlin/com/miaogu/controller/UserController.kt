package com.miaogu.controller

import com.miaogu.dto.RefreshTokenDTO
import com.miaogu.service.UserService
import com.miaogu.dto.UserDTO
import com.miaogu.entity.User
import com.miaogu.response.ApiResponse
import com.miaogu.service.JwtService
import com.miaogu.util.RSAUtils
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import kotlin.text.isNullOrEmpty

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService,
                     private val jwtService: JwtService,
                     private val redisTemplate: RedisTemplate<String, String>) {
    private val username: String?
        get() = redisTemplate.opsForValue().get("username")
    @Value("\${rsa.private-key}")
    private val privateKey: String = ""
    @Value("\${jwt.expire}")
    private val expirationTime: Long = 3600000 // 1小时
    private val logger: org.apache.logging.log4j.Logger? = LogManager.getLogger()
    @PostMapping("/login")
    @ResponseBody
    fun login(@RequestBody request: Map<String, String>): ApiResponse<Map<String, Any?>> {
        val username = request["username"]
        val encryptedPassword = request["password"]

        if (encryptedPassword.isNullOrEmpty()) {
            return ApiResponse(HttpStatus.BAD_REQUEST, "密码不能为空")
        }

        try {
            val decryptedPassword = RSAUtils.decrypt(encryptedPassword, privateKey)
            val user = User(username, password = decryptedPassword)
            return handleUserAuthentication(user, userService.login(user))
        } catch (e: Exception) {
            logger?.error("登录解密失败: ${e.message}")
            return ApiResponse(HttpStatus.BAD_REQUEST, "密码解密失败")
        }
    }

    @PostMapping("/register")
    fun register(@RequestBody request: Map<String, String>): ApiResponse<Map<String, Any?>> {
        val username = request["username"]
        val encryptedPassword = request["password"]
        val email = request["email"]
        if (encryptedPassword.isNullOrEmpty()) {
            return ApiResponse(HttpStatus.BAD_REQUEST, "密码不能为空")
        }

        try {
            val decryptedPassword = RSAUtils.decrypt(encryptedPassword, privateKey)
            println("password: $decryptedPassword")
            val user = User(username, password =  decryptedPassword, email = email)
            return handleUserAuthentication(user, userService.register(user))
        } catch (e: Exception) {
            logger?.error("注册解密失败: ${e.message}")
            return ApiResponse(HttpStatus.BAD_REQUEST, "密码解密失败")
        }
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
