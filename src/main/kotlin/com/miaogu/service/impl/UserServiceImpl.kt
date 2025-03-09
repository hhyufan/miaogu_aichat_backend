package com.miaogu.service.impl

import com.miaogu.service.UserService
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.miaogu.chain.UserValidationChain
import com.miaogu.entity.User
import com.miaogu.mapper.UserMapper
import com.miaogu.service.PasswordService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserServiceImpl(
    private val userMapper: UserMapper,
    private val passwordService: PasswordService
) : ServiceImpl<UserMapper, User>(), UserService {

    override fun register(user: User?): Pair<HttpStatus, String?> {
        val validationChain = UserValidationChain(userMapper)

        return user?.let { nonNullUser ->
            val errorMessage = validationChain.validate(nonNullUser)

            if (errorMessage != null) {
                HttpStatus.BAD_REQUEST to errorMessage
            } else {
                // 对密码进行加盐和哈希处理
                nonNullUser.password = nonNullUser.password?.let { passwordService.encodePassword(it) }
                userMapper.insert(nonNullUser)
                HttpStatus.OK to "注册成功"
            }
        } ?: (HttpStatus.BAD_REQUEST to "用户信息不能为空")
    }

    override fun login(user: User?): Pair<HttpStatus, String?> {
        // 验证用户输入
        if (user == null || (user.username.isNullOrEmpty() && user.email.isNullOrEmpty())) {
            return HttpStatus.BAD_REQUEST to "用户名或邮箱不能为空"
        }

        // 定义验证凭据的函数
        fun checkCredentials(userInfo: User?, password: String?): Pair<HttpStatus, String?> = when {
            userInfo == null -> HttpStatus.BAD_REQUEST to "用户名或邮箱不存在"
            !userInfo.password?.let { passwordService.matches(password ?: "", it) }!! ->
                HttpStatus.BAD_REQUEST to "密码错误"
            else -> HttpStatus.OK to "登录成功"
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
        return HttpStatus.BAD_REQUEST to "用户名或邮箱不存在"
    }

    @Transactional
    override fun getCompleteUser(user: User?): User? {
        if (user == null) return null

        return user.username?.takeIf { it.isNotEmpty() }?.let { username ->
            val queryWrapper = QueryWrapper<User>().apply {
                eq("username", username).or()
                eq("email", username)
                last("LIMIT 1")
            }
            getOne(queryWrapper)
        }
    }
}
