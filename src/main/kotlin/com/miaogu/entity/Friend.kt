package com.miaogu.entity

import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable

@TableName("friend")
data class Friend(
    @TableId
    val id: String,
    val name: String,
    val detail: String? = null
) : Serializable
