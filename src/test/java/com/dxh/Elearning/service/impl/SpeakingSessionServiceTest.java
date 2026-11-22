package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.dto.response.EvaluationJobResponse;
import com.dxh.Elearning.entity.*;
import com.dxh.Elearning.enums.SkillType;
import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import com.dxh.Elearning.repo.UserAnswerRepository;
import com.dxh.Elearning.repo.UserExamPartRepository;
import com.dxh.Elearning.service.interfac.EvaluationJobService;
import com.dxh.Elearning.service.interfac.UserExamPartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpeakingSessionServiceTest {
    @Mock UserExamPartRepository attempts;
    @Mock UserAnswerRepository answers;
    @Mock EvaluationJobService jobs;
    @Mock UserExamPartService submissions;
    private final ObjectMapper mapper = new ObjectMapper();

    @AfterEach void clearAuthentication() { SecurityContextHolder.clearContext(); }
    private SpeakingSessionService service() { return new SpeakingSessionService(attempts, answers, jobs, submissions, mapper); }
    private UserExamPart attempt() {
        User user = User.builder().username("alice").build();
        UserExam exam = UserExam.builder().user(user).build();
        UserExamPart attempt = UserExamPart.builder().userExam(exam).skillType(SkillType.SPEAKING).submitted(false).build();
        attempt.setId(10L);
        return attempt;
    }
    private void authenticate(String username) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(username, "", List.of(() -> "ROLE_USER")));
    }
    private List<UserAnswer> parts(UserExamPart attempt) throws Exception {
        List<UserAnswer> result = new ArrayList<>();
        for (String part : List.of("part1", "part2", "part3")) {
            Question question = Question.builder().skillType(SkillType.SPEAKING).section(part).content("Question").build();
            result.add(UserAnswer.builder().userExamPart(attempt).question(question).answerText("I enjoy studying.")
                    .aiFeedback(mapper.writeValueAsString(Map.of("audio_features", Map.of("asr_confidence", 0.9), "job_id", "job-1"))).build());
        }
        return result;
    }

    @Test void anonymousCannotScoreOrRequestFeedback() {
        when(attempts.findById(10L)).thenReturn(Optional.of(attempt()));
        assertEquals(ErrorCode.UNAUTHENTICATED, assertThrows(AppException.class, () -> service().complete(10L)).getErrorCode());
        verifyNoInteractions(jobs, answers);
    }

    @Test void otherUserCannotReadSession() {
        authenticate("bob");
        when(attempts.findById(10L)).thenReturn(Optional.of(attempt()));
        assertEquals(ErrorCode.USER_EXAM_PART_NOT_FOUND, assertThrows(AppException.class, () -> service().current(10L, false)).getErrorCode());
        verifyNoInteractions(jobs, answers);
    }

    @Test void completionRequiresAllThreePartsBeforeLlm() throws Exception {
        authenticate("alice");
        UserExamPart attempt = attempt();
        when(attempts.findById(10L)).thenReturn(Optional.of(attempt));
        when(answers.findAllByUserExamPart_Id(10L)).thenReturn(parts(attempt).subList(0, 2));
        assertEquals(ErrorCode.INVALID_REQUEST, assertThrows(AppException.class, () -> service().complete(10L)).getErrorCode());
        verifyNoInteractions(jobs, submissions);
    }

    @Test void completionCreatesOneFullSessionJobWithAllParts() throws Exception {
        authenticate("alice");
        UserExamPart attempt = attempt();
        when(attempts.findById(10L)).thenReturn(Optional.of(attempt));
        when(answers.findAllByUserExamPart_Id(10L)).thenReturn(parts(attempt));
        when(jobs.create(eq("speaking"), anyMap())).thenAnswer(invocation -> {
            Map<String, Object> payload = invocation.getArgument(1);
            assertEquals("full", payload.get("evaluate_type"));
            assertEquals(3, ((List<?>) payload.get("responses")).size());
            assertFalse(payload.containsKey("target_band"));
            return EvaluationJobResponse.builder().jobId("job-1").status("PENDING").build();
        });
        assertEquals("job-1", service().complete(10L).getJobId());
        verifyNoInteractions(submissions);
    }

    @Test void resultPersistsFeedbackAndSubmitsServerCriteriaNotClientBand() throws Exception {
        authenticate("alice");
        UserExamPart attempt = attempt();
        List<UserAnswer> rows = parts(attempt);
        when(attempts.findById(10L)).thenReturn(Optional.of(attempt));
        when(answers.findAllByUserExamPart_Id(10L)).thenReturn(rows);
        Map<String, Object> result = Map.of("overall_band", 7.5, "score_status", "FINAL", "feedback_status", "COMPLETED",
                "feedback", Map.of("criterion_feedback", Map.of("pronunciation", "Experimental")),
                "criteria_scores", Map.of("fluency_coherence", 7.5, "lexical_resource", 8.0, "grammatical_range_accuracy", 7.5, "pronunciation", 7.0));
        when(jobs.get("job-1")).thenReturn(EvaluationJobResponse.builder().jobId("job-1").status("COMPLETED").result(result).build());
        service().current(10L, false);
        assertTrue(rows.stream().allMatch(row -> row.getAiScore() == 7.5 && row.getAiFeedback().contains("criterion_feedback")));
        verify(submissions).submitSpeakingExam(argThat(req -> req.getOverallBand() == null && req.getFluencyCoherence() == 7.5 && req.getPronunciation() == 7.0));
    }
}
