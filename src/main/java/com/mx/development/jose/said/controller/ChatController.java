package com.mx.development.jose.said.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mx.development.jose.said.service.chat.ChatService;

@RestController
@RequestMapping("api/v1/chat-service")
public class ChatController {

    private final ChatService chatService;
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/chat")
        public String chat(String question) {
         return chatService.chat(question);
        }
}
