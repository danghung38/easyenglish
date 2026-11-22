package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.dto.request.SubmitSpeakingExamRequest;
import com.dxh.Elearning.dto.response.EvaluationJobResponse;
import com.dxh.Elearning.dto.response.IELTSSpeakingResponse;
import com.dxh.Elearning.entity.UserAnswer;
import com.dxh.Elearning.entity.UserExamPart;
import com.dxh.Elearning.enums.SkillType;
import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import com.dxh.Elearning.repo.UserAnswerRepository;
import com.dxh.Elearning.repo.UserExamPartRepository;
import com.dxh.Elearning.service.interfac.EvaluationJobService;
import com.dxh.Elearning.service.interfac.UserExamPartService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.util.*;

/** Whole-session scoring and on-demand feedback reuse the existing AI job queue. */
@Service
@RequiredArgsConstructor
public class SpeakingSessionService {
    private final UserExamPartRepository attemptRepository;
    private final UserAnswerRepository answerRepository;
    private final EvaluationJobService jobs;
    private final UserExamPartService submissions;
    private final ObjectMapper mapper;

    @Transactional
    public EvaluationJobResponse complete(Long attemptId) {
        UserExamPart attempt = ownedAttempt(attemptId);
        List<UserAnswer> answers = answers(attemptId);
        List<Map<String, Object>> responses = new ArrayList<>();
        Set<String> parts = new HashSet<>();
        for (UserAnswer answer : answers) {
            Map<String, Object> stored = metadata(answer);
            String part = answer.getQuestion().getSection();
            if (!parts.add(part) || answer.getAnswerText() == null || answer.getAnswerText().isBlank()
                    || !(stored.get("audio_features") instanceof Map)) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            responses.add(Map.of("part", part, "question", stored.getOrDefault("question_text", answer.getQuestion().getContent()),
                    "text", answer.getAnswerText(), "audio_features", stored.get("audio_features")));
        }
        if (!parts.equals(Set.of("part1", "part2", "part3"))) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if (Boolean.TRUE.equals(attempt.getSubmitted())) return current(attemptId, false);
        String key;
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(mapper.writeValueAsBytes(responses));
            key = "speaking-" + attemptId + "-" + HexFormat.of().formatHex(hash);
        } catch (Exception exception) {
            throw new AppException(ErrorCode.AI_RESPONSE_PARSE_ERROR);
        }
        EvaluationJobResponse job = jobs.create("speaking", Map.of(
                "requestId", key, "idempotencyKey", key, "evaluate_type", "full", "responses", responses));
        persist(answers, job);
        return job;
    }

    @Transactional
    public EvaluationJobResponse current(Long attemptId, boolean requestFeedback) {
        ownedAttempt(attemptId);
        List<UserAnswer> answers = answers(attemptId);
        String jobId = answers.stream().map(this::metadata).map(item -> item.get("job_id"))
                .filter(Objects::nonNull).map(Object::toString).findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));
        EvaluationJobResponse job = requestFeedback ? jobs.requestFeedback(jobId) : jobs.get(jobId);
        persist(answers, job);
        return job;
    }

    private void persist(List<UserAnswer> answers, EvaluationJobResponse job) {
        Map<String, Object> result = job.getResult();
        IELTSSpeakingResponse score = result == null ? null : mapper.convertValue(result, IELTSSpeakingResponse.class);
        for (UserAnswer answer : answers) {
            Map<String, Object> stored = metadata(answer);
            stored.put("job_id", job.getJobId());
            stored.put("scope", "full_session");
            if (result != null) stored.putAll(result);
            if (score != null) {
                answer.setAiScore(score.getOverallBand());
                answer.setScore(score.getOverallBand());
            }
            try {
                answer.setAiFeedback(mapper.writeValueAsString(stored));
            } catch (Exception exception) {
                throw new AppException(ErrorCode.AI_RESPONSE_PARSE_ERROR);
            }
        }
        answerRepository.saveAll(answers);
        if (score != null && "COMPLETED".equals(job.getStatus()) && score.getOverallBand() == null) {
            // Completed assessment, but no overall IELTS estimate without Pronunciation.
            UserExamPart attempt = answers.get(0).getUserExamPart();
            attempt.setSubmitted(true);
            attempt.setScore(null);
            attemptRepository.save(attempt);
        }
        if (score != null && "COMPLETED".equals(job.getStatus()) && score.getOverallBand() != null) {
            var criteria = score.getCriteriaScores();
            submissions.submitSpeakingExam(SubmitSpeakingExamRequest.builder()
                    .userExamPartId(answers.get(0).getUserExamPart().getId())
                    .fluencyCoherence(criteria.getFluencyCoherence()).lexicalResource(criteria.getLexicalResource())
                    .grammaticalRange(criteria.getGrammaticalRange()).pronunciation(criteria.getPronunciation()).build());
        }
    }

    private UserExamPart ownedAttempt(Long id) {
        UserExamPart attempt = attemptRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_EXAM_PART_NOT_FOUND));
        IELTSSpeakingServiceImpl.requireOwner(attempt);
        if (attempt.getSkillType() != SkillType.SPEAKING) throw new AppException(ErrorCode.INVALID_REQUEST);
        return attempt;
    }

    private List<UserAnswer> answers(Long id) {
        return answerRepository.findAllByUserExamPart_Id(id).stream()
                .filter(answer -> answer.getQuestion() != null && answer.getQuestion().getSkillType() == SkillType.SPEAKING)
                .sorted(Comparator.comparing(answer -> answer.getQuestion().getSection()))
                .toList();
    }

    private Map<String, Object> metadata(UserAnswer answer) {
        try {
            return mapper.readValue(answer.getAiFeedback(), new TypeReference<LinkedHashMap<String, Object>>() { });
        } catch (Exception exception) {
            throw new AppException(ErrorCode.AI_RESPONSE_PARSE_ERROR);
        }
    }
}
