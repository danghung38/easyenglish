package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import com.dxh.Elearning.repo.QuestionRepository;
import com.dxh.Elearning.repo.UserAnswerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IELTSWritingServiceOwnershipTest {

    @Mock
    QuestionRepository questionRepository;

    @Mock
    UserAnswerRepository userAnswerRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void feedbackRequiresAuthentication() {
        IELTSWritingServiceImpl service = service();

        AppException error = assertThrows(
                AppException.class,
                () -> service.requestFeedback(10L)
        );

        assertEquals(ErrorCode.UNAUTHENTICATED, error.getErrorCode());
    }

    @Test
    void feedbackQueriesOnlyExamPartsOwnedByCurrentUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "alice",
                        "password",
                        List.of(() -> "USER")
                )
        );
        when(userAnswerRepository
                .findAllByUserExamPart_IdAndUserExamPart_UserExam_User_Username(99L, "alice"))
                .thenReturn(Collections.emptyList());

        AppException error = assertThrows(
                AppException.class,
                () -> service().requestFeedback(99L)
        );

        assertEquals(ErrorCode.USER_EXAM_PART_NOT_FOUND, error.getErrorCode());
        verify(userAnswerRepository)
                .findAllByUserExamPart_IdAndUserExamPart_UserExam_User_Username(99L, "alice");
    }

    @Test
    void evaluationRejectsExtremelyShortTextUsingActualWordCount() {
        AppException error = assertThrows(
                AppException.class,
                () -> service().evaluateWriting(
                        "Hi",
                        "Describe the chart.",
                        "task1",
                        500,
                        20
                )
        );

        assertEquals(ErrorCode.WRITING_RESPONSE_TOO_SHORT, error.getErrorCode());
    }

    private IELTSWritingServiceImpl service() {
        return new IELTSWritingServiceImpl(questionRepository, userAnswerRepository);
    }
}
