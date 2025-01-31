package com.miaogu.controller

import com.miaogu.annotation.RequireJwt
import com.miaogu.response.ApiResponse
import com.miaogu.service.ChatMessageService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chat")
@RequireJwt
class ChatMessageController(
    private val chatMessageService: ChatMessageService

) {
    @DeleteMapping("/clear")
    fun clearChatMessages(): ApiResponse<Pair<String, String>?> {
        return ApiResponse(HttpStatus.NO_CONTENT, chatMessageService.clearChatMessages())
    }

    @PostMapping("/revert")
    fun revertChatMessages(): ApiResponse<Pair<String, String>?> {
        val revertStatus = chatMessageService.revertVersion()
        return ApiResponse(revertStatus.status, revertStatus.template)
    }
}
