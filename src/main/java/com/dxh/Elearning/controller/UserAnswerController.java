package com.dxh.Elearning.controller;

import com.dxh.Elearning.dto.request.UserExamRequest;
import com.dxh.Elearning.dto.response.ApiResponse;
import com.dxh.Elearning.dto.response.UserAnswerResponse;
import com.dxh.Elearning.dto.response.UserExamResponse;
import com.dxh.Elearning.service.interfac.UserAnswerService;
import com.dxh.Elearning.service.interfac.UserExamService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/useranswers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserAnswerController {
    UserAnswerService userAnswerService;

    @Operation(method = "GET", summary = "Get user answers by UserExamPart ID", description = "Retrieve list of answers for a specific exam part")
    @GetMapping("/answers/{userExamPartId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')")
    public ApiResponse<List<UserAnswerResponse>> getAnswers(@PathVariable Long userExamPartId) {
        List<UserAnswerResponse> answers = userAnswerService.getAnswersByUserExamPartId(userExamPartId);
        return ApiResponse.<List<UserAnswerResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully retrieved answers")
                .result(answers)
                .build();
    }
}
