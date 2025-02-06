package com.miaogu.service
import com.baomidou.mybatisplus.extension.service.IService
import com.miaogu.entity.DeepSeekMessage
import com.miaogu.enums.RestoreStatus

interface DeepSeekService: IService<DeepSeekMessage> {
    fun clearWithVersion(username: String): Boolean
    fun restoreVersion(username: String): RestoreStatus
}
