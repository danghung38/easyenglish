package com.dxh.Elearning.controller;

import com.dxh.Elearning.dto.response.ApiResponse;
import com.dxh.Elearning.dto.response.IELTSSpeakingResponse;
import com.dxh.Elearning.service.interfac.IELTSSpeakingService;
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
 * Controller for IELTS Speaking evaluation with AI
 * 
 * Requirements:
 * 1. Question must exist in database with skillType = SPEAKING
 * 2. Question.section should be "part1", "part2", or "part3"
 * 3. IELTSensei service must be running at http://localhost:5000
 * 
 * Flow:
 * - Upload audio file → Transcribe via IELTSensei (faster-whisper local) → Evaluate via AI
 * - Returns IELTS band scores (0-9) with detailed Vietnamese feedback
 */
@RestController
@RequestMapping("/ielts/speaking")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "IELTS Speaking", description = "AI-powered IELTS Speaking evaluation with band scores")
public class IELTSSpeakingController {

    IELTSSpeakingService ieltsSpeakingService;

    @Operation(
            summary = "Evaluate IELTS Speaking by question ID",
            description = "Upload audio file and evaluate based on speaking question from database. " +
                    "Returns IELTS band scores (0-9) with detailed feedback for 4 criteria: " +
                    "Fluency & Coherence, Lexical Resource, Grammatical Range, Pronunciation. " +
                    "Results are automatically saved to user_answers table. " +
                    "\n\nNote: Question must have skillType = SPEAKING and section = part1/part2/part3"
    )
    @PostMapping(value = "/evaluate-by-question", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<IELTSSpeakingResponse> evaluateByQuestion(
            @RequestParam("file") MultipartFile file,
            @RequestParam("questionId") Long questionId,
            @RequestParam("userExamPartId") Long userExamPartId,
            @RequestParam(value = "audioUrl", required = false) String audioUrl) {
        
        log.info("Received IELTS Speaking evaluation request for questionId: {}, userExamPartId: {}", questionId, userExamPartId);
        
        IELTSSpeakingResponse evaluation = ieltsSpeakingService.evaluateSpeakingByQuestion(
            file, 
            questionId,
            userExamPartId,
            audioUrl
        );
        
        return ApiResponse.<IELTSSpeakingResponse>builder()
                .result(evaluation)
                .build();
    }
}
