package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.dto.request.UserExamRequest;
import com.dxh.Elearning.dto.response.UserAnswerResponse;
import com.dxh.Elearning.dto.response.UserExamResponse;
import com.dxh.Elearning.entity.*;
import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import com.dxh.Elearning.mapper.QuestionMapper;
import com.dxh.Elearning.mapper.UserExamMapper;
import com.dxh.Elearning.repo.*;
import com.dxh.Elearning.service.interfac.UserAnswerService;
import com.dxh.Elearning.service.interfac.UserExamService;
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
public class UserAnswerServiceImpl implements UserAnswerService {
    UserAnswerRepository userAnswerRepository;
    QuestionMapper questionMapper;


    @Override
    @Transactional(readOnly = true)
    public List<UserAnswerResponse> getAnswersByUserExamPartId(Long userExamPartId) {
        List<UserAnswer> answers = userAnswerRepository.findAllByUserExamPart_Id(userExamPartId);

        return answers.stream().map(a -> UserAnswerResponse.builder()
                        .id(a.getId())
                        .question(questionMapper.toQuestionResponse(a.getQuestion()))
                        .selectedOptionId(a.getSelectedOptionId())
                        .answerText(a.getAnswerText())
                        .audioUrl(a.getAudioUrl())
                        .score(a.getScore())
                        .aiScore(a.getAiScore())
                        .aiFeedback(a.getAiFeedback())
                        .build())
                .collect(Collectors.toList());
    }
}
