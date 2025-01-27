package com.miaogu.dto

import com.miaogu.entity.User

data class UserDTO(
    val username: String = "",
    val password: String? = "",
    val email: String? = ""
) {
    fun toEntity(): User {
        return User(
            username = username,
            email = email,
            password = password
        )
    }

    constructor(username: String, password: String) : this(username, "", password)
}
