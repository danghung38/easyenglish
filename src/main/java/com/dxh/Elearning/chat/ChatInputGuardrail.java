package com.dxh.Elearning.chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class ChatInputGuardrail {
    private static final String EMPTY_MESSAGE = "Vui lòng nhập câu hỏi về tiếng Anh.";
    private static final String TOO_LONG_MESSAGE =
            "Nội dung quá dài. Vui lòng gửi tối đa %d ký tự mỗi lần.";
    private static final String BLOCKED_MESSAGE =
            "Yêu cầu này đã bị chặn vì có dấu hiệu can thiệp chỉ dẫn hệ thống. "
                    + "Bạn hãy đặt lại câu hỏi học tiếng Anh theo cách thông thường.";

    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            pattern("\\b(ignore|disregard|forget|override)\\b.{0,60}"
                    + "\\b(previous|prior|above|system|developer|hidden)\\b.{0,30}"
                    + "\\b(instructions?|rules?|prompt|message)\\b"),
            pattern("\\b(reveal|show|print|repeat|leak|expose)\\b.{0,60}"
                    + "\\b(system|developer|hidden|initial)\\b.{0,30}"
                    + "\\b(prompt|message|instructions?|rules?)\\b"),
            pattern("\\b(you are now|act as|pretend (to be|you are)|new role)\\b.{0,60}"
                    + "\\b(system|developer|admin|unrestricted|dan)\\b"),
            pattern("\\b(jailbreak|developer mode|do anything now|bypass.{0,20}"
                    + "(safety|guardrail|policy|restriction))\\b"),
            pattern("(<\\|system\\|>|<\\|assistant\\|>|\\[/?inst]|"
                    + "###\\s*(system|developer|assistant))"),
            pattern("\\b(bỏ qua|quên|phớt lờ|ghi đè)\\b.{0,70}"
                    + "\\b(hướng dẫn|chỉ dẫn|quy tắc|prompt|lệnh)\\b.{0,30}"
                    + "\\b(trước|trên|hệ thống|developer)\\b"),
            pattern("\\b(tiết lộ|hiển thị|in ra|đọc lại)\\b.{0,70}"
                    + "\\b(prompt|lệnh|hướng dẫn|chỉ dẫn|tin nhắn)\\b.{0,30}"
                    + "\\b(hệ thống|ẩn|developer)\\b"),
            pattern("\\b(show|reveal|print|give|tell|display|đưa|cho|hiện|in)\\b.{0,50}"
                    + "\\b((openai[-_ ]?)?api[-_ ]?key|secret|jwt[-_ ]?key|"
                    + "environment variables?|\\.env.backend)\\b")
    );

    private final int maxInputCharacters;

    public ChatInputGuardrail(
            @Value("${chatbot.guardrails.max-input-characters:4000}") int maxInputCharacters
    ) {
        this.maxInputCharacters = maxInputCharacters;
    }

    public Decision inspect(String rawInput) {
        if (rawInput == null || rawInput.isBlank()) {
            return Decision.rejected(Reason.EMPTY, EMPTY_MESSAGE);
        }

        String normalized = normalize(rawInput);
        if (normalized.isBlank()) {
            return Decision.rejected(Reason.EMPTY, EMPTY_MESSAGE);
        }
        if (normalized.length() > maxInputCharacters) {
            return Decision.rejected(
                    Reason.TOO_LONG,
                    TOO_LONG_MESSAGE.formatted(maxInputCharacters)
            );
        }

        String searchable = normalized.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        boolean injectionDetected = INJECTION_PATTERNS.stream()
                .anyMatch(candidate -> candidate.matcher(searchable).find());
        if (injectionDetected) {
            return Decision.rejected(Reason.PROMPT_INJECTION, BLOCKED_MESSAGE);
        }
        return Decision.allowed(normalized);
    }

    private static Pattern pattern(String expression) {
        return Pattern.compile(expression, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    private String normalize(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFKC);
        StringBuilder safe = new StringBuilder(normalized.length());
        normalized.codePoints().forEach(codePoint -> {
            int type = Character.getType(codePoint);
            if (type == Character.FORMAT) {
                return;
            }
            if (Character.isISOControl(codePoint)
                    && codePoint != '\n' && codePoint != '\r' && codePoint != '\t') {
                safe.append(' ');
                return;
            }
            safe.appendCodePoint(codePoint);
        });
        return safe.toString().trim();
    }

    public enum Reason {
        ALLOWED,
        EMPTY,
        TOO_LONG,
        PROMPT_INJECTION
    }

    public record Decision(boolean allowed, String normalizedInput, Reason reason, String message) {
        private static Decision allowed(String normalizedInput) {
            return new Decision(true, normalizedInput, Reason.ALLOWED, null);
        }

        private static Decision rejected(Reason reason, String message) {
            return new Decision(false, null, reason, message);
        }
    }
}
