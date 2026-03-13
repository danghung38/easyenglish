package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.dto.request.AnswerRLPartRequest;
import com.dxh.Elearning.dto.request.SubmitRLPartRequest;
import com.dxh.Elearning.dto.request.SubmitSpeakingExamRequest;
import com.dxh.Elearning.dto.request.SubmitWritingExamRequest;
import com.dxh.Elearning.dto.request.UserExamRequest;
import com.dxh.Elearning.dto.response.SubmitRLPartResponse;
import com.dxh.Elearning.dto.response.UserExamPartResponse;
import com.dxh.Elearning.dto.response.UserExamResponse;
import com.dxh.Elearning.dto.response.WritingResultResponse;
import com.dxh.Elearning.entity.*;
import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import com.dxh.Elearning.mapper.UserExamMapper;
import com.dxh.Elearning.mapper.UserExamPartMapper;
import com.dxh.Elearning.repo.*;
import com.dxh.Elearning.service.interfac.UserExamPartService;
import com.dxh.Elearning.service.interfac.UserExamService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserExamPartServiceImpl implements UserExamPartService {
    UserExamRepository userExamRepository;
    UserRepository userRepository;
    QuestionRepository questionRepository;
    UserExamPartRepository userExamPartRepository;
    UserAnswerRepository userAnswerRepository;
    UserExamPartMapper userExamPartMapper;

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

//    @Transactional(rollbackFor = Exception.class)
//    @Override
//    public SubmitRLPartResponse submitRLPart(SubmitRLPartRequest req) {
//        UserExamPart userExamPart = userExamPartRepository.findById(req.getUserExamPartId())
//                .orElseThrow(() -> new AppException(ErrorCode.USER_EXAM_PART_NOT_EXISTED));
//
//        // 1️⃣ Lưu từng câu trả lời
//        for (AnswerRLPartRequest a : req.getAnswers()) {
//            Question question = questionRepository.findById(a.getQuestionId())
//                    .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
//
//            // Xác định đúng sai
//            boolean isCorrect = question.getCorrectOption() != null &&
//                    question.getCorrectOption().getId().equals(a.getSelectedOptionId());
//
//            double score = isCorrect ? 1.0 : 0.0; // ví dụ mỗi câu đúng = 1 điểm
//
//            UserAnswer ua = UserAnswer.builder()
//                    .userExamPart(userExamPart)
//                    .question(question)
//                    .selectedOptionId(a.getSelectedOptionId())
//                    .score(score)
//                    .build();
//            userAnswerRepository.save(ua);
//        }
//
//        // 2️⃣ Tính tổng điểm phần thi này
//        double totalScore = userAnswerRepository.sumScoreByUserExamPartId(userExamPart.getId());
//        userExamPart.setScore(totalScore);
//        userExamPart.setSubmitted(true);
//
//        UserExamPart save = userExamPartRepository.save(userExamPart);
//
//        // 4️⃣ Cập nhật tổng điểm UserExam
//        double examTotalScore = userExamPartRepository.sumScoreByUserExamId(userExamPart.getUserExam().getId());
//        UserExam userExam = userExamPart.getUserExam();
//        userExam.setTotalScore(examTotalScore);
//        userExamRepository.save(userExam);
//
//        // 3️⃣ Trả về response
//        return userExamPartMapper.toSubmitRLPartResponse(save);
//    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public SubmitRLPartResponse submitRLPart(SubmitRLPartRequest req) {
        UserExamPart userExamPart = userExamPartRepository.findById(req.getUserExamPartId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_EXAM_PART_NOT_EXISTED));

        //fix: Xóa answers cũ trước khi thêm mới
        userAnswerRepository.deleteByUserExamPartId(userExamPart.getId());
        userExamPartRepository.flush(); // Đồng bộ với DB ngay lập tức

        // 1️⃣ Lưu từng câu trả lời
        for (AnswerRLPartRequest a : req.getAnswers()) {
            Question question = questionRepository.findById(a.getQuestionId())
                    .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

            // Xác định đúng sai
            boolean isCorrect = question.getCorrectOption() != null &&
                    question.getCorrectOption().getId().equals(a.getSelectedOptionId());

            double score = isCorrect ? 1.0 : 0.0; // ví dụ mỗi câu đúng = 1 điểm

            UserAnswer ua = UserAnswer.builder()
                    .userExamPart(userExamPart)
                    .question(question)
                    .selectedOptionId(a.getSelectedOptionId())
                    .score(score)
                    .build();
            userAnswerRepository.save(ua);
        }

        // 2️⃣ Tính tổng điểm phần thi này (số câu đúng)
        double rawScore = userAnswerRepository.sumScoreByUserExamPartId(userExamPart.getId());
        
        // 2.1️⃣ Chuyển đổi sang IELTS band score (0-9)
        double bandScore = convertRawScoreToBandScore((int) rawScore);
        log.info("Reading/Listening score conversion: {} correct answers -> Band {}", (int) rawScore, bandScore);
        
        userExamPart.setScore(bandScore);
        userExamPart.setSubmitted(true);

        UserExamPart save = userExamPartRepository.save(userExamPart);

        // 4️⃣ Cập nhật tổng điểm UserExam (chỉ khi là full test)
        UserExam userExam = userExamPart.getUserExam();
        if (userExam != null) {
            // Tính AVERAGE score thay vì SUM (vì giờ tất cả parts đều là band score 0-9)
            double examAverageScore = userExamPartRepository.calculateAverageScoreByUserExamId(userExam.getId());
            userExam.setTotalScore(examAverageScore);
            
            // Kiểm tra nếu tất cả parts đã submit thì cập nhật UserExam
            checkAndUpdateUserExamSubmission(userExam);
            userExamRepository.save(userExam);
        }

        // 3️⃣ Trả về response
        return userExamPartMapper.toSubmitRLPartResponse(save);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void submitSpeakingExam(SubmitSpeakingExamRequest req) {
        log.info("Submitting Speaking exam for userExamPartId: {}", req.getUserExamPartId());
        
        UserExamPart userExamPart = userExamPartRepository.findById(req.getUserExamPartId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_EXAM_PART_NOT_EXISTED));
        
        // Round overall band to nearest 0.5 (IELTS standard)
        double roundedBand = Math.round(req.getOverallBand() * 2.0) / 2.0;
        
        // Update UserExamPart with final overall score
        userExamPart.setScore(roundedBand);
        userExamPart.setSubmitted(true);
        userExamPartRepository.save(userExamPart);
        
        log.info("✅ Speaking exam submitted successfully. Overall band: {} -> rounded: {}", 
                req.getOverallBand(), roundedBand);
        
        // 🎯 UPDATE: Cập nhật điểm cho tất cả các câu trong từng part
        // Part 1: Cập nhật tất cả câu part1 với điểm part1Score
        if (req.getPart1Score() != null) {
            updatePartQuestionScores(userExamPart.getId(), "part1", req.getPart1Score());
        }
        
        // Part 2: Cập nhật tất cả câu part2 với điểm part2Score
        if (req.getPart2Score() != null) {
            updatePartQuestionScores(userExamPart.getId(), "part2", req.getPart2Score());
        }
        
        // Part 3: Cập nhật tất cả câu part3 với điểm part3Score
        if (req.getPart3Score() != null) {
            updatePartQuestionScores(userExamPart.getId(), "part3", req.getPart3Score());
        }
        
        // Update UserExam total score (chỉ khi là full test)
        UserExam userExam = userExamPart.getUserExam();
        if (userExam != null) {
            // Tính AVERAGE score thay vì SUM (vì giờ tất cả parts đều là band score 0-9)
            double examAverageScore = userExamPartRepository.calculateAverageScoreByUserExamId(userExam.getId());
            userExam.setTotalScore(examAverageScore);
            
            // Kiểm tra nếu tất cả parts đã submit thì cập nhật UserExam
            checkAndUpdateUserExamSubmission(userExam);
            userExamRepository.save(userExam);
            
            log.info("UserExam average score updated: {}", examAverageScore);
        } else {
            log.info("✅ Speaking exam submitted as standalone part (not full test)");
        }
    }
    
    /**
     * Cập nhật điểm của tất cả các câu trong một part
     * @param userExamPartId ID của UserExamPart
     * @param section Tên section (part1, part2, part3)
     * @param score Điểm của part
     */
    private void updatePartQuestionScores(Long userExamPartId, String section, Double score) {
        if (score == null) {
            log.warn("⚠️ Score is null for section: {}", section);
            return;
        }
        
        log.info("📝 Updating scores for section: {} with score: {}", section, score);
        
        // Lấy tất cả UserAnswer của part này và section cụ thể
        List<UserAnswer> userAnswers = userAnswerRepository.findAll().stream()
                .filter(ua -> ua.getUserExamPart().getId().equals(userExamPartId))
                .filter(ua -> ua.getQuestion() != null)
                .filter(ua -> section.equalsIgnoreCase(ua.getQuestion().getSection()))
                .toList();
        
        int updatedCount = 0;
        for (UserAnswer userAnswer : userAnswers) {
            // Cập nhật điểm cho từng câu = điểm của part đó
            userAnswer.setScore(score);
            userAnswer.setAiScore(score);
            userAnswerRepository.save(userAnswer);
            updatedCount++;
            
            log.debug("  ✅ Updated score for questionId: {} ({}) -> {}", 
                userAnswer.getQuestion().getId(), 
                userAnswer.getQuestion().getSection(),
                score);
        }
        
        log.info("✅ Updated {} questions in {} with score: {}", updatedCount, section, score);
    }

//    @Transactional(rollbackFor = Exception.class)
//    @Override
//    public WritingResultResponse submitWritingExam(SubmitWritingExamRequest req) {
//        log.info("Submitting Writing exam for userExamPartId: {}", req.getUserExamPartId());
//
//        UserExamPart userExamPart = userExamPartRepository.findById(req.getUserExamPartId())
//                .orElseThrow(() -> new AppException(ErrorCode.USER_EXAM_PART_NOT_EXISTED));
//
//        // Delete old answers before adding new ones
//        userAnswerRepository.deleteByUserExamPartId(userExamPart.getId());
//        userExamPartRepository.flush();
//
//        // Save each task result as UserAnswer
//        for (SubmitWritingExamRequest.WritingTaskResult taskResult : req.getTaskResults()) {
//            Question question = questionRepository.findById(taskResult.getQuestionId())
//                    .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
//
//            // Parse detailedFeedback if it's a JSON string containing all criteria feedback
//            String taskAchievementFeedback = "";
//            String coherenceCohesionFeedback = "";
//            String lexicalResourceFeedback = "";
//            String grammaticalRangeFeedback = "";
//
//            String detailedFeedback = taskResult.getDetailedFeedback();
//            log.info("📝 Received detailedFeedback: {}", detailedFeedback);
//
//            if (detailedFeedback != null && detailedFeedback.startsWith("{")) {
//                try {
//                    // It's a JSON string, parse it
//                    ObjectMapper mapper = new ObjectMapper();
//                    JsonNode feedbackNode = mapper.readTree(detailedFeedback);
//
//                    taskAchievementFeedback = feedbackNode.has("task_achievement")
//                        ? feedbackNode.get("task_achievement").asText()
//                        : feedbackNode.has("taskAchievement") ? feedbackNode.get("taskAchievement").asText() : "";
//
//                    coherenceCohesionFeedback = feedbackNode.has("coherence_cohesion")
//                        ? feedbackNode.get("coherence_cohesion").asText()
//                        : feedbackNode.has("coherenceCohesion") ? feedbackNode.get("coherenceCohesion").asText() : "";
//
//                    lexicalResourceFeedback = feedbackNode.has("lexical_resource")
//                        ? feedbackNode.get("lexical_resource").asText()
//                        : feedbackNode.has("lexicalResource") ? feedbackNode.get("lexicalResource").asText() : "";
//
//                    grammaticalRangeFeedback = feedbackNode.has("grammatical_range_accuracy")
//                        ? feedbackNode.get("grammatical_range_accuracy").asText()
//                        : feedbackNode.has("grammatical_range")
//                        ? feedbackNode.get("grammatical_range").asText()
//                        : feedbackNode.has("grammaticalRange") ? feedbackNode.get("grammaticalRange").asText() : "";
//
//                    log.info("✅ Parsed feedback - TA: {}, CC: {}, LR: {}, GR: {}",
//                        taskAchievementFeedback.substring(0, Math.min(50, taskAchievementFeedback.length())),
//                        coherenceCohesionFeedback.substring(0, Math.min(50, coherenceCohesionFeedback.length())),
//                        lexicalResourceFeedback.substring(0, Math.min(50, lexicalResourceFeedback.length())),
//                        grammaticalRangeFeedback.substring(0, Math.min(50, grammaticalRangeFeedback.length())));
//
//                } catch (Exception e) {
//                    log.warn("Failed to parse detailedFeedback as JSON, using as plain text", e);
//                    taskAchievementFeedback = detailedFeedback;
//                }
//            } else {
//                // Plain text, use for all
//                taskAchievementFeedback = detailedFeedback != null ? detailedFeedback : "";
//            }
//
//            // Create detailed feedback JSON with parsed criteria
//            String detailedFeedbackJson = String.format(
//                "{\"taskAchievement\":%.1f,\"coherenceCohesion\":%.1f,\"lexicalResource\":%.1f,\"grammaticalRange\":%.1f," +
//                "\"detailedFeedback\":{\"taskAchievement\":\"%s\",\"coherenceCohesion\":\"%s\",\"lexicalResource\":\"%s\",\"grammaticalRange\":\"%s\"}," +
//                "\"examinerFeedback\":\"%s\"}",
//                taskResult.getTaskAchievement(),
//                taskResult.getCoherenceCohesion(),
//                taskResult.getLexicalResource(),
//                taskResult.getGrammaticalRange(),
//                escapeJson(taskAchievementFeedback),
//                escapeJson(coherenceCohesionFeedback),
//                escapeJson(lexicalResourceFeedback),
//                escapeJson(grammaticalRangeFeedback),
//                escapeJson(taskResult.getExaminerFeedback() != null ? taskResult.getExaminerFeedback() : "")
//            );
//
//            UserAnswer ua = UserAnswer.builder()
//                    .userExamPart(userExamPart)
//                    .question(question)
//                    .selectedOptionId(null) // Writing doesn't have selected option
//                    .answerText(taskResult.getEssayText())
//                    .score(taskResult.getOverallBand())
//                    .aiScore(taskResult.getOverallBand())
//                    .aiFeedback(detailedFeedbackJson)
//                    .build();
//
//            userAnswerRepository.save(ua);
//            log.info("Saved Writing answer for questionId: {}, band: {}",
//                taskResult.getQuestionId(), taskResult.getOverallBand());
//        }
//
//        // Round overall band to nearest 0.5 (IELTS standard)
//        double roundedBand = Math.round(req.getOverallBand() * 2.0) / 2.0;
//
//        // Update UserExamPart with overall Writing band
//        userExamPart.setScore(roundedBand);
//        userExamPart.setSubmitted(true);
//        userExamPartRepository.save(userExamPart);
//
//        log.info("✅ Writing exam submitted successfully. Overall band: {} -> rounded: {}",
//                req.getOverallBand(), roundedBand);
//
//        // Update UserExam total score (chỉ khi là full test)
//        UserExam userExam = userExamPart.getUserExam();
//        if (userExam != null) {
//            // Tính AVERAGE score thay vì SUM (vì giờ tất cả parts đều là band score 0-9)
//            double examAverageScore = userExamPartRepository.calculateAverageScoreByUserExamId(userExam.getId());
//            userExam.setTotalScore(examAverageScore);
//
//            // Kiểm tra nếu tất cả parts đã submit thì cập nhật UserExam
//            checkAndUpdateUserExamSubmission(userExam);
//            userExamRepository.save(userExam);
//
//            log.info("UserExam average score updated: {}", examAverageScore);
//        } else {
//            log.info("✅ Writing exam submitted as standalone part (not full test)");
//        }
//
//
//    }
//


    @Transactional(rollbackFor = Exception.class)
    @Override
    public WritingResultResponse submitWritingExam(SubmitWritingExamRequest req) {

        log.info("Submitting Writing exam for userExamPartId: {}", req.getUserExamPartId());

        UserExamPart userExamPart = userExamPartRepository.findById(req.getUserExamPartId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_EXAM_PART_NOT_EXISTED));

        // Xoá answer cũ
        userAnswerRepository.deleteByUserExamPartId(userExamPart.getId());
        userExamPartRepository.flush();

        SubmitWritingExamRequest.WritingTaskResult firstTask = null;

        for (SubmitWritingExamRequest.WritingTaskResult taskResult : req.getTaskResults()) {

            if (firstTask == null) {
                firstTask = taskResult;
            }

            Question question = questionRepository.findById(taskResult.getQuestionId())
                    .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

            String taskAchievementFeedback = "";
            String coherenceCohesionFeedback = "";
            String lexicalResourceFeedback = "";
            String grammaticalRangeFeedback = "";

            String detailedFeedback = taskResult.getDetailedFeedback();

            if (detailedFeedback != null && detailedFeedback.startsWith("{")) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode feedbackNode = mapper.readTree(detailedFeedback);

                    taskAchievementFeedback = feedbackNode.has("task_achievement")
                            ? feedbackNode.get("task_achievement").asText()
                            : "";

                    coherenceCohesionFeedback = feedbackNode.has("coherence_cohesion")
                            ? feedbackNode.get("coherence_cohesion").asText()
                            : "";

                    lexicalResourceFeedback = feedbackNode.has("lexical_resource")
                            ? feedbackNode.get("lexical_resource").asText()
                            : "";

                    grammaticalRangeFeedback = feedbackNode.has("grammatical_range_accuracy")
                            ? feedbackNode.get("grammatical_range_accuracy").asText()
                            : "";

                } catch (Exception e) {
                    log.warn("Failed to parse detailedFeedback JSON", e);
                    taskAchievementFeedback = detailedFeedback;
                }
            } else {
                taskAchievementFeedback = detailedFeedback != null ? detailedFeedback : "";
            }

            String detailedFeedbackJson = String.format(
                    "{\"taskAchievement\":%.1f," +
                            "\"coherenceCohesion\":%.1f," +
                            "\"lexicalResource\":%.1f," +
                            "\"grammaticalRange\":%.1f," +
                            "\"detailedFeedback\":{" +
                            "\"taskAchievement\":\"%s\"," +
                            "\"coherenceCohesion\":\"%s\"," +
                            "\"lexicalResource\":\"%s\"," +
                            "\"grammaticalRange\":\"%s\"}," +
                            "\"examinerFeedback\":\"%s\"}",
                    taskResult.getTaskAchievement(),
                    taskResult.getCoherenceCohesion(),
                    taskResult.getLexicalResource(),
                    taskResult.getGrammaticalRange(),
                    escapeJson(taskAchievementFeedback),
                    escapeJson(coherenceCohesionFeedback),
                    escapeJson(lexicalResourceFeedback),
                    escapeJson(grammaticalRangeFeedback),
                    escapeJson(taskResult.getExaminerFeedback() != null ? taskResult.getExaminerFeedback() : "")
            );

            UserAnswer ua = UserAnswer.builder()
                    .userExamPart(userExamPart)
                    .question(question)
                    .answerText(taskResult.getEssayText())
                    .score(taskResult.getOverallBand())
                    .aiScore(taskResult.getOverallBand())
                    .aiFeedback(detailedFeedbackJson)
                    .build();

            userAnswerRepository.save(ua);

            log.info("Saved Writing answer for questionId: {}, band: {}",
                    taskResult.getQuestionId(), taskResult.getOverallBand());
        }

        // Round band theo IELTS (0.5)
        double roundedBand = Math.round(req.getOverallBand() * 2.0) / 2.0;

        userExamPart.setScore(roundedBand);
        userExamPart.setSubmitted(true);
        userExamPartRepository.save(userExamPart);

        // Update UserExam nếu là full test
        UserExam userExam = userExamPart.getUserExam();
        if (userExam != null) {
            double examAverageScore =
                    userExamPartRepository.calculateAverageScoreByUserExamId(userExam.getId());

            userExam.setTotalScore(examAverageScore);
            checkAndUpdateUserExamSubmission(userExam);
            userExamRepository.save(userExam);

            log.info("UserExam average score updated: {}", examAverageScore);
        }

        log.info("Writing exam submitted successfully. Overall band rounded: {}", roundedBand);

        // ✅ RETURN cho Android
        return WritingResultResponse.builder()
                .overallBand(roundedBand)
                .taskAchievement(firstTask != null ? firstTask.getTaskAchievement() : 0.0)
                .coherenceCohesion(firstTask != null ? firstTask.getCoherenceCohesion() : 0.0)
                .lexicalResource(firstTask != null ? firstTask.getLexicalResource() : 0.0)
                .grammaticalRange(firstTask != null ? firstTask.getGrammaticalRange() : 0.0)
                .detailedFeedback(firstTask != null ? firstTask.getDetailedFeedback() : "")
                .examinerFeedback(firstTask != null ? firstTask.getExaminerFeedback() : "")
                .build();
    }


    /**
     * Kiểm tra và cập nhật trạng thái submitted của UserExam nếu tất cả parts đã submit
     */
    private void checkAndUpdateUserExamSubmission(UserExam userExam) {
        // Lấy tất cả parts của UserExam
        List<UserExamPart> allParts = userExamPartRepository.findByUserExamId(userExam.getId());
        
        // Kiểm tra nếu tất cả parts đã submitted
        boolean allPartsSubmitted = allParts.stream()
                .allMatch(part -> Boolean.TRUE.equals(part.getSubmitted()));
        
        if (allPartsSubmitted && allParts.size() > 0) {
            userExam.setSubmitted(true);
            userExam.setSubmittedAt(LocalDateTime.now());
            log.info("✅ All parts submitted! UserExam {} marked as submitted at {}", 
                userExam.getId(), userExam.getSubmittedAt());
        } else {
            log.info("⏳ Not all parts submitted yet. Submitted: {}/{}", 
                allParts.stream().filter(p -> Boolean.TRUE.equals(p.getSubmitted())).count(),
                allParts.size());
        }
    }
    
    /**
     * Escape special characters for JSON string
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

}
