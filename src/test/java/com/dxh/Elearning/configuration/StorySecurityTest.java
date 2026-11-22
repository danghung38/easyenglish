package com.dxh.Elearning.configuration;

import com.dxh.Elearning.controller.StoryController;
import com.dxh.Elearning.dto.request.StoryRequest;
import com.dxh.Elearning.service.impl.StoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoryController.class)
@Import(SecurityConfig.class)
class StorySecurityTest {
    private static final String BODY = """
            {"title":"Sample","titleVi":"Mẫu","content":"Original text.",
             "translation":"Bản dịch.","level":"BEGINNER"}
            """;

    @Autowired MockMvc mvc;
    @MockBean StoryService storyService;
    @MockBean CustomJwtDecoder customJwtDecoder;

    @BeforeEach
    void setUp() {
        mockJwt("admin-token", "ROLE_ADMIN");
        mockJwt("learner-token", "ROLE_USER");
    }

    @Test
    void allowsReadingWithoutAnAdminToken() throws Exception {
        mvc.perform(get("/api/stories").contextPath("/api")).andExpect(status().isOk());
        mvc.perform(get("/api/stories/1").contextPath("/api")).andExpect(status().isOk());
    }

    @Test
    void deniesWritingForLearners() throws Exception {
        mvc.perform(post("/api/stories").contextPath("/api").header("Authorization", "Bearer learner-token")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/stories/1").contextPath("/api").header("Authorization", "Bearer learner-token")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/stories/1").contextPath("/api").header("Authorization", "Bearer learner-token"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(storyService);
    }

    @Test
    void deniesDeletingWithoutAuthentication() throws Exception {
        mvc.perform(delete("/api/stories/1").contextPath("/api"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(storyService);
    }

    @Test
    void allowsAdminToCreateUpdateAndDelete() throws Exception {
        mvc.perform(post("/api/stories").contextPath("/api").header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated());
        mvc.perform(put("/api/stories/1").contextPath("/api").header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/stories/1").contextPath("/api").header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNoContent());

        verify(storyService).create(any(StoryRequest.class));
        verify(storyService).update(eq(1L), any(StoryRequest.class));
        verify(storyService).delete(1L);
    }

    private void mockJwt(String token, String authority) {
        when(customJwtDecoder.decode(token)).thenReturn(Jwt.withTokenValue(token)
                .header("alg", "HS512")
                .subject("test-user")
                .claim("scope", authority)
                .build());
    }
}
