package com.dxh.Elearning.service.interfac;

import com.dxh.Elearning.dto.response.IELTSWritingResponse;

/**
 * Service for IELTS Writing evaluation using IELTSensei AI
 */
public interface IELTSWritingService {
    
    /**
     * Evaluate writing essay by question ID
     * 
     * @param essayText Student's essay
     * @param questionId ID of the writing question
     * @param wordCount Number of words in essay
     * @param duration Time taken in seconds
     * @return IELTS band scores and detailed feedback
     */
    IELTSWritingResponse evaluateWritingByQuestion(String essayText, Long questionId, Integer wordCount, Integer duration);
    
    /**
     * Evaluate writing with custom question
     * 
     * @param essayText Student's essay
     * @param questionText Writing task/question
     * @param task Task type (task1 or task2)
     * @param wordCount Number of words
     * @param duration Time taken in seconds
     * @return IELTS band scores and detailed feedback
     */
    IELTSWritingResponse evaluateWriting(String essayText, String questionText, String task, Integer wordCount, Integer duration);
}
