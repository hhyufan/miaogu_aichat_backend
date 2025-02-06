package com.miaogu.entity

import com.baomidou.mybatisplus.annotation.TableLogic
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.fasterxml.jackson.annotation.JsonFormat
import java.util.Date
import java.io.Serializable

@TableName("chat4_message")
data class Chat4Message(
    @TableId
    override val id: Long? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    override val time: Date,
    override val content: String,
    override val role: String,
    override var username: String? = null,
    @TableLogic(value = "0", delval = "null") // 禁用原生逻辑删除的自动更新
    @TableField(value = "delete_version")            // 手动控制逻辑删除版本
    override var deleteVersion: Int? = 0
): ChatMessage, Serializable
