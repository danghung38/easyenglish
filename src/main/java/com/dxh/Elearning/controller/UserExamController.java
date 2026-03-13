package com.dxh.Elearning.controller;

import com.dxh.Elearning.dto.request.UserExamRequest;
import com.dxh.Elearning.dto.response.ApiResponse;
import com.dxh.Elearning.dto.response.TestHistoryResponse;
import com.dxh.Elearning.dto.response.UserExamResponse;
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
@RequestMapping("/userexams")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserExamController {
    UserExamService userExamService;

    @Operation(method = "POST", summary = "Add new user exam", description = "Send a request via this API to create new user exam")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')")
    public ApiResponse<UserExamResponse> createExam(@RequestBody UserExamRequest req) {
        return ApiResponse.<UserExamResponse>builder().code(HttpStatus.CREATED.value()).message("Successfully created user exam").result(userExamService.create(req)).build();
    }

    @Operation(method = "GET", summary = "Get user exams", description = "Get list of exams the current user has taken")
    @GetMapping("/my-exams")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')")
    public ApiResponse<List<UserExamResponse>> getMyExams() {
        List<UserExamResponse> exams = userExamService.getUserExams();
        return ApiResponse.<List<UserExamResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully retrieved user exams")
                .result(exams)
                .build();
    }

    @Operation(method = "GET", summary = "Get all user exams (Admin only)", description = "Get list of all user exams from all users - Admin access only")
    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<List<UserExamResponse>> getAllUserExams() {
        List<UserExamResponse> exams = userExamService.getAllUserExams();
        return ApiResponse.<List<UserExamResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully retrieved all user exams")
                .result(exams)
                .build();
    }

    @Operation(method = "GET", summary = "Search user exams (Admin only)", description = "Search user exams by user ID, username, email, or exam title - Admin access only")
    @GetMapping("/admin/search")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<List<UserExamResponse>> searchUserExams(
            @RequestParam(required = false) String query) {
        List<UserExamResponse> exams = userExamService.searchUserExams(query);
        return ApiResponse.<List<UserExamResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully searched user exams")
                .result(exams)
                .build();
    }

    @Operation(method = "GET", summary = "Get leaderboard", description = "Get top users ranked by average exam scores")
    @GetMapping("/leaderboard")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')")
    public ApiResponse<List<UserExamResponse>> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        List<UserExamResponse> leaderboard = userExamService.getLeaderboard(limit);
        return ApiResponse.<List<UserExamResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully retrieved leaderboard")
                .result(leaderboard)
                .build();
    }

    @Operation(
        method = "GET", 
        summary = "Get test history", 
        description = "Get test history for current user. Distinguishes between full tests (all 4 parts) and individual part tests. Returns most recent tests first."
    )
    @GetMapping("/test-history")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')")
    public ApiResponse<List<TestHistoryResponse>> getTestHistory(
            @RequestParam(defaultValue = "5") int limit) {
        log.info("Getting test history with limit: {}", limit);
        List<TestHistoryResponse> history = userExamService.getTestHistory(limit);
        return ApiResponse.<List<TestHistoryResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully retrieved test history")
                .result(history)
                .build();
    }

    @Operation(method = "GET", summary = "Get user exam by ID", description = "Get a specific user exam by its ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')")
    public ApiResponse<UserExamResponse> getUserExamById(@PathVariable Long id) {
        UserExamResponse userExam = userExamService.getUserExamById(id);
        return ApiResponse.<UserExamResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully retrieved user exam")
                .result(userExam)
                .build();
    }
}
