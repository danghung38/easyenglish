package com.dxh.Elearning.controller;

import com.dxh.Elearning.dto.request.*;
import com.dxh.Elearning.dto.response.ApiResponse;
import com.dxh.Elearning.dto.response.ExamPartResponse;
import com.dxh.Elearning.dto.response.PageResponse;
import com.dxh.Elearning.dto.response.QuestionResponse;
import com.dxh.Elearning.enums.SkillType;
import com.dxh.Elearning.service.interfac.ExamPartService;
import com.dxh.Elearning.service.interfac.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QuestionController {
    QuestionService questionService;

    @Operation(
            method = "POST",
            summary = "Upload Image for Reading Section",
            description = "Upload image for a reading section. Select section from dropdown (SECTION1-5). Updates all questions in that section with the image URL. Questions must be created first."
    )
    @PostMapping("/reading/section/image")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<List<QuestionResponse>> uploadReadingSectionImage(
            @RequestParam("examPartId") Long examPartId,
            @RequestParam("section") String sectionName, // SECTION1, SECTION2, etc from enum
            @RequestPart("image") MultipartFile imageFile) {
        
        List<QuestionResponse> responses = questionService.uploadImageForSection(examPartId, sectionName, imageFile);
        
        return ApiResponse.<List<QuestionResponse>>builder()
                .code(HttpStatus.OK.value())
                .message(String.format("Successfully uploaded image for %s (%d questions updated)", 
                        sectionName, responses.size()))
                .result(responses)
                .build();
    }

    @Operation(
            method = "POST",
            summary = "Add Single Reading Question",
            description = "Add one reading question. Select section from dropdown (SECTION1-5). Question will be linked to the section."
    )
    @PostMapping("/reading/question")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<QuestionResponse> addReadingQuestion(@RequestBody QuestionRequest req) {
        QuestionResponse response = questionService.createQuestionReading(req);
        return ApiResponse.<QuestionResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message(String.format("Successfully added question to %s", req.getSection()))
                .result(response)
                .build();
    }

    @Operation(
            method = "POST",
            summary = "Add Multiple Reading Questions (Batch)",
            description = "Add multiple reading questions at once. Select section from dropdown (SECTION1-5) in the request."
    )
    @PostMapping("/reading/questions-batch")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<List<QuestionResponse>> addReadingQuestionsBatch(
            @RequestBody CreateReadingPassageRequest req) {
        
        List<QuestionResponse> responses = questionService.createReadingQuestionsBatch(
                req.getExamPartId(), 
                req.getSection(), 
                req.getQuestions()
        );
        
        return ApiResponse.<List<QuestionResponse>>builder()
                .code(HttpStatus.CREATED.value())
                .message(String.format("Successfully added %d questions to %s", 
                        responses.size(), req.getSection()))
                .result(responses)
                .build();
    }

    //update question
    @Operation(method = "PUT", summary = "Update reading question", description = "Update a reading question by its ID")
    @PutMapping("/reading/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<QuestionResponse> updateReadingQuestion(
            @PathVariable Long id,
            @RequestBody UpdateQuestionReadingRequest req) {
        QuestionResponse updated = questionService.updateQuestionReading(id, req);
        return ApiResponse.<QuestionResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Reading question updated successfully")
                .result(updated)
                .build();
    }

    @Operation(
            method = "POST",
            summary = "Create listening question with audio",
            description = "Create a single listening question with its own audio file. Each question has a separate audio file and options."
    )
    @PostMapping("/listening/question")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<QuestionResponse> createListeningQuestion(
            @RequestPart("question") QuestionRequest req,
            @RequestPart("audio") MultipartFile audioFile) {

        QuestionResponse response = questionService.createQuestionListening(req, audioFile);

        return ApiResponse.<QuestionResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Successfully created listening question with audio")
                .result(response)
                .build();
    }

    @Operation(
            method = "POST",
            summary = "Create single listening question (Simple API)",
            description = "Create a single listening question without audio file. Audio will be added later at section level. Use this API to create individual questions one by one."
    )
    @PostMapping("/listening/simple")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<QuestionResponse> createSimpleListeningQuestion(
            @RequestBody QuestionRequest req) {

        QuestionResponse response = questionService.createQuestionListening(req);

        return ApiResponse.<QuestionResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Successfully created listening question (without audio)")
                .result(response)
                .build();
    }
    
    @Operation(
            method = "POST",
            summary = "Add Multiple Listening Questions (Batch)",
            description = "Add multiple listening questions at once. Select section from dropdown (SECTION1-5) in the request."
    )
    @PostMapping("/listening/questions-batch")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<List<QuestionResponse>> addListeningQuestionsBatch(
            @RequestBody CreateReadingPassageRequest req) {
        
        List<QuestionResponse> responses = questionService.createListeningQuestionsBatch(
                req.getExamPartId(), 
                req.getSection(), 
                req.getQuestions()
        );
        
        return ApiResponse.<List<QuestionResponse>>builder()
                .code(HttpStatus.CREATED.value())
                .message(String.format("Successfully added %d listening questions to %s", 
                        responses.size(), req.getSection()))
                .result(responses)
                .build();
    }
    //update question listening
    @Operation(method = "PUT", summary = "Update listening question", description = "Update a listening question by its ID")
    @PutMapping("/listening/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<QuestionResponse> updateQuestionListening(
            @PathVariable Long id,
            @RequestPart("question") UpdateQuestionListeningRequest req,
            @RequestPart(value = "audio", required = false) MultipartFile audioFile) {

        QuestionResponse updated = questionService.updateQuestionListening(id, req, audioFile);

        return ApiResponse.<QuestionResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Listening question updated successfully")
                .result(updated)
                .build();
    }

    @Operation(
            method = "POST",
            summary = "Add new question speaking",
            description = "Create new IELTS Speaking question. Set section to 'part1' (Interview), 'part2' (Long Turn), or 'part3' (Discussion)"
    )
    @PostMapping("/speaking")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<QuestionResponse> createSpeakingQuestion(@RequestBody QuestionRequest req) {
        QuestionResponse response = questionService.createQuestionSpeaking(req);
        return ApiResponse.<QuestionResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Successfully created speaking question")
                .result(response)
                .build();
    }

    @Operation(method = "PUT", summary = "Update speaking question", description = "Update a speaking question by its ID")
    @PutMapping("/speaking/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<QuestionResponse> updateSpeakingQuestion(
            @PathVariable Long id,
            @RequestBody UpdateQuestionSpeakingRequest req) {

        QuestionResponse updated = questionService.updateQuestionSpeaking(id, req);

        return ApiResponse.<QuestionResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Speaking question updated successfully")
                .result(updated)
                .build();
    }

    @Operation(
            method = "POST",
            summary = "Add new question writing",
            description = "Create new IELTS Writing question. Set section to 'task1' (Report/Chart with image) or 'task2' (Essay). For Task 1, include image file."
    )
    @PostMapping("/writing")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<QuestionResponse> createWritingQuestion(
            @RequestPart("question") QuestionRequest req,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        QuestionResponse response = questionService.createQuestionWriting(req, imageFile);
        return ApiResponse.<QuestionResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Successfully created writing question")
                .result(response)
                .build();
    }

    @Operation(method = "PUT", summary = "Update writing question", description = "Update a writing question by its ID. Include image file for Task 1 charts/graphs.")
    @PutMapping("/writing/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<QuestionResponse> updateWritingQuestion(
            @PathVariable Long id,
            @RequestPart("question") UpdateQuestionWritingRequest req,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        QuestionResponse updated = questionService.updateQuestionWriting(id, req, imageFile);

        return ApiResponse.<QuestionResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Writing question updated successfully")
                .result(updated)
                .build();
    }

    @Operation(
            method = "GET",
            summary = "Get list of questions by examPart",
            description = "Retrieve paginated list of questions filtered by examPartId and skillType"
    )
    @GetMapping("/list")
    public ApiResponse<PageResponse<List<QuestionResponse>>> getQuestionsByExamPart(
            @RequestParam Long examPartId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "100") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy) {

        return ApiResponse.<PageResponse<List<QuestionResponse>>>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully fetched questions")
                .result(questionService.getQuestionsByExamPart(examPartId, pageNo, pageSize, sortBy))
                .build();
    }

    @Operation(
            method = "GET",
            summary = "Get Reading questions by examPartId",
            description = "Retrieve all reading questions for a specific exam part. Returns questions grouped by section/passage."
    )
    @GetMapping("/reading/exam-part/{examPartId}")
    public ApiResponse<List<QuestionResponse>> getReadingQuestions(
            @PathVariable Long examPartId) {

        PageResponse<List<QuestionResponse>> pageResponse = questionService.getQuestionsByExamPart(
                examPartId, 1, 1000, "id:asc"
        );

        return ApiResponse.<List<QuestionResponse>>builder()
                .code(HttpStatus.OK.value())
                .message(String.format("Successfully fetched %d reading questions", 
                        pageResponse.getTotalElements()))
                .result(pageResponse.getItems())
                .build();
    }

    @Operation(
            method = "POST",
            summary = "Assign existing questions to section with audio (Legacy API)",
            description = "Assign pre-created questions to a section and add audio file. Requires question IDs to exist first. Section name must be one of: SECTION1, SECTION2, SECTION3, SECTION4, SECTION5. For new projects, use /listening/create-section instead."
    )
    @PostMapping("/listening/section")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<List<QuestionResponse>> assignQuestionsToSection(
            @RequestPart("section") SectionRequest req,
            @RequestPart("audio") MultipartFile audioFile) {

        List<QuestionResponse> responses = questionService.createListeningSection(req, audioFile);

        return ApiResponse.<List<QuestionResponse>>builder()
                .code(HttpStatus.CREATED.value())
                .message(String.format("Successfully assigned %d questions to %s with audio", 
                        req.getQuestionIds().size(), req.getSectionDisplayName()))
                .result(responses)
                .build();
    }

    @Operation(
            method = "GET", 
            summary = "Get questions by section",
            description = "Get all questions in a specific section (e.g., Section 1)"
    )
    @GetMapping("/section")
    public ApiResponse<List<QuestionResponse>> getQuestionsBySection(
            @RequestParam Long examPartId,
            @RequestParam String sectionName) {

        return ApiResponse.<List<QuestionResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully fetched section questions")
                .result(questionService.getQuestionsBySection(examPartId, sectionName))
                .build();
    }



    @Operation(
            method = "GET",
            summary = "Get all 4 listening sections",
            description = "Get all questions organized by 4 sections for IELTS listening test"
    )
    @GetMapping("/listening/sections")
    public ApiResponse<Object> getAllListeningSections(
            @RequestParam Long examPartId) {

        return ApiResponse.<Object>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully fetched all listening sections")
                .result(questionService.getAllListeningSections(examPartId))
                .build();
    }

    @Operation(
            method = "GET",
            summary = "Get all speaking questions by examPartId",
            description = "Retrieve all speaking questions for a specific exam part (skillType='SPEAKING'). Returns questions organized by section (part1, part2, part3)."
    )
    @GetMapping("/speaking/exam-part/{examPartId}")
    public ApiResponse<List<QuestionResponse>> getSpeakingQuestions(
            @PathVariable Long examPartId) {

        PageResponse<List<QuestionResponse>> pageResponse = questionService.getQuestionsByExamPart(
                examPartId, 0, 1000, "id:asc"
        );

        return ApiResponse.<List<QuestionResponse>>builder()
                .code(HttpStatus.OK.value())
                .message(String.format("Successfully fetched %d speaking questions", 
                        pageResponse.getTotalElements()))
                .result(pageResponse.getItems())
                .build();
    }

    @Operation(
            method = "GET",
            summary = "Get all questions by examId and skillType",
            description = "Retrieve all questions for a specific exam and skill type (LISTENING, READING, WRITING, SPEAKING). This is a generic API that works for all skill types."
    )
    @GetMapping("/exam/{examId}/skill-type/{skillType}")
    public ApiResponse<List<QuestionResponse>> getQuestionsByExamIdAndSkillType(
            @PathVariable Long examId,
            @PathVariable String skillType,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "1000") int pageSize,
            @RequestParam(defaultValue = "id:asc") String sortBy) {

        // Validate and parse skillType
        SkillType parsedSkillType;
        try {
            parsedSkillType = SkillType.valueOf(skillType.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ApiResponse.<List<QuestionResponse>>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(String.format("Invalid skillType '%s'. Valid values are: LISTENING, READING, WRITING, SPEAKING", skillType))
                    .result(List.of())
                    .build();
        }

        PageResponse<List<QuestionResponse>> pageResponse = questionService.getQuestionsByExamIdAndSkillType(
                examId, parsedSkillType, pageNo, pageSize, sortBy
        );

        return ApiResponse.<List<QuestionResponse>>builder()
                .code(HttpStatus.OK.value())
                .message(String.format("Successfully fetched %d %s questions", 
                        pageResponse.getTotalElements(), parsedSkillType.name().toLowerCase()))
                .result(pageResponse.getItems())
                .build();
    }

    @Operation(
            method = "POST",
            summary = "Upload audio for listening section",
            description = "Upload audio file for a section that already has questions created. Section name must be one of: SECTION1, SECTION2, SECTION3, SECTION4, SECTION5. This will update all questions in the section with the audio URL and transcript."
    )
    @PostMapping("/listening/section/audio")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<List<QuestionResponse>> uploadSectionAudio(
            @RequestParam Long examPartId,
            @RequestParam String sectionName,
            @RequestPart("audio") MultipartFile audioFile,
            @RequestPart(value = "transcript", required = false) String transcript) {

        List<QuestionResponse> responses = questionService.uploadAudioForSection(examPartId, sectionName.toUpperCase(), audioFile, transcript);

        return ApiResponse.<List<QuestionResponse>>builder()
                .code(HttpStatus.OK.value())
                .message(String.format("Successfully uploaded audio for %s (%d questions updated)", 
                        sectionName, responses.size()))
                .result(responses)
                .build();
    }

    @Operation(
            method = "DELETE",
            summary = "Delete question",
            description = "Delete a question by its ID"
    )
    @DeleteMapping("/{questionId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Void> deleteQuestion(@PathVariable Long questionId) {
        questionService.deleteQuestion(questionId);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Question deleted successfully")
                .build();
    }


}
