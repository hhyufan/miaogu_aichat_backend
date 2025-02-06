package com.miaogu.entity

import com.baomidou.mybatisplus.annotation.FieldFill
import com.baomidou.mybatisplus.annotation.*
import com.fasterxml.jackson.annotation.JsonFormat
import java.io.Serializable
import java.util.*

@TableName("deepSeek_message")
data class DeepSeekMessage(
    @TableId
    val id: Long? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "time", fill = FieldFill.INSERT) // 在插入时自动填充
    val time: Date? = null, // 创建时间，默认为 null
    val role: String = "user",
    val content: String,
    val username: String? = null,
    @TableLogic(value = "0", delval = "null")
    @TableField(value = "delete_version")
    var deleteVersion: Int? = 0
) : Serializable
