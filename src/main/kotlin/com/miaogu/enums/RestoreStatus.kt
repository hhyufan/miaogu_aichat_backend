package com.miaogu.enums

import org.springframework.http.HttpStatus

enum class RestoreStatus(val status: HttpStatus, val template: String) {
    SUCCESS(HttpStatus.OK, "恢复成功"),
    NO_HISTORY(HttpStatus.NOT_FOUND, "无历史版本"),
    UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "服务器更新失败")
}
