package com.dxh.Elearning.controller;

import com.dxh.Elearning.dto.request.SubmitRLPartRequest;
import com.dxh.Elearning.dto.request.SubmitSpeakingExamRequest;
import com.dxh.Elearning.dto.request.SubmitWritingExamRequest;
import com.dxh.Elearning.dto.request.UserExamRequest;
import com.dxh.Elearning.dto.response.*;
import com.dxh.Elearning.service.interfac.UserExamPartService;
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
@RequestMapping("/userexamparts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserExamPartController {
    UserExamPartService userExamPartService;

    @Operation(method = "POST", summary = "Submit answer reading/listening", description = "Send a request via this API to submit answer reading/listening")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')")
    public ApiResponse<?> submitRLPart(@RequestBody SubmitRLPartRequest req) {
        return ApiResponse.<SubmitRLPartResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Part submitted successfully")
                .result(userExamPartService.submitRLPart(req)).build();
    }

    @Operation(
        summary = "Submit Speaking exam and update final score", 
        description = "Submit the entire Speaking exam (after all parts evaluated) and update UserExamPart with final band score"
    )
    @PostMapping("/speaking/submit")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')")
    public ApiResponse<?> submitSpeakingExam(@RequestBody SubmitSpeakingExamRequest req) {
        log.info("Submitting Speaking exam for userExamPartId: {}, overallBand: {}", 
            req.getUserExamPartId(), req.getOverallBand());
        
        userExamPartService.submitSpeakingExam(req);
        
        return ApiResponse.builder()
                .code(HttpStatus.OK.value())
                .message("Speaking exam submitted successfully")
                .build();
    }
    
    @Operation(
        summary = "Submit Writing exam with AI evaluation results",
        description = "Submit the entire Writing exam with AI evaluation results for each task (Task 1, Task 2). " +
                "Saves UserAnswer records with essay text and AI scores, updates UserExamPart with overall band."
    )
    @PostMapping("/writing/submit")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')")
    public ApiResponse<?> submitWritingExam(@RequestBody SubmitWritingExamRequest req) {
        log.info("Submitting Writing exam for userExamPartId: {}, overallBand: {}", 
            req.getUserExamPartId(), req.getOverallBand());

        WritingResultResponse result =
                userExamPartService.submitWritingExam(req);

        return ApiResponse.<WritingResultResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Writing exam submitted successfully")
                .result(result)
                .build();
    }

}
