package com.miaogu.controller

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.miaogu.annotation.RequireJwt
import com.miaogu.entity.Chat4Message
import com.miaogu.extension.toJson
import com.miaogu.response.ApiResponse
import com.miaogu.service.Chat4MessageService
import com.miaogu.service.ChatService
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpStatus
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
        get() = redisTemplate.opsForValue().get("username")
    /**
     * 获取所有聊天4.0消息
     */
    @PostMapping("/messages")
    fun getChat4Messages(): ApiResponse<List<Chat4Message>> {
        val data = chat4MessageService.list(QueryWrapper<Chat4Message>().eq("username", username))
        return ApiResponse(HttpStatus.OK, data = data)
    }

    /**
     * 发送聊天4.0消息
     */
    @PostMapping("/send")
    fun sendChat4Message(@RequestBody chatMessage: Chat4Message): ApiResponse<String?> {
        chatMessage.username = username
        chatMessage.deleteVersion = 0
        chat4MessageService.save(chatMessage)
        val response = chatService.chat(chatMessage, chat4MessageService.list(QueryWrapper<Chat4Message>().eq("username", username)).toJson(), 3)
        response?.let { msg ->
            chat4MessageService.save(Chat4Message(null, chatMessage.time, msg, "AI", chatMessage.username))
        }
        return ApiResponse(HttpStatus.OK, data = response)
    }

    @PostMapping("/response")
    fun responseChat3Message(@RequestBody chatMessage: Chat4Message): ApiResponse<Void> {
        chat4MessageService.save(chatMessage)
        return ApiResponse(HttpStatus.OK)
    }
}
