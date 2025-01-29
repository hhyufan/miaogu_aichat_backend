package com.miaogu.controller

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.miaogu.annotation.RequireJwt
import com.miaogu.entity.Chat3Message
import com.miaogu.extension.toJson
import com.miaogu.response.ApiResponse
import com.miaogu.service.Chat3MessageService
import com.miaogu.service.ChatService
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/1002")
@RequireJwt
class Chat3MessageController(
    private val chat3MessageService: Chat3MessageService,
    private val chatService: ChatService,
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val username: String?
        get() = redisTemplate.opsForValue().get("username")

    /**
     * 获取所有聊天3.5消息
     */
    @PostMapping("/messages")
    fun getChat3MessagesByUsername(): ApiResponse<List<Chat3Message>> {
        val data = chat3MessageService.list(QueryWrapper<Chat3Message>().eq("username", username))
        return ApiResponse(HttpStatus.OK, data = data)
    }

    /**
     * 发送聊天3.5消息
     */
    @PostMapping("/send")
    fun sendChat3Message(@RequestBody chatMessage: Chat3Message): ApiResponse<String?> {
        chatMessage.username = username
        chat3MessageService.save(chatMessage)
        val response = chatService.chat(chatMessage, chat3MessageService.list(QueryWrapper<Chat3Message>().eq("username", username)).toJson(), 3)
        response?.let { msg ->
            chat3MessageService.save(Chat3Message(null, chatMessage.time, msg, "AI", chatMessage.username))
        }
        return ApiResponse(HttpStatus.OK, data = response)
    }

    @PostMapping("/response")
    fun responseChat3Message(@RequestBody chatMessage: Chat3Message): ApiResponse<Void> {
        chat3MessageService.save(chatMessage)
        return ApiResponse(HttpStatus.OK)
    }
}
