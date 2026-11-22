package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.dto.request.CommentRequest;
import com.dxh.Elearning.dto.response.CommentResponse;
import com.dxh.Elearning.entity.Comment;
import com.dxh.Elearning.entity.Exam;
import com.dxh.Elearning.entity.User;
import com.dxh.Elearning.repo.CommentRepository;
import com.dxh.Elearning.repo.ExamRepository;
import com.dxh.Elearning.repo.UserRepository;
import com.dxh.Elearning.service.interfac.CommentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CommentServiceImpl implements CommentService {

    CommentRepository commentRepository;
    ExamRepository examRepository;
    UserRepository userRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentResponse create(CommentRequest request) {
        User currentUser = getCurrentUser();
        
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + request.getExamId()));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .user(currentUser)
                .exam(exam)
                .build();

        comment = commentRepository.save(comment);
        return mapToResponse(comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentResponse update(Long commentId, CommentRequest request) {
        User currentUser = getCurrentUser();
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        // Check if current user is the owner or admin
        if (!comment.getUser().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new AccessDeniedException("You don't have permission to update this comment");
        }

        comment.setContent(request.getContent());
        comment = commentRepository.save(comment);
        return mapToResponse(comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long commentId) {
        User currentUser = getCurrentUser();
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        // Check if current user is the owner or admin
        if (!comment.getUser().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new AccessDeniedException("You don't have permission to delete this comment");
        }

        commentRepository.delete(comment);
    }

    @Override
    public List<CommentResponse> getCommentsByExamId(Long examId) {
        List<Comment> comments = commentRepository.findByExamIdOrderByCreatedAtDesc(examId);
        return comments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CommentResponse getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
        return mapToResponse(comment);
    }

    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .userAvatar(comment.getUser().getAvatar())
                .examId(comment.getExam().getId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().contains("ADMIN"));
    }
}
