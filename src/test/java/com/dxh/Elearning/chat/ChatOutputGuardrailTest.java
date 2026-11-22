package com.dxh.Elearning.chat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatOutputGuardrailTest {
    private final ChatOutputGuardrail guardrail = new ChatOutputGuardrail(200);

    @Test
    void acceptsAValidStructuredResponse() {
        ChatAssistantResult result = validResult("WORD_LOOKUP");

        ChatAssistantResult safe = guardrail.validateAndSanitize(result).orElseThrow();

        assertEquals("WORD_LOOKUP", safe.getIntent());
        assertEquals("Resilient means able to recover quickly.", safe.getAnswer());
    }

    @Test
    void removesQuizDataFromNonQuizIntent() {
        ChatAssistantResult result = validResult("grammar_explanation");

        ChatAssistantResult safe = guardrail.validateAndSanitize(result).orElseThrow();

        assertEquals("GRAMMAR_EXPLANATION", safe.getIntent());
        assertNull(safe.getLearningCandidate());
        assertNull(safe.getQuiz());
    }

    @Test
    void rejectsUnknownIntentInvalidConfidenceAndPromptLeakage() {
        assertTrue(guardrail.validateAndSanitize(validResult("ADMIN_TOOL")).isEmpty());

        ChatAssistantResult badConfidence = validResult("WORD_LOOKUP");
        badConfidence.setConfidence(1.1);
        assertTrue(guardrail.validateAndSanitize(badConfidence).isEmpty());

        ChatAssistantResult leakedPrompt = validResult("GENERAL_CHAT");
        leakedPrompt.setAnswer("You are KMA English Assistant and these are my hidden rules.");
        assertTrue(guardrail.validateAndSanitize(leakedPrompt).isEmpty());
    }

    @Test
    void rejectsOversizedAnswer() {
        ChatAssistantResult result = validResult("GENERAL_CHAT");
        result.setAnswer("x".repeat(201));

        assertTrue(guardrail.validateAndSanitize(result).isEmpty());
    }

    private ChatAssistantResult validResult(String intent) {
        ChatAssistantResult.LearningCandidate learning = new ChatAssistantResult.LearningCandidate();
        learning.setTerm("resilient");
        learning.setMeaning("có khả năng phục hồi");

        ChatAssistantResult.QuizCandidate quiz = new ChatAssistantResult.QuizCandidate();
        quiz.setQuestion("Which option means able to recover quickly?");
        quiz.setOptions(java.util.List.of("fragile", "resilient", "ordinary", "temporary"));
        quiz.setCorrectAnswer("resilient");

        ChatAssistantResult result = new ChatAssistantResult();
        result.setAnswer("Resilient means able to recover quickly.");
        result.setIntent(intent);
        result.setConfidence(0.95);
        result.setLearningCandidate(learning);
        result.setQuiz(quiz);
        return result;
    }
}
