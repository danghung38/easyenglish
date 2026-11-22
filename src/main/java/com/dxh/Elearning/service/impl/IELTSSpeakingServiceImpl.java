package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.dto.response.IELTSSpeakingResponse;
import com.dxh.Elearning.entity.Question;
import com.dxh.Elearning.entity.UserAnswer;
import com.dxh.Elearning.entity.UserExamPart;
import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import com.dxh.Elearning.repo.QuestionRepository;
import com.dxh.Elearning.repo.UserAnswerRepository;
import com.dxh.Elearning.repo.UserExamPartRepository;
import com.dxh.Elearning.service.interfac.IELTSSpeakingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import com.dxh.Elearning.enums.SkillType;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Objects;

/**
 * Implementation of IELTS Speaking evaluation using IELTSensei service only
 * IELTSensei handles both transcription (using OpenAI Whisper API) and evaluation
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IELTSSpeakingServiceImpl implements IELTSSpeakingService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final QuestionRepository questionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final UserExamPartRepository userExamPartRepository;
    private final ObjectMapper objectMapper;
    
    @Value("${ielts-sensei.service.url:http://localhost:5000}")
    private String ieltsSenseiUrl;

    @Override
    @Transactional
    public IELTSSpeakingResponse evaluateSpeakingByQuestion(MultipartFile audioFile, Long questionId, Long userExamPartId, String audioUrl) {
        log.info("Evaluating IELTS Speaking for questionId: {}, userExamPartId: {}", questionId, userExamPartId);
        
        // Get question from database
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
        
        // Get userExamPart
        UserExamPart userExamPart = userExamPartRepository.findById(userExamPartId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_EXAM_PART_NOT_FOUND));
        requireOwner(userExamPart);
        if (!Objects.equals(question.getExamPart().getExam().getId(), userExamPart.getUserExam().getExam().getId())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        // Validate question type
        if (question.getSkillType() != com.dxh.Elearning.enums.SkillType.SPEAKING) {
            log.error("Question {} has invalid skillType: {}. Expected: SPEAKING", 
                questionId, question.getSkillType());
            throw new AppException(ErrorCode.INVALID_QUESTION_TYPE);
        }
        
        String questionText = question.getContent();
        if (questionText == null || questionText.trim().isEmpty()) {
            log.error("Question {} has empty content", questionId);
            throw new AppException(ErrorCode.INVALID_QUESTION_CONTENT);
        }
        
        // Get part from Question.section (part1, part2, part3) - default to part1
        String part = question.getSection();
        if (part == null || part.trim().isEmpty() || !part.matches("part[123]")) {
            log.warn("Question {} has invalid/missing section '{}', defaulting to part1", questionId, part);
            part = "part1";
        }
        
        log.info("Question content: '{}', Part: {}", questionText, part);
        
        // Step 1: Transcribe audio ONCE (avoid calling twice)
        TranscriptionResult transcription = transcribeAudioViaIELTSensei(audioFile, part);
        String transcript = transcription.text();
        log.info("Transcription completed: {}", transcript.substring(0, Math.min(100, transcript.length())));
        
        // Step 2: Evaluate using the transcribed text (no need to re-transcribe)
        IELTSSpeakingResponse response = evaluateSpeakingWithTranscript(
                transcript, questionText, part, transcription.audioFeatures());
        
        // Log authentication context for debugging
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUser = (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
        log.info("💾 Saving UserAnswer - Current authenticated user: {}", currentUser);
        
        // Save to UserAnswer with score (created_by/updated_by will be auto-filled by JPA Auditing)
        UserAnswer userAnswer = UserAnswer.builder()
                .userExamPart(userExamPart)
                .question(question)
                .answerText(transcript)
                .audioUrl(audioUrl)
                .score(response.getOverallBand()) // ✅ Score from AI evaluation
                .aiScore(response.getOverallBand())
                .aiFeedback(buildFeedbackJson(response))
                .build();
        
        UserAnswer savedAnswer = userAnswerRepository.save(userAnswer);
        log.info("✅ UserAnswer saved - ID: {}, questionId: {}, score: {}, created_by: {}, updated_by: {}", 
            savedAnswer.getId(), questionId, savedAnswer.getScore(), 
            savedAnswer.getCreatedBy(), savedAnswer.getUpdateBy());
        
        return response;
    }
    
    /**
     * Build JSON feedback from IELTSSpeakingResponse
     */
    private String buildFeedbackJson(IELTSSpeakingResponse response) {
        try {
            Map<String, Object> feedback = new HashMap<>();
            feedback.put("overall_band", response.getOverallBand());
            feedback.put("criteria_scores", response.getCriteriaScores());
            feedback.put("detailed_feedback", response.getDetailedFeedback());
            feedback.put("examiner_feedback", response.getExaminerFeedback());
            feedback.put("strengths", response.getStrengths());
            feedback.put("areas_for_improvement", response.getAreasForImprovement());
            feedback.put("confidence", response.getConfidence());
            feedback.put("pronunciation_status", response.getPronunciationStatus());
            feedback.put("audio_evidence", response.getAudioEvidence());
            feedback.put("review", response.getReview());
            feedback.put("retrieval_audit", response.getRetrievalAudit());
            
            return objectMapper.writeValueAsString(feedback);
        } catch (Exception e) {
            log.error("Error building feedback JSON", e);
            return response.getExaminerFeedback(); // Fallback to examiner feedback only
        }
    }

    @Override
    public IELTSSpeakingResponse evaluateSpeaking(MultipartFile audioFile, String questionText, String part) {
        try {
            log.info("Calling IELTSensei for transcription and evaluation");
            log.info("Audio file: {}, Part: {}", audioFile.getOriginalFilename(), part);
            
            // Step 1: Transcribe audio using IELTSensei's /api/transcribe
            TranscriptionResult transcription = transcribeAudioViaIELTSensei(audioFile, part);
            String transcribedText = transcription.text();
            log.info("Transcription completed: {}", transcribedText.substring(0, Math.min(100, transcribedText.length())));
            
            // Step 2: Evaluate with the transcribed text
            return evaluateSpeakingWithTranscript(
                    transcribedText, questionText, part, transcription.audioFeatures());
            
        } catch (Exception e) {
            log.error("Error during IELTS Speaking evaluation", e);
            throw new AppException(ErrorCode.EVALUATION_FAILED);
        }
    }

    /**
     * Evaluate speaking using already-transcribed text (avoids redundant transcription)
     */
    private IELTSSpeakingResponse evaluateSpeakingWithTranscript(
            String transcribedText,
            String questionText,
            String part,
            Map<String, Object> audioFeatures) {
        try {
            log.info("Calling IELTSensei evaluation API with transcript");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Prepare request body matching IELTSensei format
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("evaluate_type", "single_part");
            requestBody.put("part", part);
            
            // Create responses array with question and transcribed answer
            Map<String, Object> response = new HashMap<>();
            response.put("question", questionText);
            response.put("answer", transcribedText);
            response.put("part", part);
            response.put("audio_features", audioFeatures);
            requestBody.put("responses", Collections.singletonList(response));
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            String evaluationUrl = ieltsSenseiUrl + "/api/speaking/evaluate";
            log.info("Calling evaluation at: {}", evaluationUrl);
            
            ResponseEntity<IELTSSpeakingResponse> evaluationResponse = restTemplate.exchange(
                    evaluationUrl,
                    HttpMethod.POST,
                    requestEntity,
                    IELTSSpeakingResponse.class
            );
            
            if (evaluationResponse.getStatusCode() == HttpStatus.OK && evaluationResponse.getBody() != null) {
                IELTSSpeakingResponse result = evaluationResponse.getBody();
                log.info("IELTS evaluation completed. Band: {}", result.getOverallBand());
                return result;
            }
            
            throw new AppException(ErrorCode.EVALUATION_FAILED);
            
        } catch (Exception e) {
            log.error("Error during IELTS Speaking evaluation", e);
            throw new AppException(ErrorCode.EVALUATION_FAILED);
        }
    }
    
    /**
     * Transcribe audio using IELTSensei's transcription API
     * IELTSensei uses OpenAI Whisper API internally
     */
    private TranscriptionResult transcribeAudioViaIELTSensei(MultipartFile audioFile, String part) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            ByteArrayResource fileResource = new ByteArrayResource(audioFile.getBytes()) {
                @Override
                public String getFilename() {
                    return audioFile.getOriginalFilename();
                }
            };
            body.add("audio", fileResource);
            body.add("part", part);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String transcribeUrl = ieltsSenseiUrl + "/api/speaking/transcribe";
            log.info("Transcribing audio at: {}", transcribeUrl);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    transcribeUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String text = (String) response.getBody().get("text");
                if (text != null && !text.trim().isEmpty()) {
                    Object features = response.getBody().get("audio_features");
                    Map<String, Object> audioFeatures = features instanceof Map
                            ? (Map<String, Object>) features
                            : Collections.emptyMap();
                    return new TranscriptionResult(text.trim(), audioFeatures);
                }
            }
            
            throw new AppException(ErrorCode.TRANSCRIPTION_FAILED);
            
        } catch (HttpStatusCodeException e) {
            log.warn("IELTSensei rejected audio with status {}: {}",
                    e.getStatusCode().value(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 422) {
                throw new AppException(ErrorCode.AUDIO_QUALITY_REJECTED);
            }
            if (e.getStatusCode().is4xxClientError()) {
                throw new AppException(ErrorCode.INVALID_FILE);
            }
            throw new AppException(ErrorCode.TRANSCRIPTION_FAILED);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during transcription via IELTSensei", e);
            throw new AppException(ErrorCode.TRANSCRIPTION_FAILED);
        }
    }

    private record TranscriptionResult(
            String text,
            Map<String, Object> audioFeatures) {
    }

    /** One upload answers the questions of one part; scoring waits for all parts. */
    @Transactional
    public IELTSSpeakingResponse uploadPart(MultipartFile file, Long questionId, Long userExamPartId) {
        UserExamPart attempt = userExamPartRepository.findById(userExamPartId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_EXAM_PART_NOT_FOUND));
        requireOwner(attempt);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
        if (question.getSkillType() != SkillType.SPEAKING || attempt.getSkillType() != SkillType.SPEAKING
                || !Objects.equals(question.getExamPart().getExam().getId(), attempt.getUserExam().getExam().getId())
                || question.getSection() == null || !question.getSection().matches("part[123]")
                || Boolean.TRUE.equals(attempt.getSubmitted())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        TranscriptionResult transcription = transcribeAudioViaIELTSensei(file, question.getSection());
        UserAnswer answer = userAnswerRepository.findAllByUserExamPart_Id(userExamPartId).stream()
                .filter(item -> item.getQuestion() != null && question.getSection().equals(item.getQuestion().getSection()))
                .findFirst().orElseGet(() -> UserAnswer.builder().userExamPart(attempt).question(question).build());
        String prompts = questionRepository.findByExamPartIdAndSectionOrderByIdAsc(question.getExamPart().getId(), question.getSection())
                .stream().map(Question::getContent).filter(Objects::nonNull).collect(java.util.stream.Collectors.joining("\n"));
        answer.setQuestion(question);
        answer.setAnswerText(transcription.text());
        answer.setScore(null);
        answer.setAiScore(null);
        try {
            answer.setAiFeedback(objectMapper.writeValueAsString(Map.of(
                    "part", question.getSection(), "question_text", prompts,
                    "audio_features", transcription.audioFeatures(), "status", "TRANSCRIBED")));
        } catch (Exception exception) {
            throw new AppException(ErrorCode.AI_RESPONSE_PARSE_ERROR);
        }
        userAnswerRepository.save(answer);
        return IELTSSpeakingResponse.builder().status("TRANSCRIBED").build();
    }

    static void requireOwner(UserExamPart attempt) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (!auth.getName().equals(attempt.getUserExam().getUser().getUsername())) {
            throw new AppException(ErrorCode.USER_EXAM_PART_NOT_FOUND);
        }
    }
}
