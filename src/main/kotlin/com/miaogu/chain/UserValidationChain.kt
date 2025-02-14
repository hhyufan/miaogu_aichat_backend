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
        // 建立责任链 emailCheck: EmailFormatValidationHandler
        usernameDuplicateCheck.setNext(emailDuplicateCheckHandler).setNext(emailCheck)

    }

    fun validate(user: User): String? {

        usernameDuplicateCheck.handle(user)
        return usernameDuplicateCheck.currentError ?: emailDuplicateCheckHandler.currentError ?: emailCheck.currentError
            // 如果没有错误，返回 null
    }
}
