package com.miaogu.controller

import com.miaogu.response.R
import com.miaogu.service.ChatMessageService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chat")
class ChatMessageController(
    private val chatMessageService: ChatMessageService
) {
    @DeleteMapping("/clear")
    fun clearChatMessages(): R<Pair<String, String>?> {
        return R.success(chatMessageService.clearChatMessages(), null)
    }
}
