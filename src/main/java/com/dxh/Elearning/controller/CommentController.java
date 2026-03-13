package com.dxh.Elearning.controller;

import com.dxh.Elearning.dto.request.CommentRequest;
import com.dxh.Elearning.dto.response.ApiResponse;
import com.dxh.Elearning.dto.response.CommentResponse;
import com.dxh.Elearning.service.interfac.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CommentController {
    
    CommentService commentService;

    @Operation(method = "GET", summary = "Get comments by exam ID", description = "Get all comments for a specific exam")
    @GetMapping("/exam/{examId}")
    public ApiResponse<List<CommentResponse>> getCommentsByExamId(@PathVariable Long examId) {
        return ApiResponse.<List<CommentResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully retrieved comments")
                .result(commentService.getCommentsByExamId(examId))
                .build();
    }

    @Operation(method = "GET", summary = "Get comment by ID", description = "Get a specific comment by its ID")
    @GetMapping("/{commentId}")
    public ApiResponse<CommentResponse> getCommentById(@PathVariable Long commentId) {
        return ApiResponse.<CommentResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully retrieved comment")
                .result(commentService.getCommentById(commentId))
                .build();
    }

    @Operation(method = "POST", summary = "Create new comment", description = "Create a new comment for an exam")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CommentResponse> createComment(@Valid @RequestBody CommentRequest request) {
        return ApiResponse.<CommentResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Successfully created comment")
                .result(commentService.create(request))
                .build();
    }

    @Operation(method = "PUT", summary = "Update comment", description = "Update an existing comment")
    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request) {
        return ApiResponse.<CommentResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully updated comment")
                .result(commentService.update(commentId, request))
                .build();
    }

    @Operation(method = "DELETE", summary = "Delete comment", description = "Delete a comment")
    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> deleteComment(@PathVariable Long commentId) {
        commentService.delete(commentId);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully deleted comment")
                .build();
    }
}
