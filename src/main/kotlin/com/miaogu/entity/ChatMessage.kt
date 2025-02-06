package com.miaogu.entity

import java.util.*

interface ChatMessage {
    val id: Long?
    val time: Date
    val content: String
    val role: String
    var username: String?
    var deleteVersion: Int?
}
