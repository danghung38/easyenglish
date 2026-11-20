package com.danghung.elearning.model

data class ChatMessage(
    val message: String,
    val isUser: Boolean,
    var isAnimating: Boolean = false,
    val isTyping: Boolean = false  // ← thêm flag này
)