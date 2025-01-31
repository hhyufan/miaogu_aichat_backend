package com.miaogu.service

import com.baomidou.mybatisplus.extension.service.IService
import com.miaogu.entity.Chat3Message
import com.miaogu.entity.Chat4Message
import com.miaogu.enums.RestoreStatus

interface Chat4MessageService : IService<Chat4Message> {
    fun clearWithVersion(username: String): Boolean
    fun restoreVersion(username: String): RestoreStatus
}
