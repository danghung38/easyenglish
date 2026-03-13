package com.dxh.Elearning.mapper;

import com.dxh.Elearning.dto.request.ExamRequest;
import com.dxh.Elearning.dto.response.ExamPartResponse;
import com.dxh.Elearning.dto.response.ExamResponse;
import com.dxh.Elearning.entity.Exam;
import com.dxh.Elearning.entity.ExamPart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExamMapper {
    Exam toExam(ExamRequest req);
    
    @Mapping(target = "examParts", source = "parts")
    ExamResponse toExamResponse(Exam exam);
    
    @Mapping(target = "examId", source = "exam.id")
    ExamPartResponse toExamPartResponse(ExamPart examPart);
}
