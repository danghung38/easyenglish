package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.dto.request.UserExamRequest;
import com.dxh.Elearning.dto.response.TestHistoryResponse;
import com.dxh.Elearning.dto.response.UserExamResponse;
import com.dxh.Elearning.entity.*;
import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import com.dxh.Elearning.mapper.UserExamMapper;
import com.dxh.Elearning.repo.*;
import com.dxh.Elearning.service.interfac.ExamService;
import com.dxh.Elearning.service.interfac.UserExamService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserExamServiceImpl implements UserExamService {
    UserExamRepository userExamRepository;
    UserExamMapper userExamMapper;
    UserRepository userRepository;
    ExamRepository examRepository;
    ExamPartRepository examPartRepository;
    UserExamPartRepository userExamPartRepository;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserExamResponse create(UserExamRequest req) {
        User user = checkUser();
        Exam exam = examRepository.findById(req.getExamId())
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_EXISTED));

        // 1️⃣ Tạo user_exam
        UserExam userExam = UserExam.builder()
                .user(user)
                .exam(exam)
                .submitted(false)
                .totalScore(0.0)
                .startedAt(LocalDateTime.now())
                .build();
        userExam = userExamRepository.save(userExam);

        // 2️⃣ Lấy danh sách ExamPart của đề này
        List<ExamPart> examParts = examPartRepository.findAllByExam_Id(req.getExamId());

        // Nếu có skillType cụ thể, chỉ tạo UserExamPart cho part đó
        // Nếu không có skillType (Full Test), tạo tất cả parts
        if (req.getSkillType() != null) {
            examParts = examParts.stream()
                    .filter(part -> part.getSkillType() == req.getSkillType())
                    .collect(Collectors.toList());
            log.info("Creating UserExamPart for specific skill: {}", req.getSkillType());
        } else {
            log.info("Creating UserExamParts for Full Test (all skills)");
        }

        Set<UserExamPart> userExamParts = new HashSet<>();
        for (ExamPart part : examParts) {
            UserExamPart userExamPart = UserExamPart.builder()
                    .userExam(userExam)
                    .skillType(part.getSkillType())
                    .score(0.0)
                    .submitted(false)
                    .build();
            userExamPartRepository.save(userExamPart);
            userExamParts.add(userExamPart);
        }

        userExam.setParts(userExamParts);
        UserExam save = userExamRepository.save(userExam);

        return userExamMapper.toUserExamResponse(save);
    }

    @Transactional
    @Override
    public List<UserExamResponse> getUserExams() {
        User user = checkUser();
        List<UserExam> exams = userExamRepository.findAllByUser_Id(user.getId());
        return exams.stream()
                .map(userExamMapper::toUserExamResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<UserExamResponse> getAllUserExams() {
        // Admin only - get all user exams from all users
        List<UserExam> exams = userExamRepository.findAll();
        return exams.stream()
                .map(userExamMapper::toUserExamResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<UserExamResponse> searchUserExams(String query) {
        // Admin only - search user exams by multiple criteria
        List<UserExam> allExams = userExamRepository.findAll();
        
        if (query == null || query.trim().isEmpty()) {
            return allExams.stream()
                    .map(userExamMapper::toUserExamResponse)
                    .collect(Collectors.toList());
        }
        
        String searchTerm = query.toLowerCase().trim();
        log.info("Searching user exams with query: {}", searchTerm);
        
        return allExams.stream()
                .filter(exam -> {
                    // Search by user ID
                    if (exam.getUser().getId().toString().contains(searchTerm)) {
                        return true;
                    }
                    
                    // Search by username
                    if (exam.getUser().getUsername().toLowerCase().contains(searchTerm)) {
                        return true;
                    }
                    
                    // Search by email
                    if (exam.getUser().getEmail() != null && 
                        exam.getUser().getEmail().toLowerCase().contains(searchTerm)) {
                        return true;
                    }
                    
                    // Search by exam title
                    if (exam.getExam().getTitle().toLowerCase().contains(searchTerm)) {
                        return true;
                    }
                    
                    return false;
                })
                .map(userExamMapper::toUserExamResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public UserExamResponse getUserExamById(Long id) {
        User user = checkUser();
        UserExam userExam = userExamRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_EXAM_NOT_EXISTED));
        
        // Admin can view all exams, regular users can only view their own
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().contains("ADMIN"));
        
        if (!isAdmin && !userExam.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        return userExamMapper.toUserExamResponse(userExam);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserExamResponse> getLeaderboard(int limit) {
        User currentUser = checkUser();
        
        // Get only FULL TESTS (exams with all 4 parts submitted)
        List<UserExam> allExams = userExamRepository.findAll();
        log.info("📊 Total UserExams in database: {}", allExams.size());
        
        List<UserExam> fullTestExams = allExams.stream()
                .filter(exam -> {
                    if (exam.getTotalScore() == null || exam.getTotalScore() <= 0) {
                        return false;
                    }
                    
                    // Count submitted parts
                    long submittedParts = exam.getParts().stream()
                            .filter(UserExamPart::getSubmitted)
                            .count();
                    
                    long totalParts = exam.getParts().size();
                    
                    // Only count as full test if it has exactly 4 parts AND all are submitted
                    boolean isFullTest = totalParts == 4 && submittedParts == 4;
                    
                    if (!isFullTest && submittedParts > 0) {
                        log.debug("⏭️  Skipping partial test - UserExam ID: {}, User: {}, Total Parts: {}, Submitted: {}", 
                            exam.getId(), 
                            exam.getUser().getUsername(),
                            totalParts,
                            submittedParts);
                    }
                    
                    if (isFullTest) {
                        log.debug("✅ Full test found - UserExam ID: {}, User: {}, Score: {}, Parts: {}/{}", 
                            exam.getId(), 
                            exam.getUser().getUsername(), 
                            exam.getTotalScore(),
                            submittedParts,
                            totalParts);
                    }
                    
                    return isFullTest;
                })
                .collect(Collectors.toList());
        
        log.info("🎯 Found {} full test exams (with 4 parts submitted)", fullTestExams.size());
        
        log.info("🎯 Found {} full test exams (with 4 parts submitted)", fullTestExams.size());
        
        // Group by user and calculate average scores (only from full tests)
        var userScores = fullTestExams.stream()
                .collect(Collectors.groupingBy(
                        UserExam::getUser,
                        Collectors.averagingDouble(UserExam::getTotalScore)
                ));
        
        // Count total full tests per user
        var userExamCounts = fullTestExams.stream()
                .collect(Collectors.groupingBy(
                        UserExam::getUser,
                        Collectors.counting()
                ));
        
        log.info("👥 Number of users with full tests: {}", userScores.size());
        userScores.forEach((user, avgScore) -> 
            log.info("  - {}: {} full tests, avg score: {}", 
                user.getUsername(), 
                userExamCounts.get(user), 
                String.format("%.1f", avgScore))
        );
        
        // Create leaderboard entries with rank
        List<UserExamResponse> leaderboard = userScores.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(limit)
                .map(entry -> {
                    User user = entry.getKey();
                    double avgScore = entry.getValue();
                    long totalExams = userExamCounts.getOrDefault(user, 0L);
                    
                    UserExamResponse response = new UserExamResponse();
                    response.setUserId(user.getId());
                    response.setUserName(user.getUsername());
                    response.setFullName(user.getFullName());
                    response.setScore(Math.round(avgScore * 10.0) / 10.0); // Round to 1 decimal
                    response.setTotalTests((int) totalExams);
                    response.setCurrentUser(user.getId().equals(currentUser.getId()));
                    
                    return response;
                })
                .collect(Collectors.toList());
        
        // Add rank
        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).setRank(i + 1);
        }
        
        return leaderboard;
    }

    @Transactional(readOnly = true)
    @Override
    public List<TestHistoryResponse> getTestHistory(int limit) {
        User user = checkUser();
        
        // Get all user exams for current user
        List<UserExam> userExams = userExamRepository.findAllByUser_Id(user.getId());
        
        List<TestHistoryResponse> historyList = new ArrayList<>();
        
        for (UserExam userExam : userExams) {
            // Get all submitted parts (regardless of score)
            List<UserExamPart> submittedParts = userExam.getParts().stream()
                    .filter(UserExamPart::getSubmitted)
                    .collect(Collectors.toList());
            
            log.info("UserExam {}: Found {} submitted parts", userExam.getId(), submittedParts.size());
            submittedParts.forEach(part -> 
                log.info("  - {} submitted, score: {}", part.getSkillType(), part.getScore())
            );
            
            if (submittedParts.isEmpty()) {
                continue; // Skip exams with no submitted parts
            }
            
            // Check if this is a full test (all 4 parts submitted)
            boolean isFullTest = submittedParts.size() == 4;
            
            if (isFullTest) {
                log.info("UserExam {} identified as FULL TEST", userExam.getId());
                
                // Create one entry for full test
                List<TestHistoryResponse.PartScore> partScores = submittedParts.stream()
                        .map(part -> TestHistoryResponse.PartScore.builder()
                                .skillType(part.getSkillType())
                                .score(part.getScore() != null ? part.getScore() : 0.0)
                                .build())
                        .collect(Collectors.toList());
                
                // Calculate average from all submitted parts (use 0 for null scores)
                double avgScore = submittedParts.stream()
                        .mapToDouble(part -> part.getScore() != null ? part.getScore() : 0.0)
                        .average()
                        .orElse(0.0);
                
                TestHistoryResponse history = TestHistoryResponse.builder()
                        .userExamId(userExam.getId())
                        .examId(userExam.getExam().getId())
                        .examName(userExam.getExam().getTitle())
                        .testDate(userExam.getStartedAt())
                        .isFullTest(true)
                        .score(Math.round(avgScore * 10.0) / 10.0)
                        .skillType(null)
                        .partScores(partScores)
                        .build();
                
                historyList.add(history);
            } else {
                log.info("UserExam {} identified as SINGLE PARTS", userExam.getId());
                
                // Create separate entries for individual parts
                // Include all submitted parts, even those with score = 0
                for (UserExamPart part : submittedParts) {
                    double score = part.getScore() != null ? part.getScore() : 0.0;
                    
                    TestHistoryResponse history = TestHistoryResponse.builder()
                            .userExamId(userExam.getId())
                            .examId(userExam.getExam().getId())
                            .userExamPartId(part.getId())
                            .examName(userExam.getExam().getTitle())
                            .testDate(userExam.getStartedAt())
                            .isFullTest(false)
                            .score(score)
                            .skillType(part.getSkillType())
                            .partScores(null)
                            .build();
                    
                    historyList.add(history);
                }
            }
        }
        
        // Sort by date (newest first) and limit results
        return historyList.stream()
                .sorted((h1, h2) -> h2.getTestDate().compareTo(h1.getTestDate()))
                .limit(limit)
                .collect(Collectors.toList());
    }


    private User checkUser(){
        return userRepository.findByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
}
