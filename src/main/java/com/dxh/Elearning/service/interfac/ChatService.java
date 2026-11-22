package com.dxh.Elearning.service.interfac;

import com.dxh.Elearning.dto.request.ChatMessageDTO;

public interface ChatService {
    String chat(ChatMessageDTO request);
}
