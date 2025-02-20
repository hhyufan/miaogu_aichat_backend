package com.miaogu.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.miaogu.entity.Chat3Message
import com.miaogu.entity.Chat4Message
import com.miaogu.entity.DeepSeekMessage
import com.miaogu.enums.RestoreStatus
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class ChatMessageService(
    private val chat4MessageService: Chat4MessageService,
    private val chat3MessageService: Chat3MessageService,
    private val deepSeekService: DeepSeekService,
    private val redisTemplate: RedisTemplate<String, String>
) {
    val username: String?
        get() {
            return redisTemplate.opsForValue().get("username")
        }
    @CacheEvict(value = ["chat3Messages", "chat4Messages", "deepSeekMessages"], allEntries = true) // 清空相关缓存
    fun clearChatMessages(): String {
        val chatMsgSum = countChatMessages()
        // 直接调用 MyBatis-Plus 的删除方法清空表
        username?.let {
            chat4MessageService.clearWithVersion(it)
            chat3MessageService.clearWithVersion(it)
            deepSeekService.clearWithVersion(it)
        }
        return "清空聊天记录成功！共清除了${chatMsgSum}条记录！"
    }

    private fun countChatMessages(): Long {
        val chat3QueryWrapper = QueryWrapper<Chat3Message>()
        val chat4QueryWrapper = QueryWrapper<Chat4Message>()
        val deepSeekQueryWrapper = QueryWrapper< DeepSeekMessage>()
        chat3QueryWrapper.eq("username", username)
        chat4QueryWrapper.eq("username", username)
        deepSeekQueryWrapper.eq("username", username)
        val chat3Counter =  chat3MessageService.count(chat3QueryWrapper)
        val chat4Counter = chat4MessageService.count(chat4QueryWrapper)
        val deepSeekCounter = deepSeekService.count(deepSeekQueryWrapper)
        return chat3Counter + chat4Counter + deepSeekCounter
    }

    fun revertVersion(): RestoreStatus {
        val chat3Status = chat3MessageService.restoreVersion(username!!)
        val chat4Status = chat4MessageService.restoreVersion(username!!)
        val deepSeekStatus = deepSeekService.restoreVersion(username!!)
        return when {
            chat3Status == RestoreStatus.NO_HISTORY && chat4Status == RestoreStatus.NO_HISTORY && deepSeekStatus == RestoreStatus.NO_HISTORY-> RestoreStatus.NO_HISTORY

            RestoreStatus.UPDATE_FAILED in listOf(chat3Status, chat4Status, deepSeekStatus) -> RestoreStatus.UPDATE_FAILED

            RestoreStatus.SUCCESS in listOf(chat3Status, chat4Status, deepSeekStatus) -> RestoreStatus.SUCCESS

            else -> RestoreStatus.UPDATE_FAILED
        }
    }
}
