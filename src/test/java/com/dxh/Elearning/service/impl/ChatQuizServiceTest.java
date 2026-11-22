package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.chat.ChatAssistantResult;
import com.dxh.Elearning.chat.ChatQuizEligibilityPolicy;
import com.dxh.Elearning.entity.User;
import com.dxh.Elearning.entity.UserVocabularyQuiz;
import com.dxh.Elearning.repo.UserRepository;
import com.dxh.Elearning.repo.UserVocabularyQuizRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatQuizServiceTest {
    @Mock
    private UserVocabularyQuizRepository quizRepository;
    @Mock
    private UserRepository userRepository;

    private ChatQuizService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new ChatQuizService(
                new ChatQuizEligibilityPolicy(), quizRepository, userRepository
        );
        user = User.builder().username("learner").email("learner@example.com").build();
        user.setId(7L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("learner", "token", List.of())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsQuizForEligibleLookup() {
        when(userRepository.findByUsername("learner")).thenReturn(Optional.of(user));
        when(quizRepository.findByUserIdAndNormalizedTerm(7L, "resilient"))
                .thenReturn(Optional.empty());
        when(quizRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        boolean saved = service.saveIfEligible(validResult(), "resilient nghĩa là gì?");

        assertTrue(saved);
        ArgumentCaptor<UserVocabularyQuiz> captor = ArgumentCaptor.forClass(UserVocabularyQuiz.class);
        verify(quizRepository).save(captor.capture());
        assertEquals("resilient", captor.getValue().getNormalizedTerm());
        assertEquals("resilient", captor.getValue().getCorrectAnswer());
        assertSame(user, captor.getValue().getUser());
    }

    @Test
    void updatesExistingQuizInsteadOfCreatingDuplicate() {
        when(userRepository.findByUsername("learner")).thenReturn(Optional.of(user));
        UserVocabularyQuiz existing = new UserVocabularyQuiz();
        existing.setId(11L);
        when(quizRepository.findByUserIdAndNormalizedTerm(7L, "resilient"))
                .thenReturn(Optional.of(existing));

        assertTrue(service.saveIfEligible(validResult(), "Explain resilient"));

        verify(quizRepository).save(existing);
        assertEquals(11L, existing.getId());
        assertEquals("Resilient", existing.getTerm());
        assertEquals("resilient", existing.getNormalizedTerm());
    }

    @Test
    void doesNotTouchDatabaseForIneligibleIntent() {
        ChatAssistantResult result = validResult();
        result.setIntent("PASSAGE_TRANSLATION");

        assertFalse(service.saveIfEligible(result, "Translate this paragraph"));
        verify(quizRepository, never()).save(any());
        verify(userRepository, never()).findByUsername(any());
    }

    private ChatAssistantResult validResult() {
        ChatAssistantResult.LearningCandidate learning = new ChatAssistantResult.LearningCandidate();
        learning.setTerm(" Resilient ");
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
        result.setIntent("WORD_LOOKUP");
        result.setConfidence(0.95);
        result.setLearningCandidate(learning);
        result.setQuiz(quiz);
        return result;
    }
}
