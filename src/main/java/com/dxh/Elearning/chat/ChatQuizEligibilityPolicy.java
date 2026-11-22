package com.dxh.Elearning.chat;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class ChatQuizEligibilityPolicy {
    public static final double MIN_CONFIDENCE = 0.80;
    private static final Set<String> ELIGIBLE_INTENTS = Set.of(
            "WORD_LOOKUP",
            "PHRASE_LOOKUP",
            "EXPLICIT_SAVE_REQUEST"
    );

    public boolean isEligible(ChatAssistantResult result) {
        if (result == null || result.getIntent() == null || result.getConfidence() == null) {
            return false;
        }
        if (!ELIGIBLE_INTENTS.contains(result.getIntent().trim().toUpperCase(Locale.ROOT))) {
            return false;
        }
        if (result.getConfidence() < MIN_CONFIDENCE) {
            return false;
        }
        return validLearningCandidate(result.getLearningCandidate())
                && validQuiz(result.getQuiz());
    }

    private boolean validLearningCandidate(ChatAssistantResult.LearningCandidate candidate) {
        if (candidate == null || blank(candidate.getTerm()) || blank(candidate.getMeaning())) {
            return false;
        }
        String term = candidate.getTerm().trim();
        int wordCount = term.split("\\s+").length;
        return term.length() <= 80
                && wordCount <= 5
                && !term.contains("\n")
                && !term.matches(".*[.!?].*");
    }

    private boolean validQuiz(ChatAssistantResult.QuizCandidate quiz) {
        if (quiz == null || blank(quiz.getQuestion()) || blank(quiz.getCorrectAnswer())) {
            return false;
        }
        List<String> options = quiz.getOptions();
        if (options == null || options.size() != 4 || options.stream().anyMatch(this::blank)) {
            return false;
        }
        Set<String> uniqueOptions = new HashSet<>();
        boolean containsAnswer = false;
        for (String option : options) {
            String normalized = option.trim().toLowerCase(Locale.ROOT);
            uniqueOptions.add(normalized);
            if (option.trim().equalsIgnoreCase(quiz.getCorrectAnswer().trim())) {
                containsAnswer = true;
            }
        }
        return uniqueOptions.size() == 4 && containsAnswer;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
