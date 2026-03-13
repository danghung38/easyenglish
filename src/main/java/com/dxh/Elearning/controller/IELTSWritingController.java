package com.dxh.Elearning.controller;

import com.dxh.Elearning.dto.response.ApiResponse;
import com.dxh.Elearning.dto.response.IELTSWritingResponse;
import com.dxh.Elearning.service.interfac.IELTSWritingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for IELTS Writing evaluation with AI
 * 
 * Requirements:
 * 1. Question must exist in database with skillType = WRITING
 * 2. Question.section should be "task1" or "task2"
 * 3. IELTSensei service must be running at http://localhost:5000
 * 
 * Flow:
 * - Submit essay text → Evaluate via IELTSensei AI (OpenAI/Gemini)
 * - Returns IELTS band scores (0-9) with detailed Vietnamese feedback
 */
@RestController
@RequestMapping("/ielts/writing")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "IELTS Writing", description = "AI-powered IELTS Writing evaluation with band scores")
public class IELTSWritingController {

    IELTSWritingService ieltsWritingService;

    @Operation(
            summary = "Evaluate IELTS Writing by question ID",
            description = "Submit essay text and evaluate based on writing question from database. " +
                    "Returns IELTS band scores (0-9) with detailed feedback for 4 criteria: " +
                    "Task Achievement, Coherence & Cohesion, Lexical Resource, Grammatical Range. " +
                    "\n\nNote: Question must have skillType = WRITING and section = task1/task2"
    )
    @PostMapping("/evaluate-by-question")
    public ApiResponse<IELTSWritingResponse> evaluateByQuestion(
            @RequestParam("essayText") String essayText,
            @RequestParam("questionId") Long questionId,
            @RequestParam(value = "wordCount", required = false) Integer wordCount,
            @RequestParam(value = "duration", required = false) Integer duration) {
        
        log.info("Received IELTS Writing evaluation request for questionId: {}", questionId);
        
        IELTSWritingResponse evaluation = ieltsWritingService.evaluateWritingByQuestion(
            essayText,
            questionId,
            wordCount,
            duration
        );
        
        return ApiResponse.<IELTSWritingResponse>builder()
                .code(200)
                .message("Writing evaluation completed successfully")
                .result(evaluation)
                .build();
    }
}
