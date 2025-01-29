package com.miaogu.entity

import com.baomidou.mybatisplus.annotation.TableName
import com.miaogu.dto.UserDTO

@TableName("user")
data class User(
    val username: String? = null,
    val email: String? = null,
    var password: String? = null

) {
    constructor(username: String, password: String) : this(username, null, password)
}
