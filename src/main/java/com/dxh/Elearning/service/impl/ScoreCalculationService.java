package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.entity.UserAnswer;
import com.dxh.Elearning.entity.UserExamPart;
import com.dxh.Elearning.enums.SkillType;
import com.dxh.Elearning.repo.UserExamPartRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service để tính toán điểm tổng cho UserExamPart
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ScoreCalculationService {

    UserExamPartRepository userExamPartRepository;

    /**
     * Tính và cập nhật điểm tổng cho một UserExamPart
     * Điểm được tính dựa trên:
     * - Multiple Choice questions: score (auto-graded) -> convert to IELTS band
     * - Writing/Speaking questions: aiScore (AI-graded) -> already band score
     */
    @Transactional
    public void calculateAndUpdateScore(Long userExamPartId) {
        UserExamPart userExamPart = userExamPartRepository.findById(userExamPartId)
                .orElseThrow(() -> new IllegalArgumentException("UserExamPart not found"));

        List<UserAnswer> answers = userExamPart.getAnswers();
        
        if (answers == null || answers.isEmpty()) {
            log.warn("No answers found for UserExamPart: {}", userExamPartId);
            userExamPart.setScore(0.0);
            userExamPartRepository.save(userExamPart);
            return;
        }

        SkillType skillType = answers.get(0).getQuestion().getSkillType();
        
        if (skillType == SkillType.READING || skillType == SkillType.LISTENING) {
            // Reading/Listening: Đếm số câu đúng và convert sang band score
            int correctAnswers = 0;
            
            for (UserAnswer answer : answers) {
                if (answer.getScore() != null && answer.getScore() > 0) {
                    correctAnswers++;
                }
            }
            
            // Convert số câu đúng sang IELTS band score
            double bandScore = convertRawScoreToBandScore(correctAnswers);
            userExamPart.setScore(bandScore);
            
            log.info("Updated R/L score for UserExamPart {}: {} correct answers -> Band {}", 
                     userExamPartId, correctAnswers, bandScore);
            
        } else if (skillType == SkillType.WRITING || skillType == SkillType.SPEAKING) {
            // Writing/Speaking: Tính trung bình aiScore (đã là band score)
            double totalScore = 0.0;
            int answeredQuestions = 0;
            
            for (UserAnswer answer : answers) {
                if (answer.getAiScore() != null) {
                    totalScore += answer.getAiScore();
                    answeredQuestions++;
                }
            }
            
            double averageScore = answeredQuestions > 0 ? totalScore / answeredQuestions : 0.0;
            userExamPart.setScore(averageScore);
            
            log.info("Updated W/S score for UserExamPart {}: {} (from {} answers)", 
                     userExamPartId, averageScore, answeredQuestions);
        }
        
        userExamPartRepository.save(userExamPart);
    }
    
    /**
     * Convert raw score (number of correct answers) to IELTS band score (0-9)
     * Based on standard IELTS Reading/Listening conversion table for 20 questions
     */
    private double convertRawScoreToBandScore(int correctAnswers) {
        if (correctAnswers >= 20) return 9.0;
        if (correctAnswers == 19) return 8.5;
        if (correctAnswers == 18) return 8.0;
        if (correctAnswers == 17) return 7.5;
        if (correctAnswers == 16) return 7.0;
        if (correctAnswers == 15) return 6.5;
        if (correctAnswers == 14) return 6.0;
        if (correctAnswers == 13) return 5.5;
        if (correctAnswers == 12) return 5.0;
        if (correctAnswers == 11) return 4.5;
        if (correctAnswers == 10) return 4.0;
        if (correctAnswers == 9) return 3.5;
        if (correctAnswers == 8) return 3.0;
        if (correctAnswers >= 6) return 2.5;
        if (correctAnswers >= 4) return 2.0;
        if (correctAnswers >= 2) return 1.5;
        if (correctAnswers == 1) return 1.0;
        return 0.0;
    }

    /**
     * Kiểm tra xem UserExamPart đã hoàn thành chưa
     * (tất cả câu hỏi đã được chấm điểm)
     */
    public boolean isCompleted(Long userExamPartId) {
        UserExamPart userExamPart = userExamPartRepository.findById(userExamPartId)
                .orElseThrow(() -> new IllegalArgumentException("UserExamPart not found"));

        List<UserAnswer> answers = userExamPart.getAnswers();
        
        if (answers == null || answers.isEmpty()) {
            return false;
        }

        for (UserAnswer answer : answers) {
            SkillType skillType = answer.getQuestion().getSkillType();
            
            if (skillType == SkillType.READING || skillType == SkillType.LISTENING) {
                if (answer.getScore() == null) {
                    return false;
                }
            } else if (skillType == SkillType.WRITING || skillType == SkillType.SPEAKING) {
                if (answer.getAiScore() == null) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Lấy tiến độ hoàn thành (%)
     */
    public double getCompletionPercentage(Long userExamPartId) {
        UserExamPart userExamPart = userExamPartRepository.findById(userExamPartId)
                .orElseThrow(() -> new IllegalArgumentException("UserExamPart not found"));

        List<UserAnswer> answers = userExamPart.getAnswers();
        
        if (answers == null || answers.isEmpty()) {
            return 0.0;
        }

        int totalQuestions = answers.size();
        int gradedQuestions = 0;

        for (UserAnswer answer : answers) {
            SkillType skillType = answer.getQuestion().getSkillType();
            
            if ((skillType == SkillType.READING || skillType == SkillType.LISTENING) && answer.getScore() != null) {
                gradedQuestions++;
            } else if ((skillType == SkillType.WRITING || skillType == SkillType.SPEAKING) 
                       && answer.getAiScore() != null) {
                gradedQuestions++;
            }
        }

        return (gradedQuestions * 100.0) / totalQuestions;
    }
}
