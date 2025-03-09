package com.miaogu.chain

import com.miaogu.entity.User
import com.miaogu.handler.EmailDuplicateCheckHandler
import com.miaogu.handler.EmailFormatValidationHandler
import com.miaogu.handler.UsernameDuplicateCheckHandler
import com.miaogu.mapper.UserMapper

class UserValidationChain(target: UserMapper) {
    private val usernameDuplicateCheck = UsernameDuplicateCheckHandler(target)
    private val emailDuplicateCheckHandler = EmailDuplicateCheckHandler(target)
    private val emailCheck = EmailFormatValidationHandler()

    init {
        // 构建责任链
        usernameDuplicateCheck
            .setNext(emailDuplicateCheckHandler)
            .setNext(emailCheck)
    }

    fun validate(user: User): String? =
        usernameDuplicateCheck.apply { handle(user) }.currentError
            ?: emailDuplicateCheckHandler.currentError
            ?: emailCheck.currentError
}
