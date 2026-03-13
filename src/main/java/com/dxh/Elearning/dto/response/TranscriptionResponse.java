package com.dxh.Elearning.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
/**
 * Response DTO for audio transcription
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TranscriptionResponse {
    
    /**
     * Transcribed text from audio
     */
    private String text;
    
    /**
     * Detected or specified language
     */
    private String language;
    
    /**
     * Duration of audio in seconds
     */
    private Double duration;
    
    /**
     * Confidence score (if available)
     */
    private Double confidence;
}
