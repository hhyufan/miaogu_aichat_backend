package com.miaogu.service.impl

import com.miaogu.service.UserService
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.miaogu.chain.UserValidationChain
import com.miaogu.entity.User
import com.miaogu.mapper.UserMapper
import com.miaogu.service.JwtService
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(private val userMapper: UserMapper, private val jwtService: JwtService) : ServiceImpl<UserMapper, User>(), UserService {

    override fun register(user: User?): Pair<String, String> {
        val validationChain = UserValidationChain(userMapper)

        val error = user?.let { validationChain.validate(it) }
        if (error != null) {
            return "error" to error
        } else {
            val token = user?.let { jwtService.generateToken(it.username) }
            userMapper.insert(user!!)
            return "success" to token!!
        }
    }

    override fun login(user: User?): Pair<String, String> {
        // Validate user input
        if (user == null || (user.username.isEmpty() && user.email.isEmpty())) {
            return "error" to "用户名或邮箱不能为空"
        }

        fun checkCredentials(userInfo: User?, password: String?): Pair<String, String> = when {
            userInfo == null -> "error" to "用户名或邮箱不存在"
            userInfo.password != password -> "error" to "密码错误"
            else -> {

                val token = jwtService.generateToken(userInfo.username) // 生成 JWT 令牌
                "success" to token // 返回成功状态和 JWT
            }
        }

        // Check username
        userMapper.selectOne(QueryWrapper<User>().eq("username", user.username))?.let {
            return checkCredentials(it, user.password)
        }
        // Check email
        userMapper.selectOne(QueryWrapper<User>().eq("email", user.username))?.let {
            return checkCredentials(it, user.password)
        }

        // If neither username nor email exists
        return "error" to "用户名或邮箱不存在"
    }
}
