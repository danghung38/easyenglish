package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.dto.response.IELTSWritingResponse;
import com.dxh.Elearning.entity.Question;
import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import com.dxh.Elearning.repo.QuestionRepository;
import com.dxh.Elearning.service.interfac.IELTSWritingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of IELTS Writing evaluation using IELTSensei service
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IELTSWritingServiceImpl implements IELTSWritingService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final QuestionRepository questionRepository;
    
    @Value("${ielts-sensei.service.url:http://localhost:5000}")
    private String ieltsSenseiUrl;

    @Override
    public IELTSWritingResponse evaluateWritingByQuestion(String essayText, Long questionId, Integer wordCount, Integer duration) {
        log.info("Evaluating IELTS Writing for questionId: {}", questionId);
        
        // Get question from database
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
        
        // Validate question type
        if (question.getSkillType() != com.dxh.Elearning.enums.SkillType.WRITING) {
            log.error("Question {} has invalid skillType: {}. Expected: WRITING", 
                questionId, question.getSkillType());
            throw new AppException(ErrorCode.INVALID_QUESTION_TYPE);
        }
        
        String questionText = question.getContent();
        if (questionText == null || questionText.trim().isEmpty()) {
            log.error("Question {} has empty content", questionId);
            throw new AppException(ErrorCode.INVALID_QUESTION_CONTENT);
        }
        
        // Get task type from Question.section (task1, task2) - default to task2
        String task = question.getSection();
        if (task == null || task.trim().isEmpty() || !task.matches("task[12]")) {
            log.warn("Question {} has invalid/missing section '{}', defaulting to task2", questionId, task);
            task = "task2";
        }
        
        log.info("Question content: '{}', Task: {}", questionText, task);
        
        return evaluateWriting(essayText, questionText, task, wordCount, duration);
    }

    @Override
    public IELTSWritingResponse evaluateWriting(String essayText, String questionText, String task, Integer wordCount, Integer duration) {
        try {
            log.info("Calling IELTSensei Writing evaluation for task: {}", task);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Prepare request body matching IELTSensei format
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("task", task);
            requestBody.put("text", essayText);
            requestBody.put("word_count", wordCount != null ? wordCount : essayText.split("\\s+").length);
            requestBody.put("duration", duration != null ? duration : 0);
            requestBody.put("question", questionText);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            String evaluationUrl = ieltsSenseiUrl + "/api/writing/evaluate";
            log.info("Calling IELTSensei at: {}", evaluationUrl);
            
            ResponseEntity<IELTSWritingResponse> response = restTemplate.exchange(
                    evaluationUrl,
                    HttpMethod.POST,
                    requestEntity,
                    IELTSWritingResponse.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                IELTSWritingResponse result = response.getBody();
                log.info("IELTS Writing evaluation completed. Band: {}", result.getOverallBand());
                
                // Flatten the response for easier frontend consumption
                return flattenResponse(result);
            }
            
            throw new AppException(ErrorCode.EVALUATION_FAILED);
            
        } catch (Exception e) {
            log.error("Error during IELTS Writing evaluation", e);
            throw new AppException(ErrorCode.EVALUATION_FAILED);
        }
    }
    
    /**
     * Flatten nested response structure for easier frontend consumption
     * Extracts criteria scores and detailed feedback to top level
     */
    private IELTSWritingResponse flattenResponse(IELTSWritingResponse response) {
        IELTSWritingResponse flattened = new IELTSWritingResponse();
        flattened.setOverallBand(response.getOverallBand());
        flattened.setExaminerFeedback(response.getExaminerFeedback());
        flattened.setStrengths(response.getStrengths());
        flattened.setAreasForImprovement(response.getAreasForImprovement());
        
        // Keep nested structure
        flattened.setCriteriaScores(response.getCriteriaScores());
        flattened.setDetailedFeedback(response.getDetailedFeedback());
        
        return flattened;
    }
}
