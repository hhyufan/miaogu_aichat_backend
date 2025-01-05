package com.miaogu.service.impl

import com.miaogu.service.UserService
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.miaogu.chain.UserValidationChain
import com.miaogu.entity.User
import com.miaogu.mapper.UserMapper
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(private val userMapper: UserMapper) : ServiceImpl<UserMapper, User>(), UserService {

    override fun register(user: User?): Pair<String, String> {
        val validationChain = UserValidationChain(userMapper)

        val error = user?.let { validationChain.validate(it) }
        if (error != null) {
            return "error" to error
        } else {
            userMapper.insert(user!!)
            return "success" to "注册成功!"
        }
    }

    override fun login(user: User?): Pair<String, String> {
        // Validate user input
        if (user == null || (user.username.isEmpty() && user.email.isEmpty())) {
            return "error" to "用户名或邮箱不能为空"
        }

        // Function to check credentials
        fun checkCredentials(userInfo: User?, password: String?): Pair<String, String> {
            return when {
                userInfo == null -> "error" to "用户名或邮箱不存在"
                userInfo.password != password -> "error" to "密码错误"
                else -> "success" to "登录成功！"
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
