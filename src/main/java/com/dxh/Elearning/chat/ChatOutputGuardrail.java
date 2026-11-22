package com.dxh.Elearning.chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Component
public class ChatOutputGuardrail {
    private static final Set<String> ALLOWED_INTENTS = Set.of(
            "WORD_LOOKUP",
            "PHRASE_LOOKUP",
            "EXPLICIT_SAVE_REQUEST",
            "PASSAGE_TRANSLATION",
            "GRAMMAR_EXPLANATION",
            "GREETING",
            "GENERAL_CHAT",
            "UNSAFE_REQUEST"
    );
    private static final Set<String> QUIZ_INTENTS = Set.of(
            "WORD_LOOKUP",
            "PHRASE_LOOKUP",
            "EXPLICIT_SAVE_REQUEST"
    );
    private static final Set<String> LEAK_MARKERS = Set.of(
            "you are kma english assistant",
            "openai_api_key",
            "authorization: bearer",
            "begin system prompt",
            "developer message:"
    );

    private final int maxOutputCharacters;

    public ChatOutputGuardrail(
            @Value("${chatbot.guardrails.max-output-characters:3000}") int maxOutputCharacters
    ) {
        this.maxOutputCharacters = maxOutputCharacters;
    }

    public Optional<ChatAssistantResult> validateAndSanitize(ChatAssistantResult result) {
        if (result == null || blank(result.getAnswer()) || blank(result.getIntent())
                || result.getConfidence() == null
                || !Double.isFinite(result.getConfidence())
                || result.getConfidence() < 0.0
                || result.getConfidence() > 1.0) {
            return Optional.empty();
        }

        String intent = result.getIntent().trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_INTENTS.contains(intent)) {
            return Optional.empty();
        }

        String answer = stripUnsafeControls(result.getAnswer()).trim();
        if (answer.isBlank() || answer.length() > maxOutputCharacters || containsLeak(answer)) {
            return Optional.empty();
        }

        result.setAnswer(answer);
        result.setIntent(intent);
        if (!QUIZ_INTENTS.contains(intent)) {
            result.setLearningCandidate(null);
            result.setQuiz(null);
        }
        return Optional.of(result);
    }

    private boolean containsLeak(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        return LEAK_MARKERS.stream().anyMatch(normalized::contains);
    }

    private String stripUnsafeControls(String value) {
        StringBuilder safe = new StringBuilder(value.length());
        value.codePoints().forEach(codePoint -> {
            if (!Character.isISOControl(codePoint)
                    || codePoint == '\n' || codePoint == '\r' || codePoint == '\t') {
                safe.appendCodePoint(codePoint);
            }
        });
        return safe.toString();
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
