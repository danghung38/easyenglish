package com.dxh.Elearning.controller;

import com.dxh.Elearning.dto.request.ExamPartRequest;
import com.dxh.Elearning.dto.response.ApiResponse;
import com.dxh.Elearning.dto.response.ExamPartResponse;
import com.dxh.Elearning.dto.response.ExamResponse;
import com.dxh.Elearning.dto.response.PageResponse;
import com.dxh.Elearning.service.interfac.ExamPartService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/examparts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ExamPartController {
    ExamPartService examPartService;

    @Operation(method = "POST", summary = "Add new exam part", description = "Send a request via this API to create new exam part")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<ExamPartResponse> createExamPart(@RequestBody ExamPartRequest req) {
        return ApiResponse.<ExamPartResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Successfully created exam part")
                .result(examPartService.create(req)).build();
    }

    @Operation(method = "PUT", summary = "Update exam part", description = "Send a request via this API to update an existing exam part (including duration)")
    @PutMapping("/{examPartId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<ExamPartResponse> updateExamPart(
            @PathVariable Long examPartId,
            @RequestBody ExamPartRequest req) {
        return ApiResponse.<ExamPartResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully updated exam part")
                .result(examPartService.update(examPartId, req)).build();
    }



    @Operation(summary = "Get list of exam part ", description = "Send a request via this API to get exam part")
    @GetMapping("/list")
    public ApiResponse<List<ExamPartResponse>> getAllExamPartByExamId(@RequestParam Long examId){
        log.info("get all exam part by exam id");
        return ApiResponse.<List<ExamPartResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully get exam list")
                .result(examPartService.findAllByExamId(examId))
                .build();
    }


    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{examPartId}")
    public ApiResponse<?> delete(@PathVariable Long examPartId) {
        examPartService.delete(examPartId);
        return ApiResponse.builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("delete exam part successful")
                .build();
    }
}
