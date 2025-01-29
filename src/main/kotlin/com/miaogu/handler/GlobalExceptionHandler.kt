package com.miaogu.handler

import com.miaogu.exception.JwtValidationException
import com.miaogu.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    // 处理JWT验证相关异常
    @ExceptionHandler(JwtValidationException::class)
    fun handleJwtValidationException(e: JwtValidationException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse(HttpStatus.UNAUTHORIZED, e.message))
    }

    // 处理所有RuntimeException
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(e: RuntimeException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse(HttpStatus.BAD_REQUEST, e.message ?: "请求处理失败"))
    }

    // 处理其他未捕获异常
    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.message ?: "系统内部错误"))
    }
}
