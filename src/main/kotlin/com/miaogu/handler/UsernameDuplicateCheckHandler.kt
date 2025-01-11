package com.miaogu.handler

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.miaogu.entity.User
import com.miaogu.mapper.UserMapper

class UsernameDuplicateCheckHandler(private var target: UserMapper) : UserValidationHandler() {
    private var nextHandler: UserValidationHandler? = null

    override fun setNext(handler: UserValidationHandler): UserValidationHandler {
        nextHandler = handler
        return handler
    }

    override fun handle(user: User) {
        if (target.selectOne(QueryWrapper<User>().eq("username", user.username)) != null) {
            currentError = "用户名已存在！"
            return
        }
        nextHandler?.handle(user)
    }
}
