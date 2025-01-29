package com.miaogu.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.miaogu.entity.Chat3Message
import com.miaogu.entity.Chat4Message
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class ChatMessageService(
    private val chat4MessageService: Chat4MessageService,
    private val chat3MessageService: Chat3MessageService,
    private val redisTemplate: RedisTemplate<String, String>
) {
    val username: String?
        get() {
            return redisTemplate.opsForValue().get("username")
        }
    @CacheEvict(value = ["chat3Messages", "chat4Messages"], allEntries = true) // 清空相关缓存
    fun clearChatMessages(): String {
        val chatMsgSum = countChatMessages()
        // 直接调用 MyBatis-Plus 的删除方法清空表
        val chat3QueryWrapper = QueryWrapper<Chat3Message>()
        val chat4QueryWrapper = QueryWrapper<Chat4Message>()
        chat3QueryWrapper.eq("username", username)
        chat4QueryWrapper.eq("username", username)
        chat4MessageService.remove(chat4QueryWrapper)
        chat3MessageService.remove(chat3QueryWrapper)
        return "清空聊天记录成功！共清除了${chatMsgSum}条记录！"
    }

    private fun countChatMessages(): Long {
        val chat3QueryWrapper = QueryWrapper<Chat3Message>()
        val chat4QueryWrapper = QueryWrapper<Chat4Message>()
        chat3QueryWrapper.eq("username", username)
        chat4QueryWrapper.eq("username", username)
        val chat3Counter =  chat3MessageService.count(chat3QueryWrapper)
        val chat4Counter = chat4MessageService.count(chat4QueryWrapper)
        return chat3Counter + chat4Counter
    }
}
