package com.dxh.Elearning.controller;

import com.dxh.Elearning.dto.request.VocabularyRequest;
import com.dxh.Elearning.dto.response.ApiResponse;
import com.dxh.Elearning.dto.response.VocabularyResponse;
import com.dxh.Elearning.dto.response.VocabularyTopicResponse;
import com.dxh.Elearning.service.AwsS3Service;
import com.dxh.Elearning.service.interfac.VocabularyService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/vocabulary")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VocabularyController {
    
    VocabularyService vocabularyService;
    AwsS3Service awsS3Service;
    
    // ==================== TOPIC ENDPOINTS ====================
    
    @Operation(summary = "Get all vocabulary topics", description = "Retrieve all vocabulary topics with word count")
    @GetMapping("/topics")
    public ApiResponse<List<VocabularyTopicResponse>> getAllTopics() {
        return ApiResponse.<List<VocabularyTopicResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully retrieved topics")
                .result(vocabularyService.getAllTopics())
                .build();
    }
    
    // ==================== VOCABULARY ENDPOINTS ====================
    
    @Operation(summary = "Get vocabularies by topic name", description = "Retrieve all vocabularies for a specific topic")
    @GetMapping("/topics/{topicName}/words")
    public ApiResponse<List<VocabularyResponse>> getVocabulariesByTopic(@PathVariable String topicName) {
        return ApiResponse.<List<VocabularyResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully retrieved vocabularies")
                .result(vocabularyService.getVocabulariesByTopic(topicName))
                .build();
    }
    
    @Operation(summary = "Get vocabulary by ID", description = "Retrieve a specific vocabulary by its ID")
    @GetMapping("/words/{id}")
    public ApiResponse<VocabularyResponse> getVocabularyById(@PathVariable Long id) {
        return ApiResponse.<VocabularyResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully retrieved vocabulary")
                .result(vocabularyService.getVocabularyById(id))
                .build();
    }
    
    @Operation(summary = "Create vocabulary", description = "Add a new vocabulary with audio and image files (Admin only)")
    @PostMapping(value = "/words", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<VocabularyResponse> createVocabulary(
            @RequestPart("data") VocabularyRequest request,
            @RequestPart(value = "audioFile", required = false) MultipartFile audioFile,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        
        // Upload audio file to S3 if provided
        if (audioFile != null && !audioFile.isEmpty()) {
            String audioUrl = awsS3Service.saveAudioToS3(audioFile);
            request.setPronunciationAudioUrl(audioUrl);
        }
        
        // Upload image file to S3 if provided
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = awsS3Service.saveImageToS3(imageFile);
            request.setImageUrl(imageUrl);
        }
        
        return ApiResponse.<VocabularyResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Successfully created vocabulary")
                .result(vocabularyService.createVocabulary(request))
                .build();
    }
    
    @Operation(summary = "Update vocabulary", description = "Update an existing vocabulary with optional new audio and image files (Admin only)")
    @PutMapping(value = "/words/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<VocabularyResponse> updateVocabulary(
            @PathVariable Long id,
            @RequestPart("data") VocabularyRequest request,
            @RequestPart(value = "audioFile", required = false) MultipartFile audioFile,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        
        // Upload new audio file to S3 if provided
        if (audioFile != null && !audioFile.isEmpty()) {
            String audioUrl = awsS3Service.saveAudioToS3(audioFile);
            request.setPronunciationAudioUrl(audioUrl);
        }
        
        // Upload new image file to S3 if provided
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = awsS3Service.saveImageToS3(imageFile);
            request.setImageUrl(imageUrl);
        }
        
        return ApiResponse.<VocabularyResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully updated vocabulary")
                .result(vocabularyService.updateVocabulary(id, request))
                .build();
    }
    
    @Operation(summary = "Delete vocabulary", description = "Delete a vocabulary (Admin only)")
    @DeleteMapping("/words/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Void> deleteVocabulary(@PathVariable Long id) {
        vocabularyService.deleteVocabulary(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully deleted vocabulary")
                .build();
    }
}
