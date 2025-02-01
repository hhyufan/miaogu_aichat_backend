package com.miaogu.service

import com.baomidou.mybatisplus.extension.service.IService
import com.miaogu.entity.User
import org.springframework.http.HttpStatus

interface UserService : IService<User?> {
    fun login(user: User?): Pair<HttpStatus, String?>
    fun register(user: User?): Pair<HttpStatus, String?>
    fun getCompleteUser(user: User?): User?
}
