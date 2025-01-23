package com.miaogu.controller

import com.miaogu.service.UserService
import com.miaogu.dto.UserDTO
import com.miaogu.response.R
import com.miaogu.service.JwtService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService, private val jwtService: JwtService) {
    @PostMapping("/login")
    @ResponseBody
    fun login(@RequestBody userDTO: UserDTO): R<UserDTO> {
        val user = userDTO.toEntity()
        val loginPair = userService.login(user)
        val refreshToken = jwtService.generateRefreshToken(userDTO.username)

        return R.success(mapOf(
            "token" to loginPair.second,
            "refreshToken" to refreshToken
        ), userDTO)
    }

    @PostMapping("/register")
    @ResponseBody
    fun register(@RequestBody userDTO: UserDTO): R<UserDTO>  {
        val user = userDTO.toEntity()
        val registerPair = userService.login(user)
        val refreshToken = jwtService.generateRefreshToken(userDTO.username)

        return R.success( mapOf(
            "token" to registerPair.second,
            "refreshToken" to refreshToken
        ), userDTO)
    }
    @PostMapping("/refresh")
    fun refresh(@RequestParam username: String, @RequestParam refreshToken: String): R<Map<String, String>> {
        if (jwtService.validateRefreshToken(username, refreshToken)) {
            val newToken = jwtService.generateToken(username)
            val refreshToken = jwtService.generateRefreshToken(username)
            return R.success(mapOf(
                "token" to newToken,
                "refreshToken" to refreshToken
            ))
        } else {
            return R.fail("error" to "无效的刷新令牌")
        }
    }
}
