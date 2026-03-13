package com.dxh.Elearning.service.interfac;


import com.dxh.Elearning.dto.request.*;
import com.dxh.Elearning.dto.response.ExamResponse;
import com.dxh.Elearning.dto.response.PageResponse;
import com.dxh.Elearning.dto.response.QuestionResponse;

import com.dxh.Elearning.enums.SkillType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface QuestionService {

    QuestionResponse createQuestionReading(QuestionRequest req);
    QuestionResponse createQuestionListening(QuestionRequest req, MultipartFile audioFile);
    QuestionResponse createQuestionListening(QuestionRequest req); // New: without audio file
    QuestionResponse createQuestionWriting(QuestionRequest req, MultipartFile imageFile);
    QuestionResponse createQuestionSpeaking(QuestionRequest req);

    PageResponse<List<QuestionResponse>> getQuestionsByExamPart(Long examPartId, int pageNo, int pageSize, String sortBy);
    
    PageResponse<List<QuestionResponse>> getQuestionsByExamIdAndSkillType(Long examId, SkillType skillType, int pageNo, int pageSize, String sortBy);

    QuestionResponse updateQuestionReading(Long id, UpdateQuestionReadingRequest req);

    QuestionResponse updateQuestionListening(Long id, UpdateQuestionListeningRequest req, MultipartFile audioFile);

    QuestionResponse updateQuestionSpeaking(Long id, UpdateQuestionSpeakingRequest req);

    QuestionResponse updateQuestionWriting(Long id, UpdateQuestionWritingRequest req, MultipartFile imageFile);
    
    // IELTS Listening Section API
    List<QuestionResponse> createListeningSection(SectionRequest req, MultipartFile audioFile);
    List<QuestionResponse> getQuestionsBySection(Long examPartId, String sectionName);
    Object getAllListeningSections(Long examPartId);
    
    // Add list of questions to a section with one audio file
    List<QuestionResponse> addQuestionsToSection(AddQuestionsToSectionRequest req, MultipartFile audioFile);
    
    // Upload audio for existing section (questions already created with section field)
    List<QuestionResponse> uploadAudioForSection(Long examPartId, String sectionName, MultipartFile audioFile, String transcript);
    
    // Upload image for existing reading section (questions already created with section field)
    List<QuestionResponse> uploadImageForSection(Long examPartId, String sectionName, MultipartFile imageFile);
    
    // Delete question
    void deleteQuestion(Long questionId);
    
    // TOEIC Reading Passage API - Create passage with image and questions in one call
    List<QuestionResponse> createReadingPassage(CreateReadingPassageRequest req, MultipartFile imageFile);
    
    // Batch create reading questions for a section
    List<QuestionResponse> createReadingQuestionsBatch(Long examPartId, String sectionName, List<ReadingQuestionRequest> questions);
    
    // Batch create listening questions for a section
    List<QuestionResponse> createListeningQuestionsBatch(Long examPartId, String sectionName, List<ReadingQuestionRequest> questions);
}
