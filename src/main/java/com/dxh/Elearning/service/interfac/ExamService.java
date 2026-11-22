package com.dxh.Elearning.service.interfac;

import com.dxh.Elearning.dto.request.ExamRequest;
import com.dxh.Elearning.dto.response.ExamResponse;
import com.dxh.Elearning.dto.response.PageResponse;
import com.dxh.Elearning.dto.response.UserResponse;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface ExamService {
    ExamResponse create(ExamRequest req, MultipartFile image);

    ExamResponse update(Long examId, ExamRequest req, MultipartFile image);

    void delete(Long examId);

    PageResponse<List<ExamResponse>> getAllExamsSortBy(int pageNo, int pageSize, String sortBy);

}
