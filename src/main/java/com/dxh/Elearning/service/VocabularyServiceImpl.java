package com.dxh.Elearning.service;

import com.dxh.Elearning.dto.request.VocabularyRequest;
import com.dxh.Elearning.dto.response.VocabularyResponse;
import com.dxh.Elearning.dto.response.VocabularyTopicResponse;
import com.dxh.Elearning.entity.Vocabulary;
import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import com.dxh.Elearning.repo.VocabularyRepository;
import com.dxh.Elearning.service.interfac.VocabularyService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VocabularyServiceImpl implements VocabularyService {
    
    VocabularyRepository vocabularyRepository;
    
    // Định nghĩa gradient và icon color cho từng chủ đề
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
        List<Vocabulary> allVocabs = vocabularyRepository.findAll();
        
        // Group by topic and count words
        return allVocabs.stream()
                .collect(Collectors.groupingBy(Vocabulary::getTopic))
                .entrySet()
                .stream()
                .map(entry -> {
                    String topicName = entry.getKey();
                    int wordCount = entry.getValue().size();
                    String[] styles = TOPIC_STYLES.getOrDefault(topicName, 
                            new String[]{"linear-gradient(135deg, #667eea 0%, #764ba2 100%)", "#fff"});
                    
                    return VocabularyTopicResponse.builder()
                            .title(topicName)
                            .wordCount(wordCount)
                            .gradient(styles[0])
                            .iconColor(styles[1])
                            .build();
                })
                .collect(Collectors.toList());
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
        
        vocabulary = vocabularyRepository.save(vocabulary);
        return mapToVocabularyResponse(vocabulary);
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
        
        vocabulary = vocabularyRepository.save(vocabulary);
        return mapToVocabularyResponse(vocabulary);
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
        return vocabularyRepository.findByTopic(topic).stream()
                .map(this::mapToVocabularyResponse)
                .collect(Collectors.toList());
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
                .build();
    }
}
