package com.miaogu.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.miaogu.mapper.Chat3MessageMapper
import com.miaogu.mapper.Chat4MessageMapper
import org.springframework.stereotype.Service

@Service
class ChatMessageService(
    private val chat4MessageMapper: Chat4MessageMapper,
    private val chat3MessageMapper: Chat3MessageMapper
) {
    fun clearChatMessages(): Pair<String, String>? {
        val chatMsgSum = countChatMessages()
        // 直接调用 MyBatis-Plus 的删除方法清空表
        chat4MessageMapper.delete(null)
        chat3MessageMapper.delete(null)
        return "success" to "清空聊天记录成功！共清除了${chatMsgSum}条记录！"
    }

    private fun countChatMessages(): Long {
        // 使用 QueryWrapper 来构建查询条件，若需要计数所有消息，可以传入空的 QueryWrapper
        val chat3Counter =  chat3MessageMapper.selectCount(QueryWrapper())
        val chat4Counter = chat4MessageMapper.selectCount(QueryWrapper())
        return chat3Counter + chat4Counter
    }
}
