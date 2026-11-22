package com.dxh.Elearning.controller;

import com.dxh.Elearning.dto.request.ExamRequest;
import com.dxh.Elearning.dto.response.ApiResponse;
import com.dxh.Elearning.dto.response.ExamPartResponse;
import com.dxh.Elearning.dto.response.ExamResponse;
import com.dxh.Elearning.dto.response.PageResponse;
import com.dxh.Elearning.dto.response.UserResponse;
import com.dxh.Elearning.service.interfac.ExamPartService;
import com.dxh.Elearning.service.interfac.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ExamController {
    ExamService examService;
    ExamPartService examPartService;

    @Operation(method = "POST", summary = "Add new exam", description = "Send a request via this API to create new exam")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<ExamResponse> createExam(
            @RequestPart("exam") ExamRequest req,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ApiResponse.<ExamResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Successfully created exam")
                .result(examService.create(req, image)).build();
    }

    @Operation(method = "PUT", summary = "Update exam", description = "Send a request via this API to update an existing exam")
    @PutMapping("/{examId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<ExamResponse> updateExam(
            @PathVariable Long examId,
            @RequestPart("exam") ExamRequest req,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ApiResponse.<ExamResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully updated exam")
                .result(examService.update(examId, req, image)).build();
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{examId}")
    public ApiResponse<?> delete(@PathVariable Long examId) {
        examService.delete(examId);
        return ApiResponse.builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("delete exam successful")
                .build();
    }

    @Operation(summary = "Get list of exam per pageNo and sort by one column", description = "Send a request via this API to get exam list by pageNo and pageSize")
    @GetMapping("/list")
    public ApiResponse<PageResponse<List<ExamResponse>>> getAllExamSortBy(@RequestParam(defaultValue = "1", required = false) Integer pageNo,
                                                                           @Min(value = 1,message = "pageSize must be greater than 1") @RequestParam(defaultValue = "20", required = false) Integer pageSize,
                                                                           @RequestParam(required = false) String sortBy) {
        log.info("get all exams");
        return ApiResponse.<PageResponse<List<ExamResponse>>>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully get exam list")
                .result(examService.getAllExamsSortBy(pageNo,pageSize,sortBy))
                .build();
    }

    @Operation(
            method = "GET",
            summary = "Get all exam parts of a specific exam",
            description = "Retrieve all exam parts (LISTENING, READING, WRITING, SPEAKING) for a specific exam by examId"
    )
    @GetMapping("/{examId}/parts")
    public ApiResponse<PageResponse<List<ExamPartResponse>>> getExamPartsByExamId(
            @PathVariable Long examId,
            @RequestParam(defaultValue = "1", required = false) Integer pageNo,
            @RequestParam(defaultValue = "100", required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy) {
        
        log.info("Get all exam parts for exam ID: {}", examId);
        
        return ApiResponse.<PageResponse<List<ExamPartResponse>>>builder()
                .code(HttpStatus.OK.value())
                .message(String.format("Successfully fetched exam parts for exam ID: %d", examId))
                .result(examPartService.getAllExamPartsByExamId(examId, pageNo, pageSize, sortBy))
                .build();
    }
}
