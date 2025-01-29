package com.miaogu.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class UserDTO(
    val username: String? = "",
    val password: String? = "",
    val email: String? = ""
) {
    constructor(username: String?, email: String?) : this(username = username, email = email, password = "")
}
