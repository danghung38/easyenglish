package com.dxh.Elearning.configuration;

import com.dxh.Elearning.controller.UserController;
import com.dxh.Elearning.service.AwsS3Service;
import com.dxh.Elearning.service.interfac.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class RegistrationSecurityTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AwsS3Service awsS3Service;

    @MockBean
    private CustomJwtDecoder customJwtDecoder;

    @Test
    void verifiesRegistrationWithoutJwt() throws Exception {
        mvc.perform(post("/api/users/verify-register").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"otp\":\"01234567\"}"))
                .andExpect(status().isOk());

        verify(userService).verifyRegister("01234567");
    }

    @Test
    void resendsRegistrationCodeWithoutJwt() throws Exception {
        mvc.perform(post("/api/users/resend-verification").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"learner@example.com\"}"))
                .andExpect(status().isOk());

        verify(userService).resendVerification("learner@example.com");
    }

    @Test
    void profileStillRequiresJwt() throws Exception {
        mvc.perform(get("/api/users/myInfo").contextPath("/api"))
                .andExpect(status().isUnauthorized());
    }
}
