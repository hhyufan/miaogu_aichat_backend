package com.miaogu.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service

@Service
class ChatMessageService(
    private val chat4MessageService: Chat4MessageService,
    private val chat3MessageService: Chat3MessageService
) {
    @CacheEvict(value = ["chat3Messages", "chat4Messages"], allEntries = true) // 清空相关缓存
    fun clearChatMessages(): Pair<String, String>? {
        val chatMsgSum = countChatMessages()
        // 直接调用 MyBatis-Plus 的删除方法清空表
        chat4MessageService.remove(QueryWrapper())
        chat3MessageService.remove(QueryWrapper())
        return "success" to "清空聊天记录成功！共清除了${chatMsgSum}条记录！"
    }

    private fun countChatMessages(): Long {
        // 使用 QueryWrapper 来构建查询条件，若需要计数所有消息，可以传入空的 QueryWrapper
        val chat3Counter =  chat3MessageService.count(QueryWrapper())
        val chat4Counter = chat4MessageService.count(QueryWrapper())
        return chat3Counter + chat4Counter
    }
}
