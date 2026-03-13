package com.dxh.Elearning.service.interfac;

import com.dxh.Elearning.dto.response.IELTSSpeakingResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for IELTS Speaking evaluation using IELTSensei AI
 */
public interface IELTSSpeakingService {
    
    /**
     * Evaluate speaking response by question ID
     * Transcribes audio and evaluates with IELTS criteria
     * Results are saved to UserAnswer table
     * 
     * @param audioFile Audio file of student's response
     * @param questionId ID of the question
     * @param userExamPartId ID of the UserExamPart to save the answer
     * @param audioUrl Optional audio URL (if already uploaded)
     * @return IELTS band scores and detailed feedback
     */
    IELTSSpeakingResponse evaluateSpeakingByQuestion(MultipartFile audioFile, Long questionId, Long userExamPartId, String audioUrl);
    
    /**
     * Evaluate speaking with custom question text
     * 
     * @param audioFile Audio file of student's response
     * @param questionText Question or topic
     * @param part IELTS Speaking part (part1, part2, part3)
     * @return IELTS band scores and detailed feedback
     */
    IELTSSpeakingResponse evaluateSpeaking(MultipartFile audioFile, String questionText, String part);
}
