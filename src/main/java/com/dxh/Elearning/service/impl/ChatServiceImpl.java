package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.chat.ChatAssistantResult;
import com.dxh.Elearning.chat.ChatInputGuardrail;
import com.dxh.Elearning.chat.ChatOutputGuardrail;
import com.dxh.Elearning.chat.ChatPromptFactory;
import com.dxh.Elearning.chat.ChatRateLimiter;
import com.dxh.Elearning.chat.ChatStructuredResponseParser;
import com.dxh.Elearning.dto.request.ChatMessageDTO;
import com.dxh.Elearning.service.interfac.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {
    private static final String FALLBACK_MESSAGE =
            "Hệ thống chưa thể xử lý câu hỏi này, vui lòng thử lại sau.";
    private static final String RATE_LIMIT_MESSAGE =
            "Bạn đang gửi quá nhiều câu hỏi. Vui lòng đợi một phút rồi thử lại.";
    private static final String QUIZ_SAVED_MESSAGE =
            "\n\nMình đã thêm một câu hỏi vào My Chatbot Quiz trong tab Learn.";

    private final ChatClient chatClient;
    private final ChatStructuredResponseParser responseParser;
    private final ChatQuizService chatQuizService;
    private final ChatInputGuardrail inputGuardrail;
    private final ChatOutputGuardrail outputGuardrail;
    private final ChatPromptFactory promptFactory;
    private final ChatRateLimiter rateLimiter;

    public ChatServiceImpl(
            ChatClient.Builder chatClientBuilder,
            ChatStructuredResponseParser responseParser,
            ChatQuizService chatQuizService,
            ChatInputGuardrail inputGuardrail,
            ChatOutputGuardrail outputGuardrail,
            ChatPromptFactory promptFactory,
            ChatRateLimiter rateLimiter
    ) {
        this.chatClient = chatClientBuilder.build();
        this.responseParser = responseParser;
        this.chatQuizService = chatQuizService;
        this.inputGuardrail = inputGuardrail;
        this.outputGuardrail = outputGuardrail;
        this.promptFactory = promptFactory;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public String chat(ChatMessageDTO request) {
        String rawInput = request == null ? null : request.getMessage();
        ChatInputGuardrail.Decision inputDecision = inputGuardrail.inspect(rawInput);
        if (!inputDecision.allowed()) {
            log.info("Chat input rejected by guardrail: {}", inputDecision.reason());
            return inputDecision.message();
        }
        if (!rateLimiter.tryAcquire(currentIdentity())) {
            log.info("Chat request rejected by rate limiter");
            return RATE_LIMIT_MESSAGE;
        }

        String safeInput = inputDecision.normalizedInput();
        try {
            String rawResponse = chatClient.prompt(promptFactory.create(safeInput)).call().content();
            Optional<ChatAssistantResult> safeResult = responseParser.parse(rawResponse)
                    .flatMap(outputGuardrail::validateAndSanitize);
            if (safeResult.isEmpty()) {
                log.warn("Chat model response rejected by output guardrail");
                return FALLBACK_MESSAGE;
            }

            ChatAssistantResult result = safeResult.get();
            boolean quizSaved = chatQuizService.saveIfEligible(result, safeInput);
            return quizSaved ? result.getAnswer() + QUIZ_SAVED_MESSAGE : result.getAnswer();
        } catch (Exception exception) {
            log.error("Failed to call chatbot model: {}", exception.getMessage());
            return FALLBACK_MESSAGE;
        }
    }

    private String currentIdentity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }
        return authentication.getName();
    }
}
