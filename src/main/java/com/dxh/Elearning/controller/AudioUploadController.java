package com.dxh.Elearning.controller;

import com.dxh.Elearning.dto.response.ApiResponse;
import com.dxh.Elearning.dto.response.TranscriptionResponse;
import com.dxh.Elearning.service.interfac.WhisperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller for handling audio upload and transcription
 */
@RestController
@RequestMapping("/audio")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Audio Transcription", description = "APIs for audio file upload and transcription")
public class AudioUploadController {

    WhisperService whisperService;

    @Operation(
            summary = "Transcribe audio to text",
            description = "Upload an audio file and get the transcribed text using Whisper AI. Supports all audio formats including .opus"
    )
    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<TranscriptionResponse> transcribeAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "language", defaultValue = "en") String language) {
        
        log.info("Received transcription request for file: {}", file.getOriginalFilename());
        
        String transcribedText = whisperService.transcribeAudio(file, language);
        
        TranscriptionResponse response = TranscriptionResponse.builder()
                .text(transcribedText)
                .language(language)
                .build();
        
        return ApiResponse.<TranscriptionResponse>builder()
                .result(response)
                .build();
    }

    @Operation(
            summary = "Check Whisper service health",
            description = "Check if the Whisper transcription service is available and healthy"
    )
    @GetMapping("/health")
    public ApiResponse<Boolean> checkHealth() {
        boolean isHealthy = whisperService.isServiceHealthy();
        return ApiResponse.<Boolean>builder()
                .result(isHealthy)
                .build();
    }
}
