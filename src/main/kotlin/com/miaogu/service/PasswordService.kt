package com.miaogu.service

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class PasswordService(private val passwordEncoder: BCryptPasswordEncoder) {

    // 加密密码
    fun encodePassword(rawPassword: String): String {
        return passwordEncoder.encode(rawPassword)
    }

    // 验证密码
    fun matches(rawPassword: String, encodedPassword: String): Boolean {
        println("rawPassword: $rawPassword")
        println("encodedPassword: $encodedPassword")
        return passwordEncoder.matches(rawPassword, encodedPassword)
    }
}
