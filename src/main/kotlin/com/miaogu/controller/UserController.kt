package com.miaogu.controller

import com.miaogu.context.UserContext
import com.miaogu.dto.RefreshTokenDTO
import com.miaogu.service.UserService
import com.miaogu.dto.UserDTO
import com.miaogu.entity.User
import com.miaogu.response.ApiResponse
import com.miaogu.service.JwtService
import com.miaogu.util.RSAUtils
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserController(
    private val userService: UserService,
    private val jwtService: JwtService,
    private val userContext: UserContext
) {
    private val username get() = userContext.username

    @Value("\${rsa.private-key}")
    private val privateKey: String = ""

    @Value("\${jwt.expire}")
    private val expirationTime: Long = 3600000 // 1小时

    private val logger = LogManager.getLogger()

    @PostMapping("/login")
    @ResponseBody
    fun login(@RequestBody request: Map<String, String>): ApiResponse<Map<String, Any?>> =
        request["password"]?.let { encryptedPassword ->
            try {
                val decryptedPassword = RSAUtils.decrypt(encryptedPassword, privateKey)
                val user = User(request["username"], password = decryptedPassword)
                handleUserAuthentication(user, userService.login(user))
            } catch (e: Exception) {
                logger.error("登录解密失败: ${e.message}")
                ApiResponse(HttpStatus.BAD_REQUEST, "密码解密失败")
            }
        } ?: ApiResponse(HttpStatus.BAD_REQUEST, "密码不能为空")

    @PostMapping("/register")
    fun register(@RequestBody request: Map<String, String>): ApiResponse<Map<String, Any?>> =
        request["password"]?.let { encryptedPassword ->
            try {
                val decryptedPassword = RSAUtils.decrypt(encryptedPassword, privateKey)
                val user = User(
                    username = request["username"],
                    password = decryptedPassword,
                    email = request["email"]
                )
                handleUserAuthentication(user, userService.register(user))
            } catch (e: Exception) {
                logger.error("注册解密失败: ${e.message}")
                ApiResponse(HttpStatus.BAD_REQUEST, "密码解密失败")
            }
        } ?: ApiResponse(HttpStatus.BAD_REQUEST, "密码不能为空")

    @PostMapping("/logout")
    fun logout(): ApiResponse<Map<String, Any?>> {
        username?.let { jwtService.logout(it) }
        return ApiResponse(HttpStatus.OK, "Logout successful")
    }

    private fun handleUserAuthentication(user: User, authPair: Pair<HttpStatus, String?>): ApiResponse<Map<String, Any?>> {
        if (authPair.first != HttpStatus.OK) {
            return ApiResponse(authPair.first, authPair.second)
        }

        val completedUser = userService.getCompleteUser(user)
            ?: return ApiResponse(HttpStatus.NOT_FOUND, "User not found")

        val username = completedUser.username
            ?: return ApiResponse(HttpStatus.BAD_REQUEST, "Username is missing")

        val token = jwtService.generateToken(username)
        val refreshToken = jwtService.generateRefreshToken(username)

        return ApiResponse(
            authPair.first,
            authPair.second,
            mapOf(
                "refreshToken" to refreshToken,
                "token" to token,
                "expiresIn" to expirationTime.toString(),
                "user" to UserDTO(
                    username = completedUser.username,
                    email = completedUser.email
                )
            )
        )
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody refreshTokenDTO: RefreshTokenDTO): ApiResponse<Any?> =
        if (jwtService.validateRefreshToken(refreshTokenDTO.username, refreshTokenDTO.refreshToken)) {
            val newToken = jwtService.generateToken(refreshTokenDTO.username)
            val refreshToken = jwtService.generateRefreshToken(refreshTokenDTO.username)
            ApiResponse(
                HttpStatus.OK,
                data = mapOf(
                    "token" to newToken,
                    "refreshToken" to refreshToken
                )
            )
        } else {
            ApiResponse(HttpStatus.UNAUTHORIZED, "无效的刷新令牌")
        }

    @PostMapping("/token")
    fun getToken(): ApiResponse<Map<String, String?>> =
        ApiResponse(
            HttpStatus.OK,
            data = mapOf("token" to jwtService.getTokenByUsername())
        )
}
