package com.dxh.Elearning.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Request DTO for audio transcription
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranscriptionRequest {
    
    /**
     * Audio file to transcribe
     */
    @NotNull(message = "Audio file is required")
    private MultipartFile file;
    
    /**
     * Language code for transcription (optional)
     * e.g., 'en' for English, 'vi' for Vietnamese
     */
    private String language;
}
