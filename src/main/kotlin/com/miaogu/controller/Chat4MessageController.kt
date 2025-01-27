package com.miaogu.controller

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.miaogu.annotation.RequireJwt
import com.miaogu.entity.Chat3Message
import com.miaogu.entity.Chat4Message
import com.miaogu.extension.toJson
import com.miaogu.response.R
import com.miaogu.service.Chat4MessageService
import com.miaogu.service.ChatService
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/1003")
@RequireJwt
class Chat4MessageController(
    private val chat4MessageService: Chat4MessageService,
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
    @GetMapping("/messages")
    fun getChat3Messages(): R<List<Chat4Message>> {
        val queryWrapper = QueryWrapper<Chat3Message>()
        queryWrapper.eq("username", username)
        val messages = chat4MessageService.list() // 使用IService的list()方法获取所有消息
        return R.success(messages)
    }
    /**
     * 发送聊天3.5消息
     */
    @PostMapping("/send")
    fun sendChat4Message(@RequestBody chatMessage: Chat4Message): R<String?> {
        val queryWrapper = QueryWrapper<Chat4Message>()
        queryWrapper.eq("username", username)
        chatMessage.username = username
        chat4MessageService.save(chatMessage)
        Chat3Message(null, chatMessage.time, chatMessage.msg, "User", username)
        val response = chatService.chat(chatMessage, chat4MessageService.list(queryWrapper).toJson(), 3)
        response?.let { msg ->
            Chat4Message(null, chatMessage.time, msg, "AI", username).also {
                chat4MessageService.save(it)
            }
        }
        return R.success(response) // 返回成功响应
    }

    @PostMapping("/response")
    fun responseChat3Message(@RequestBody chatMessage: Chat4Message): R<Void> {

        chat4MessageService.save(chatMessage) // 使用IService的save()方法
        return R.success() // 返回成功响应
    }
}
