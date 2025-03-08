package com.miaogu.context

import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@RequestScope
@Component
class UserContext {
    var username: String? = null
}
