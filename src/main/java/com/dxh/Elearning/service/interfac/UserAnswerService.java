package com.dxh.Elearning.service.interfac;

import com.dxh.Elearning.dto.request.UserExamRequest;
import com.dxh.Elearning.dto.response.UserAnswerResponse;
import com.dxh.Elearning.dto.response.UserExamResponse;

import java.util.List;

public interface UserAnswerService {

    List<UserAnswerResponse> getAnswersByUserExamPartId(Long userExamPartId);
}
