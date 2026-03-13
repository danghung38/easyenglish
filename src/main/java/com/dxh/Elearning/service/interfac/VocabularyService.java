package com.dxh.Elearning.service.interfac;

import com.dxh.Elearning.dto.request.VocabularyRequest;
import com.dxh.Elearning.dto.response.VocabularyResponse;
import com.dxh.Elearning.dto.response.VocabularyTopicResponse;

import java.util.List;

public interface VocabularyService {
    
    // Topic methods
    List<VocabularyTopicResponse> getAllTopics();
    
    // Vocabulary methods
    VocabularyResponse createVocabulary(VocabularyRequest request);
    VocabularyResponse updateVocabulary(Long id, VocabularyRequest request);
    void deleteVocabulary(Long id);
    VocabularyResponse getVocabularyById(Long id);
    List<VocabularyResponse> getVocabulariesByTopic(String topic);
}
