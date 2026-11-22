package com.dxh.Elearning.chat;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatQuizEligibilityPolicyTest {
    private final ChatQuizEligibilityPolicy policy = new ChatQuizEligibilityPolicy();

    @Test
    void acceptsConfidentWordAndPhraseLookups() {
        assertTrue(policy.isEligible(validResult("WORD_LOOKUP", "resilient", 0.95)));
        assertTrue(policy.isEligible(validResult("PHRASE_LOOKUP", "look forward to", 0.91)));
        assertTrue(policy.isEligible(validResult("EXPLICIT_SAVE_REQUEST", "carry on", 0.88)));
    }

    @Test
    void rejectsTranslationGrammarGreetingAndGeneralChat() {
        assertFalse(policy.isEligible(validResult("PASSAGE_TRANSLATION", "resilient", 0.99)));
        assertFalse(policy.isEligible(validResult("GRAMMAR_EXPLANATION", "present perfect", 0.99)));
        assertFalse(policy.isEligible(validResult("GREETING", "hello", 0.99)));
        assertFalse(policy.isEligible(validResult("GENERAL_CHAT", "football", 0.99)));
    }

    @Test
    void rejectsLowConfidenceLongTermsAndSentenceLikeTerms() {
        assertFalse(policy.isEligible(validResult("WORD_LOOKUP", "resilient", 0.79)));
        assertFalse(policy.isEligible(validResult(
                "PHRASE_LOOKUP", "this phrase contains more than five words", 0.99
        )));
        assertFalse(policy.isEligible(validResult("WORD_LOOKUP", "What is this?", 0.99)));
    }

    @Test
    void rejectsMalformedQuizOptions() {
        ChatAssistantResult missingAnswer = validResult("WORD_LOOKUP", "resilient", 0.95);
        missingAnswer.getQuiz().setCorrectAnswer("not in options");
        assertFalse(policy.isEligible(missingAnswer));

        ChatAssistantResult duplicateOptions = validResult("WORD_LOOKUP", "resilient", 0.95);
        duplicateOptions.getQuiz().setOptions(List.of("strong", "strong", "weak", "quick"));
        assertFalse(policy.isEligible(duplicateOptions));
    }

    static ChatAssistantResult validResult(String intent, String term, double confidence) {
        ChatAssistantResult.LearningCandidate learning = new ChatAssistantResult.LearningCandidate();
        learning.setTerm(term);
        learning.setMeaning("có khả năng phục hồi");
        learning.setPronunciation("/rɪˈzɪliənt/");
        learning.setPartOfSpeech("adjective");
        learning.setExample("The community remained resilient.");

        ChatAssistantResult.QuizCandidate quiz = new ChatAssistantResult.QuizCandidate();
        quiz.setQuestion("Which word means able to recover quickly?");
        quiz.setOptions(List.of("fragile", "resilient", "temporary", "ordinary"));
        quiz.setCorrectAnswer("resilient");
        quiz.setExplanation("Resilient means able to recover after difficulty.");

        ChatAssistantResult result = new ChatAssistantResult();
        result.setAnswer("Resilient means able to recover quickly.");
        result.setIntent(intent);
        result.setConfidence(confidence);
        result.setLearningCandidate(learning);
        result.setQuiz(quiz);
        return result;
    }
}
