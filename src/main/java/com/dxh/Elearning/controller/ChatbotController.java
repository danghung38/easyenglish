package com.dxh.Elearning.controller;

import com.dxh.Elearning.dto.request.ChatMessageDTO;
import com.dxh.Elearning.service.interfac.ChatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/assistant")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ChatbotController {
    ChatService chatService;

    @PostMapping("/chat")
    public String chat(@RequestBody ChatMessageDTO request) {
        return chatService.chat(request);
    }
}