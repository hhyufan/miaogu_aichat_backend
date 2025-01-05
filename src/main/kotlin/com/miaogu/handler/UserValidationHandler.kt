package com.miaogu.handler

import com.miaogu.entity.User

abstract class UserValidationHandler {
    var currentError: String? = null
    abstract fun setNext(handler: UserValidationHandler): UserValidationHandler
    abstract fun handle(user: User)
}
