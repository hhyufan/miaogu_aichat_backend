package com.miaogu.handler
import com.miaogu.entity.User

class EmailFormatValidationHandler : UserValidationHandler() {
    private var nextHandler: UserValidationHandler? = null

    override fun setNext(handler: UserValidationHandler): UserValidationHandler {
        nextHandler = handler
        return handler
    }

    override fun handle(user: User) {
        if (!user.email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))) {
            super.currentError = "邮箱格式错误！"
            return
        }
        nextHandler?.handle(user)
    }
}
