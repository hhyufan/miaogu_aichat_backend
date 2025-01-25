package com.miaogu.entity

import com.baomidou.mybatisplus.annotation.TableName
import com.miaogu.dto.UserDTO

@TableName("user")
data class User(
    val username: String = "",
    val email: String = "",
    var password: String = ""

) {
    fun toDTO() : UserDTO {
        return UserDTO(
            username = this.username,
            password = this.password,
            email = this.email
        )
    }

    constructor(username: String, password: String) : this(username, "", password)
}
