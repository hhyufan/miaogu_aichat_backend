package com.miaogu.response

import com.fasterxml.jackson.annotation.JsonProperty

data class DeepSeekResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>

) {
    data class Choice(
        val message: Message,
        val index: Int,
        @JsonProperty("finish_reason") // 映射 JSON 中的 "finish_reason" 字段
        val finishReason: String // 符合 Kotlin 命名规范
    ) {
        data class Message(
            val role: String,
            val content: String
        )
    }
}



