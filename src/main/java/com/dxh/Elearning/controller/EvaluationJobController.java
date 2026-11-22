package com.dxh.Elearning.controller;

import com.dxh.Elearning.dto.response.ApiResponse;
import com.dxh.Elearning.dto.response.EvaluationJobResponse;
import com.dxh.Elearning.service.interfac.EvaluationJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/evaluations")
@RequiredArgsConstructor
public class EvaluationJobController {
    private final EvaluationJobService evaluationJobService;

    @PostMapping("/{skill}")
    public ApiResponse<EvaluationJobResponse> create(
            @PathVariable String skill,
            @RequestBody Map<String, Object> request) {
        return response(evaluationJobService.create(skill, request));
    }

    @GetMapping("/{jobId}")
    public ApiResponse<EvaluationJobResponse> get(@PathVariable String jobId) {
        return response(evaluationJobService.get(jobId));
    }

    @PostMapping("/{jobId}/cancel")
    public ApiResponse<EvaluationJobResponse> cancel(@PathVariable String jobId) {
        return response(evaluationJobService.cancel(jobId));
    }

    @PostMapping("/{jobId}/feedback")
    public ApiResponse<EvaluationJobResponse> requestFeedback(@PathVariable String jobId) {
        return response(evaluationJobService.requestFeedback(jobId));
    }

    private ApiResponse<EvaluationJobResponse> response(EvaluationJobResponse value) {
        return ApiResponse.<EvaluationJobResponse>builder()
                .code(200)
                .message("Evaluation job status")
                .result(value)
                .build();
    }
}
