package com.miaogu.controller

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.miaogu.annotation.RequireJwt
import com.miaogu.entity.Chat4Message
import com.miaogu.entity.DeepSeekMessage
import com.miaogu.extension.toJson
import com.miaogu.request.MessageRequest
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
    fun getMessages(
        @RequestBody request: MessageRequest // 使用请求体接收参数
    ): ApiResponse<List<Chat4Message>> {
        val queryWrapper = QueryWrapper<Chat4Message>()
        queryWrapper.orderByDesc("id") // 按 id 降序排列

        if (request.size != null) {
            queryWrapper.last("LIMIT ${request.size} OFFSET ${request.offset ?: 0}") // 使用请求体中的分页大小和偏移量
        }

        val data = chat4MessageService.list(queryWrapper.eq("username", username)).reversed()
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
        response?.let { content ->
            chat4MessageService.save(Chat4Message(null, chatMessage.time, content, "AI", chatMessage.username))
        }
        return ApiResponse(HttpStatus.OK, data = response)
    }

    @PostMapping("/response")
    fun responseChat3Message(@RequestBody chatMessage: Chat4Message): ApiResponse<Void> {
        chat4MessageService.save(chatMessage)
        return ApiResponse(HttpStatus.OK)
    }
}
