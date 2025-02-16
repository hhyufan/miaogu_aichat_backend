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
    private val passwordService: PasswordService // 注入 PasswordService
) : ServiceImpl<UserMapper, User>(), UserService {

    override fun register(user: User?): Pair<HttpStatus, String?> {
        // 使用用户验证链进行验证
        val validationChain = UserValidationChain(userMapper)
        if (user != null) {
            println("password：" + user.password)
        }
        val errorMessage = user?.let { validationChain.validate(it) }

        return if (errorMessage != null) {
            HttpStatus.BAD_REQUEST to errorMessage // 返回错误信息
        } else {
            // 生成 JWT 令牌并插入用户
            user?.let {
                // 对密码进行加盐和哈希处理
                it.password = it.password?.let { rawPassword -> passwordService.encodePassword(rawPassword) }
                userMapper.insert(it)

                HttpStatus.OK to "注册成功" // 返回成功状态和 JWT
            } ?: run {
                HttpStatus.BAD_REQUEST to "用户信息不能为空" // 处理 user 为 null 的情况
            }
        }
    }

    override fun login(user: User?): Pair<HttpStatus, String?> {
        // 验证用户输入
        if (user == null || (user.username?.isEmpty() == true && user.email?.isEmpty() == true)) {
            return HttpStatus.BAD_REQUEST to "用户名或邮箱不能为空"
        }
        fun checkCredentials(userInfo: User?, password: String?): Pair<HttpStatus, String?> = when {
            userInfo == null -> HttpStatus.BAD_REQUEST to "用户名或邮箱不存在"
            !userInfo.password?.let { passwordService.matches(password ?: "", it) }!! -> HttpStatus.BAD_REQUEST to "密码错误" // 使用 PasswordService 验证密码
            else -> {
                HttpStatus.OK to "登录成功" // 返回成功状态和 JWT
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
        return HttpStatus.BAD_REQUEST to "用户名或邮箱不存在"
    }

    @Transactional
    override fun getCompleteUser(user: User?): User? {
        val queryWrapper = QueryWrapper<User>()

        user?.username?.takeIf { it.isNotEmpty() }?.let {
            queryWrapper.eq("username", it).or()
        }

        user?.email?.takeIf { it.isNotEmpty() }?.let {
            queryWrapper.eq("email", it)
        }
        queryWrapper.last("LIMIT 1")
        return this.getOne(queryWrapper)
    }
}
