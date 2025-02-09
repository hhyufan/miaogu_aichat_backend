package com.miaogu.request

data class MessageRequest(
    val offset: Int? = 0, // 从第几条记录开始，默认为0
    val size: Int? // 必填的每页大小
)
