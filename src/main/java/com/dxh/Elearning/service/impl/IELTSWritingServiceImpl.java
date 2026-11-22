package com.dxh.Elearning.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.dxh.Elearning.dto.response.IELTSWritingResponse;
import com.dxh.Elearning.entity.Question;
import com.dxh.Elearning.entity.UserAnswer;
import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import com.dxh.Elearning.repo.QuestionRepository;
import com.dxh.Elearning.repo.UserAnswerRepository;
import com.dxh.Elearning.service.interfac.IELTSWritingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of IELTS Writing evaluation using IELTSensei service
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IELTSWritingServiceImpl implements IELTSWritingService {

    private static final int MIN_EVALUATABLE_WORDS = 20;

    private final RestTemplate restTemplate = new RestTemplate();
    private final QuestionRepository questionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
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
        int actualWordCount = countWords(essayText);
        if (actualWordCount < MIN_EVALUATABLE_WORDS) {
            throw new AppException(ErrorCode.WRITING_RESPONSE_TOO_SHORT);
        }

        try {
            log.info("Calling IELTSensei Writing evaluation for task: {}", task);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Prepare request body matching IELTSensei format
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("task", task);
            requestBody.put("text", essayText);
            requestBody.put("word_count", actualWordCount);
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

    private int countWords(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

    @Override
    @Transactional
    public Map<String, Object> requestFeedback(Long userExamPartId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        List<UserAnswer> answers = userAnswerRepository
                .findAllByUserExamPart_IdAndUserExamPart_UserExam_User_Username(
                        userExamPartId,
                        authentication.getName()
                )
                .stream()
                .filter(answer -> answer.getQuestion() != null)
                .filter(answer -> answer.getQuestion().getSkillType()
                        == com.dxh.Elearning.enums.SkillType.WRITING)
                .filter(answer -> answer.getAnswerText() != null
                        && !answer.getAnswerText().isBlank())
                .toList();
        if (answers.isEmpty()) {
            // Do not reveal whether another user's exam part exists.
            throw new AppException(ErrorCode.USER_EXAM_PART_NOT_FOUND);
        }

        List<Map<String, Object>> submissions = new ArrayList<>();
        Map<String, Object> taskScores = new LinkedHashMap<>();
        for (UserAnswer answer : answers) {
            String task = answer.getQuestion().getSection();
            String normalizedTask = "task1".equalsIgnoreCase(task) ? "task1" : "task2";
            Map<String, Object> storedScore = parseStoredFeedback(answer.getAiFeedback());
            Map<String, Object> submission = new LinkedHashMap<>();
            submission.put("task", normalizedTask);
            submission.put("question", answer.getQuestion().getContent());
            submission.put("text", answer.getAnswerText());
            submission.put("score", storedScore);
            submissions.add(submission);
            taskScores.put(normalizedTask, storedScore);
        }

        Map<String, Object> score = new LinkedHashMap<>();
        score.put("overall_band", answers.get(0).getUserExamPart().getScore());
        score.put("tasks", taskScores);
        Map<String, Object> requestBody = Map.of(
                "submissions", submissions,
                "score", score
        );

        final Map<String, Object> feedback;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<Map> response = restTemplate.exchange(
                    ieltsSenseiUrl + "/api/writing/feedback",
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    Map.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new AppException(ErrorCode.EVALUATION_FAILED);
            }
            feedback = objectMapper.convertValue(
                    response.getBody(),
                    new TypeReference<Map<String, Object>>() { }
            );
        } catch (RestClientException exception) {
            log.error("Writing feedback request failed for exam part {}", userExamPartId, exception);
            throw new AppException(ErrorCode.EVALUATION_FAILED);
        }

        for (UserAnswer answer : answers) {
            answer.setAiFeedback(mergeFeedback(answer, feedback));
        }
        userAnswerRepository.saveAll(answers);
        return feedback;
    }

    private Map<String, Object> parseStoredFeedback(String value) {
        if (value == null || value.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<Map<String, Object>>() { });
        } catch (Exception exception) {
            log.warn("Ignoring malformed stored Writing feedback");
            return new LinkedHashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    private String mergeFeedback(UserAnswer answer, Map<String, Object> feedback) {
        try {
            ObjectNode stored = objectMapper.valueToTree(parseStoredFeedback(answer.getAiFeedback()));
            Map<String, Object> criteria = feedback.get("criterion_feedback") instanceof Map
                    ? (Map<String, Object>) feedback.get("criterion_feedback")
                    : Map.of();
            boolean taskOne = "task1".equalsIgnoreCase(answer.getQuestion().getSection());
            ObjectNode details = objectMapper.createObjectNode();
            details.put(
                    "taskAchievement",
                    text(criteria.get(taskOne ? "task1_task_achievement" : "task2_task_response"))
            );
            details.put("coherenceCohesion", text(criteria.get("coherence_cohesion")));
            details.put("lexicalResource", text(criteria.get("lexical_resource")));
            details.put("grammaticalRange", text(criteria.get("grammatical_range_accuracy")));
            stored.set("detailedFeedback", details);
            stored.set("strengths", objectMapper.valueToTree(feedback.get("strengths")));
            stored.set(
                    "areasForImprovement",
                    objectMapper.valueToTree(feedback.get("priority_improvements"))
            );
            stored.set("nextActions", objectMapper.valueToTree(feedback.get("next_actions")));
            stored.put("examinerFeedback", "Detailed feedback generated on request.");
            stored.put("feedbackStatus", "COMPLETED");
            return objectMapper.writeValueAsString(stored);
        } catch (Exception exception) {
            log.error("Failed to merge Writing feedback for answer {}", answer.getId(), exception);
            throw new AppException(ErrorCode.AI_RESPONSE_PARSE_ERROR);
        }
    }

    private String text(Object value) {
        return value != null ? String.valueOf(value) : "";
    }
    
    /**
     * Flatten nested response structure for easier frontend consumption
     * Extracts criteria scores and detailed feedback to top level
     */
    private IELTSWritingResponse flattenResponse(IELTSWritingResponse response) {
        IELTSWritingResponse flattened = new IELTSWritingResponse();
        flattened.setStatus(response.getStatus());
        flattened.setOverallBand(response.getOverallBand());
        flattened.setExaminerFeedback(response.getExaminerFeedback());
        flattened.setStrengths(response.getStrengths());
        flattened.setAreasForImprovement(response.getAreasForImprovement());
        
        // Keep nested structure
        flattened.setCriteriaScores(response.getCriteriaScores());
        flattened.setDetailedFeedback(response.getDetailedFeedback());
        flattened.setConfidence(response.getConfidence());
        flattened.setEvidence(response.getEvidence());
        flattened.setReview(response.getReview());
        flattened.setRetrievalDocumentIds(response.getRetrievalDocumentIds());
        flattened.setRetrievalIndexVersion(response.getRetrievalIndexVersion());
        flattened.setRetrievalAudit(response.getRetrievalAudit());
        
        return flattened;
    }
}
