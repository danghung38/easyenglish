package com.dxh.Elearning.service.interfac;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for audio transcription using Whisper
 */
public interface WhisperService {
    
    /**
     * Transcribe audio file to text
     * 
     * @param audioFile Audio file to transcribe
     * @return Transcribed text
     */
    String transcribeAudio(MultipartFile audioFile);
    
    /**
     * Transcribe audio file to text with specified language
     * 
     * @param audioFile Audio file to transcribe
     * @param language Language code (e.g., 'en', 'vi')
     * @return Transcribed text
     */
    String transcribeAudio(MultipartFile audioFile, String language);
    
    /**
     * Check if Whisper service is available
     * 
     * @return true if service is healthy
     */
    boolean isServiceHealthy();
}
