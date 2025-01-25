package com.miaogu.service.impl

import com.miaogu.service.UserService
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.miaogu.chain.UserValidationChain
import com.miaogu.entity.User
import com.miaogu.mapper.UserMapper
import com.miaogu.service.JwtService
import com.miaogu.service.PasswordService
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val userMapper: UserMapper,
    private val jwtService: JwtService,
    private val passwordService: PasswordService // 注入 PasswordService
) : ServiceImpl<UserMapper, User>(), UserService {

    override fun register(user: User?): Pair<String, String> {
        // 使用用户验证链进行验证
        val validationChain = UserValidationChain(userMapper)
        if (user != null) {
            println("password：" + user.password)
        }
        val errorMessage = user?.let { validationChain.validate(it) }

        return if (errorMessage != null) {
            "error" to errorMessage // 返回错误信息
        } else {
            // 生成 JWT 令牌并插入用户
            user?.let {
                // 对密码进行加盐和哈希处理
                it.password = passwordService.encodePassword(it.password)
                userMapper.insert(it)
                val token = jwtService.generateToken(it.username)
                "success" to token // 返回成功状态和 JWT
            } ?: run {
                "error" to "用户信息不能为空" // 处理 user 为 null 的情况
            }
        }
    }

    override fun login(user: User?): Pair<String, String> {
        // 验证用户输入
        if (user == null || (user.username.isEmpty() && user.email.isEmpty())) {
            return "error" to "用户名或邮箱不能为空"
        }

        fun checkCredentials(userInfo: User?, password: String?): Pair<String, String> = when {
            userInfo == null -> "error" to "用户名或邮箱不存在"
            !passwordService.matches(password ?: "", userInfo.password) -> "error" to "密码错误" // 使用 PasswordService 验证密码
            else -> {
                val token = jwtService.generateToken(userInfo.username) // 生成 JWT 令牌
                "success" to token // 返回成功状态和 JWT
            }
        }

        // 检查用户名
        userMapper.selectOne(QueryWrapper<User>().eq("username", user.username))?.let {
            return checkCredentials(it, user.password)
        }
        // 检查邮箱
        userMapper.selectOne(QueryWrapper<User>().eq("email", user.username))?.let {
            return checkCredentials(it, user.password)
        }

        // 如果用户名和邮箱都不存在
        return "error" to "用户名或邮箱不存在"
    }
}
