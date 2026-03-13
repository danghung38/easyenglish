package com.dxh.Elearning.controller;

import com.dxh.Elearning.dto.request.RAGExampleRequest;
import com.dxh.Elearning.dto.response.ApiResponse;
import com.dxh.Elearning.dto.response.RAGExampleResponse;
import com.dxh.Elearning.dto.response.RAGStatsResponse;
import com.dxh.Elearning.service.interfac.RAGExampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/rag-examples")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "RAG Examples Management", description = "Admin endpoints for managing AI training examples")
public class RAGExampleController {

    private final RAGExampleService ragExampleService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all RAG examples", description = "Retrieve all training examples with optional filters")
    public ApiResponse<List<RAGExampleResponse>> getAllExamples(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double band,
            @RequestParam(required = false) String task
    ) {
        log.info("Admin fetching RAG examples - type: {}, band: {}, task: {}", type, band, task);
        List<RAGExampleResponse> examples = ragExampleService.getAllExamples(type, band, task);
        return ApiResponse.<List<RAGExampleResponse>>builder()
                .message("Retrieved " + examples.size() + " RAG examples successfully")
                .result(examples)
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get example by ID", description = "Retrieve a specific training example")
    public ApiResponse<RAGExampleResponse> getExample(@PathVariable String id) {
        log.info("Admin fetching RAG example: {}", id);
        RAGExampleResponse example = ragExampleService.getExampleById(id);
        return ApiResponse.<RAGExampleResponse>builder()
                .message("Retrieved example successfully")
                .result(example)
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new example", description = "Add a new training example to the system")
    public ApiResponse<RAGExampleResponse> createExample(@RequestBody RAGExampleRequest request) {
        log.info("Admin creating new RAG example - type: {}", request.getMetadata().get("type"));
        RAGExampleResponse example = ragExampleService.createExample(request);
        return ApiResponse.<RAGExampleResponse>builder()
                .message("Example created successfully")
                .result(example)
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update example", description = "Update an existing training example")
    public ApiResponse<RAGExampleResponse> updateExample(
            @PathVariable String id,
            @RequestBody RAGExampleRequest request
    ) {
        log.info("Admin updating RAG example: {}", id);
        RAGExampleResponse example = ragExampleService.updateExample(id, request);
        return ApiResponse.<RAGExampleResponse>builder()
                .message("Example updated successfully")
                .result(example)
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete example", description = "Remove a training example from the system")
    public ApiResponse<Void> deleteExample(@PathVariable String id) {
        log.info("Admin deleting RAG example: {}", id);
        ragExampleService.deleteExample(id);
        return ApiResponse.<Void>builder()
                .message("Example deleted successfully")
                .build();
    }

    @PostMapping("/reload")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reload AI service", description = "Trigger AI service to reload examples and rebuild FAISS index")
    public ApiResponse<Void> reloadAIService() {
        log.info("Admin triggering AI service reload");
        ragExampleService.reloadAIService();
        return ApiResponse.<Void>builder()
                .message("AI service reload triggered successfully")
                .build();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get statistics", description = "Get statistics about training examples")
    public ApiResponse<RAGStatsResponse> getStats() {
        log.info("Admin fetching RAG statistics");
        RAGStatsResponse stats = ragExampleService.getStatistics();
        return ApiResponse.<RAGStatsResponse>builder()
                .message("Retrieved statistics successfully")
                .result(stats)
                .build();
    }
}
