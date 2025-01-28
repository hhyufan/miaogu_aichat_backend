package com.miaogu.controller

import com.miaogu.dto.RefreshTokenDTO
import com.miaogu.service.UserService
import com.miaogu.dto.UserDTO
import com.miaogu.dto.UserResponseDTO
import com.miaogu.response.R
import com.miaogu.service.JwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService, private val jwtService: JwtService, ) {
    @Value("\${jwt.expire}")
    private val expirationTime: Long = 3600000 // 1小时
    @PostMapping("/login")
    @ResponseBody
    fun login(@RequestBody userDTO: UserDTO): R<UserResponseDTO> {
        val user = userDTO.toEntity()
        val loginPair = userService.login(user)
        val refreshToken = jwtService.generateRefreshToken(userDTO.username)
        val userResponseDTO = UserResponseDTO(
            username = userDTO.username.takeIf { it.isNotBlank() },
            email = userDTO.email?.takeIf { it.isNotBlank() }
        )
        if (loginPair.first == "error") {
            return R.fail("error" to loginPair.second, userResponseDTO)
        }

        return R.success(mapOf(
            "refreshToken" to refreshToken,
            "token" to loginPair.second,
            "expiresIn" to expirationTime.toString()
        ), userResponseDTO)
    }

    @PostMapping("/register")
    @ResponseBody
    fun register(@RequestBody userDTO: UserDTO): R<UserResponseDTO>  {
        val user = userDTO.toEntity()
        val refreshToken = jwtService.generateRefreshToken(userDTO.username)
        val registerPair = userService.register(user)
        val userResponseDTO = UserResponseDTO(
            username = userDTO.username,
            email = userDTO.email
        )
        if (registerPair.first == "error") {
            return R.fail("error" to registerPair.second, userResponseDTO)
        }
        return R.success( mapOf(
            "token" to registerPair.second,
            "refreshToken" to refreshToken,
            "expiresIn" to expirationTime.toString()
        ), userResponseDTO )
    }
    @PostMapping("/refresh")
    fun refresh(@RequestBody refreshTokenDTO: RefreshTokenDTO): R<out Map<String, Any>> {
        if (jwtService.validateRefreshToken(refreshTokenDTO.username, refreshTokenDTO.refreshToken)) {
            val newToken = jwtService.generateToken(refreshTokenDTO.username)
            val refreshToken = jwtService.generateRefreshToken(refreshTokenDTO.username)

            return R.success(mapOf(
                "token" to newToken,
                "refreshToken" to refreshToken
            ))
        } else {
            return R.fail("error" to "无效的刷新令牌")
        }
    }
    @PostMapping("/token")
    fun getToken(): R<Map<String, String?>> {
        val token = jwtService.getTokenByUsername()
        return R.success(mapOf("token" to token))
    }
}
