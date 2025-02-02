package com.miaogu.controller


import com.miaogu.exception.JwtValidationException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/exceptions")
class ExceptionController {

    @RequestMapping("/JwtValidationException")
    fun handleInvalidJwtException(request: HttpServletRequest) {
        val message = request.getAttribute("filter.exception.message") as String
        throw JwtValidationException(message)
    }

    @RequestMapping("/Exception")
    fun handleException(request: HttpServletRequest) {
        println("经过异常控制层")
        throw request.getAttribute("filter.exception") as Exception
    }
}
