package com.dxh.Elearning.service;

import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailServiceTest {
    private SendGrid sendGrid;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        sendGrid = mock(SendGrid.class);
        emailService = new EmailService(sendGrid);
        ReflectionTestUtils.setField(emailService, "from", "noreply@example.com");
        ReflectionTestUtils.setField(emailService, "templateId", "verification-template");
        ReflectionTestUtils.setField(
                emailService,
                "verificationLink",
                "http://localhost:8081/api/users/confirm-email"
        );
    }

    @Test
    void verificationEmailContainsConfiguredBackendLink() throws IOException {
        Response response = mock(Response.class);
        when(response.getStatusCode()).thenReturn(202);
        when(sendGrid.api(any(Request.class))).thenReturn(response);

        emailService.sendVerificationEmail("learner@example.com", "Learner", "secret-123");

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(sendGrid).api(requestCaptor.capture());
        assertTrue(requestCaptor.getValue().getBody().contains(
                "http://localhost:8081/api/users/confirm-email?secretKey=secret-123"
        ));
    }

    @Test
    void rejectedVerificationEmailIsReportedAsFailure() throws IOException {
        Response response = mock(Response.class);
        when(response.getStatusCode()).thenReturn(401);
        when(sendGrid.api(any(Request.class))).thenReturn(response);

        assertThrows(
                IOException.class,
                () -> emailService.sendVerificationEmail(
                        "learner@example.com",
                        "Learner",
                        "secret-123"
                )
        );
    }
}
