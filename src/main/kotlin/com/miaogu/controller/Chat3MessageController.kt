package com.miaogu.controller

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.miaogu.annotation.RequireJwt
import com.miaogu.entity.Chat3Message
import com.miaogu.extension.toJson
import com.miaogu.response.R
import com.miaogu.service.Chat3MessageService
import com.miaogu.service.ChatService
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/1002")
@RequireJwt
class Chat3MessageController(
    private val chat3MessageService: Chat3MessageService,
    private val chatService: ChatService,
    private val redisTemplate: RedisTemplate<String, String>
) {
    val username: String?
        get() {
            return redisTemplate.opsForValue().get("username")
        }

    /**
     * 获取所有聊天3.5消息
     */
    @PostMapping("/messages")
    fun getChat3MessagesByUsername(): R<List<Chat3Message>> {
        val queryWrapper = QueryWrapper<Chat3Message>()
        queryWrapper.eq("username", username)
        val messages = chat3MessageService.list(queryWrapper) // 根据 username 查询聊天记录
        return R.success(messages)
    }
    /**
     * 发送聊天3.5消息
     */
    @PostMapping("/send")
    fun sendChat3Message(@RequestBody chatMessage: Chat3Message): R<String?> {
        val queryWrapper = QueryWrapper<Chat3Message>()
        queryWrapper.eq("username", username)
        chatMessage.username = username
        chat3MessageService.save(chatMessage)
        val response = chatService.chat(chatMessage, chat3MessageService.list(queryWrapper).toJson(), 3)
        response?.let { msg ->
            Chat3Message(null, chatMessage.time, msg, "AI", chatMessage.username).also {
                chat3MessageService.save(it)
            }
        }
        return R.success(response) // 返回成功响应
    }

    @PostMapping("/response")
    fun responseChat3Message(@RequestBody chatMessage: Chat3Message): R<Void> {

        chat3MessageService.save(chatMessage) // 使用IService的save()方法
        return R.success() // 返回成功响应
    }
}
