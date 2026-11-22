package com.dxh.Elearning.service.interfac;

import com.dxh.Elearning.dto.request.ExamPartRequest;
import com.dxh.Elearning.dto.response.ExamPartResponse;
import com.dxh.Elearning.dto.response.PageResponse;

import java.util.List;

public interface ExamPartService {
    ExamPartResponse create(ExamPartRequest req);
    
    ExamPartResponse update(Long examPartId, ExamPartRequest req);

    List<ExamPartResponse> findAllByExamId(Long examId);
    
    PageResponse<List<ExamPartResponse>> getAllExamPartsByExamId(Long examId, Integer pageNo, Integer pageSize, String sortBy);

    void delete(Long examPartId);
}
