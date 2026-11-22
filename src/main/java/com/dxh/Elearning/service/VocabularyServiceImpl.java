package com.dxh.Elearning.service;

import com.dxh.Elearning.dto.request.VocabularyRequest;
import com.dxh.Elearning.dto.response.VocabularyResponse;
import com.dxh.Elearning.dto.response.VocabularyTopicResponse;
import com.dxh.Elearning.entity.User;
import com.dxh.Elearning.entity.UserVocabularyQuiz;
import com.dxh.Elearning.entity.Vocabulary;
import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import com.dxh.Elearning.repo.UserRepository;
import com.dxh.Elearning.repo.UserVocabularyQuizRepository;
import com.dxh.Elearning.repo.VocabularyRepository;
import com.dxh.Elearning.service.interfac.VocabularyService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VocabularyServiceImpl implements VocabularyService {
    public static final String CHATBOT_QUIZ_TOPIC = "My Chatbot Quiz";
    private static final String TYPE_FLASHCARD = "FLASHCARD";
    private static final String TYPE_CHATBOT_QUIZ = "CHATBOT_QUIZ";

    VocabularyRepository vocabularyRepository;
    UserVocabularyQuizRepository userVocabularyQuizRepository;
    UserRepository userRepository;

    private static final Map<String, String[]> TOPIC_STYLES = new HashMap<>();

    static {
        TOPIC_STYLES.put("Environment", new String[]{"linear-gradient(135deg, #667eea 0%, #764ba2 100%)", "#fff"});
        TOPIC_STYLES.put("Technology", new String[]{"linear-gradient(135deg, #f093fb 0%, #f5576c 100%)", "#fff"});
        TOPIC_STYLES.put("Education", new String[]{"linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)", "#fff"});
        TOPIC_STYLES.put("Health", new String[]{"linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)", "#fff"});
        TOPIC_STYLES.put("Business", new String[]{"linear-gradient(135deg, #fa709a 0%, #fee140 100%)", "#fff"});
    }

    @Override
    public List<VocabularyTopicResponse> getAllTopics() {
        List<VocabularyTopicResponse> topics = new ArrayList<>(vocabularyRepository.findAll().stream()
                .collect(Collectors.groupingBy(Vocabulary::getTopic))
                .entrySet()
                .stream()
                .map(entry -> mapTopic(entry.getKey(), entry.getValue().size()))
                .sorted(Comparator.comparing(VocabularyTopicResponse::getTitle))
                .toList());

        currentUser().ifPresent(user -> {
            long quizCount = userVocabularyQuizRepository.countByUserId(user.getId());
            if (quizCount > 0) {
                topics.add(0, VocabularyTopicResponse.builder()
                        .title(CHATBOT_QUIZ_TOPIC)
                        .wordCount(Math.toIntExact(quizCount))
                        .gradient("linear-gradient(135deg, #7F7FD5 0%, #86A8E7 50%, #91EAE4 100%)")
                        .iconColor("#fff")
                        .type(TYPE_CHATBOT_QUIZ)
                        .build());
            }
        });
        return topics;
    }

    @Override
    @Transactional
    public VocabularyResponse createVocabulary(VocabularyRequest request) {
        Vocabulary vocabulary = Vocabulary.builder()
                .word(request.getWord())
                .meaning(request.getMeaning())
                .pronunciation(request.getPronunciation())
                .pronunciationAudioUrl(request.getPronunciationAudioUrl())
                .example(request.getExample())
                .imageUrl(request.getImageUrl())
                .topic(request.getTopic())
                .build();
        return mapToVocabularyResponse(vocabularyRepository.save(vocabulary));
    }

    @Override
    @Transactional
    public VocabularyResponse updateVocabulary(Long id, VocabularyRequest request) {
        Vocabulary vocabulary = vocabularyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        vocabulary.setWord(request.getWord());
        vocabulary.setMeaning(request.getMeaning());
        vocabulary.setPronunciation(request.getPronunciation());
        vocabulary.setPronunciationAudioUrl(request.getPronunciationAudioUrl());
        vocabulary.setExample(request.getExample());
        vocabulary.setImageUrl(request.getImageUrl());
        vocabulary.setTopic(request.getTopic());
        return mapToVocabularyResponse(vocabularyRepository.save(vocabulary));
    }

    @Override
    @Transactional
    public void deleteVocabulary(Long id) {
        if (!vocabularyRepository.existsById(id)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED);
        }
        vocabularyRepository.deleteById(id);
    }

    @Override
    public VocabularyResponse getVocabularyById(Long id) {
        Vocabulary vocabulary = vocabularyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        return mapToVocabularyResponse(vocabulary);
    }

    @Override
    public List<VocabularyResponse> getVocabulariesByTopic(String topic) {
        if (CHATBOT_QUIZ_TOPIC.equalsIgnoreCase(topic)) {
            return currentUser()
                    .map(user -> userVocabularyQuizRepository.findByUserIdOrderByUpdatedAtDesc(user.getId())
                            .stream()
                            .map(this::mapToQuizResponse)
                            .toList())
                    .orElseGet(List::of);
        }
        return vocabularyRepository.findByTopic(topic).stream()
                .map(this::mapToVocabularyResponse)
                .toList();
    }

    private VocabularyTopicResponse mapTopic(String name, int wordCount) {
        String[] styles = TOPIC_STYLES.getOrDefault(
                name,
                new String[]{"linear-gradient(135deg, #667eea 0%, #764ba2 100%)", "#fff"}
        );
        return VocabularyTopicResponse.builder()
                .title(name)
                .wordCount(wordCount)
                .gradient(styles[0])
                .iconColor(styles[1])
                .type(TYPE_FLASHCARD)
                .build();
    }

    private VocabularyResponse mapToVocabularyResponse(Vocabulary vocabulary) {
        return VocabularyResponse.builder()
                .id(vocabulary.getId())
                .word(vocabulary.getWord())
                .meaning(vocabulary.getMeaning())
                .pronunciation(vocabulary.getPronunciation())
                .pronunciationAudioUrl(vocabulary.getPronunciationAudioUrl())
                .example(vocabulary.getExample())
                .imageUrl(vocabulary.getImageUrl())
                .topic(vocabulary.getTopic())
                .chatbotQuiz(false)
                .build();
    }

    private VocabularyResponse mapToQuizResponse(UserVocabularyQuiz quiz) {
        return VocabularyResponse.builder()
                .id(quiz.getId())
                .word(quiz.getTerm())
                .meaning(quiz.getMeaning())
                .pronunciation(quiz.getPronunciation())
                .example(quiz.getExample())
                .topic(CHATBOT_QUIZ_TOPIC)
                .chatbotQuiz(true)
                .quizQuestion(quiz.getQuizQuestion())
                .quizOptions(List.of(
                        quiz.getOptionOne(), quiz.getOptionTwo(),
                        quiz.getOptionThree(), quiz.getOptionFour()
                ))
                .correctAnswer(quiz.getCorrectAnswer())
                .explanation(quiz.getExplanation())
                .build();
    }

    private Optional<User> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        return userRepository.findByUsername(authentication.getName());
    }
}
