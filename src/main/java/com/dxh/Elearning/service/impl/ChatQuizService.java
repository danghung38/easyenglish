package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.chat.ChatAssistantResult;
import com.dxh.Elearning.chat.ChatQuizEligibilityPolicy;
import com.dxh.Elearning.entity.User;
import com.dxh.Elearning.entity.UserVocabularyQuiz;
import com.dxh.Elearning.repo.UserRepository;
import com.dxh.Elearning.repo.UserVocabularyQuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatQuizService {
    private final ChatQuizEligibilityPolicy eligibilityPolicy;
    private final UserVocabularyQuizRepository quizRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean saveIfEligible(ChatAssistantResult result, String sourceMessage) {
        if (!eligibilityPolicy.isEligible(result)) {
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            log.debug("Skipping chatbot quiz because no authenticated user is available");
            return false;
        }

        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) {
            log.warn("Skipping chatbot quiz because user '{}' was not found", authentication.getName());
            return false;
        }

        ChatAssistantResult.LearningCandidate candidate = result.getLearningCandidate();
        ChatAssistantResult.QuizCandidate quiz = result.getQuiz();
        String normalizedTerm = normalize(candidate.getTerm());
        UserVocabularyQuiz entity = quizRepository
                .findByUserIdAndNormalizedTerm(user.getId(), normalizedTerm)
                .orElseGet(UserVocabularyQuiz::new);

        entity.setUser(user);
        entity.setTerm(candidate.getTerm().trim());
        entity.setNormalizedTerm(normalizedTerm);
        entity.setMeaning(candidate.getMeaning().trim());
        entity.setPronunciation(trimToNull(candidate.getPronunciation()));
        entity.setPartOfSpeech(trimToNull(candidate.getPartOfSpeech()));
        entity.setExample(trimToNull(candidate.getExample()));
        entity.setQuizQuestion(quiz.getQuestion().trim());
        List<String> options = quiz.getOptions();
        entity.setOptionOne(options.get(0).trim());
        entity.setOptionTwo(options.get(1).trim());
        entity.setOptionThree(options.get(2).trim());
        entity.setOptionFour(options.get(3).trim());
        entity.setCorrectAnswer(quiz.getCorrectAnswer().trim());
        entity.setExplanation(trimToNull(quiz.getExplanation()));
        entity.setSourceMessage(trimToNull(sourceMessage));
        entity.setConfidence(result.getConfidence());
        quizRepository.save(entity);
        return true;
    }

    static String normalize(String term) {
        return Normalizer.normalize(term.trim(), Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
