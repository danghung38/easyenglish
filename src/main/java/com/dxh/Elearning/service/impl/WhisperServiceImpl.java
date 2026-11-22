package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.dto.response.TranscriptionResponse;
import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import com.dxh.Elearning.service.interfac.WhisperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Implementation of WhisperService for audio transcription
 */
@Service
@Slf4j
public class WhisperServiceImpl implements WhisperService {

    @Value("${whisper.service.url:http://localhost:8000}")
    private String whisperServiceUrl;

    @Value("${whisper.service.timeout:30000}")
    private int timeout;

    private final RestTemplate restTemplate;

    public WhisperServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String transcribeAudio(MultipartFile audioFile) {
        return transcribeAudio(audioFile, null);
    }

    @Override
    public String transcribeAudio(MultipartFile audioFile, String language) {
        try {
            log.info("Transcribing audio file: {}", audioFile.getOriginalFilename());

            // Validate file
            if (audioFile.isEmpty()) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }

            // Prepare request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Create multipart body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // Add file
            ByteArrayResource fileResource = new ByteArrayResource(audioFile.getBytes()) {
                @Override
                public String getFilename() {
                    return audioFile.getOriginalFilename();
                }
            };
            body.add("file", fileResource);

            // Add language if specified
            if (language != null && !language.isEmpty()) {
                body.add("language", language);
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Call Whisper service
            String url = whisperServiceUrl + "/transcribe";
            log.info("Calling Whisper service at: {}", url);

            ResponseEntity<TranscriptionResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    TranscriptionResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String transcribedText = response.getBody().getText();
                log.info("Transcription completed successfully. Text length: {}", transcribedText.length());
                return transcribedText;
            } else {
                log.error("Whisper service returned non-OK status: {}", response.getStatusCode());
                throw new AppException(ErrorCode.TRANSCRIPTION_FAILED);
            }

        } catch (IOException e) {
            log.error("Error reading audio file", e);
            throw new AppException(ErrorCode.INVALID_FILE);
        } catch (Exception e) {
            log.error("Error during transcription", e);
            throw new AppException(ErrorCode.TRANSCRIPTION_FAILED);
        }
    }

    @Override
    public boolean isServiceHealthy() {
        try {
            String url = whisperServiceUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("Whisper service health check failed", e);
            return false;
        }
    }
}
