package com.miaogu.service

import com.baomidou.mybatisplus.extension.service.IService
import com.miaogu.entity.User

interface UserService : IService<User?> {
    fun login(user: User?): Pair<String, String>
    fun register(user: User?): Pair<String, String>
}
