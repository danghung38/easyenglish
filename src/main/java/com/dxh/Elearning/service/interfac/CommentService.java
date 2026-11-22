package com.dxh.Elearning.service.interfac;

import com.dxh.Elearning.dto.request.CommentRequest;
import com.dxh.Elearning.dto.response.CommentResponse;

import java.util.List;

public interface CommentService {
    CommentResponse create(CommentRequest request);

    CommentResponse update(Long commentId, CommentRequest request);

    void delete(Long commentId);

    List<CommentResponse> getCommentsByExamId(Long examId);

    CommentResponse getCommentById(Long commentId);
}
