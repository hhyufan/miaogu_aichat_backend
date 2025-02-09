package com.miaogu.controller

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.miaogu.annotation.RequireJwt
import com.miaogu.entity.Chat3Message
import com.miaogu.extension.toJson
import com.miaogu.request.MessageRequest
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
    fun getMessages(
        @RequestBody request: MessageRequest // 使用请求体接收参数
    ): ApiResponse<List<Chat3Message>> {
        val queryWrapper = QueryWrapper<Chat3Message>()
        queryWrapper.orderByDesc("id") // 按 id 降序排列

        if (request.size != null) {
            queryWrapper.last("LIMIT ${request.size} OFFSET ${request.offset ?: 0}") // 使用请求体中的分页大小和偏移量
        }

        val data = chat3MessageService.list(queryWrapper.eq("username", username)).reversed()
        return ApiResponse(HttpStatus.OK, data = data)
    }

    /**
     * 发送聊天3.5消息
     */
    @PostMapping("/send")
    fun sendChat3Message(@RequestBody chatMessage: Chat3Message): ApiResponse<String?> {
        chatMessage.username = username
        chatMessage.deleteVersion = 0
        chat3MessageService.save(chatMessage)
        val response = chatService.chat(chatMessage, chat3MessageService.list(QueryWrapper<Chat3Message>().eq("username", username)).toJson(), 3)
        response?.let { content ->
            chat3MessageService.save(Chat3Message(null, chatMessage.time, content, "AI", chatMessage.username))
        }
        return ApiResponse(HttpStatus.OK, data = response)
    }

    @PostMapping("/response")
    fun responseChat3Message(@RequestBody chatMessage: Chat3Message): ApiResponse<Void> {
        chat3MessageService.save(chatMessage)
        return ApiResponse(HttpStatus.OK)
    }
}
