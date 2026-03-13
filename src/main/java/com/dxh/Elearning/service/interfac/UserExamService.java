package com.dxh.Elearning.service.interfac;

import com.dxh.Elearning.dto.request.UserExamRequest;
import com.dxh.Elearning.dto.response.TestHistoryResponse;
import com.dxh.Elearning.dto.response.UserExamResponse;

import java.util.List;

public interface UserExamService {

    UserExamResponse create(UserExamRequest req);

    List<UserExamResponse> getUserExams();

    List<UserExamResponse> getAllUserExams();

    List<UserExamResponse> searchUserExams(String query);

    UserExamResponse getUserExamById(Long id);

    List<UserExamResponse> getLeaderboard(int limit);
    
    List<TestHistoryResponse> getTestHistory(int limit);
}
