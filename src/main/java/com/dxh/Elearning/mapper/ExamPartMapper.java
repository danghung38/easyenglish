package com.dxh.Elearning.mapper;

import com.dxh.Elearning.dto.response.ExamPartResponse;
import com.dxh.Elearning.entity.ExamPart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExamPartMapper {

    @Mapping(target = "examId", source = "exam.id")
    ExamPartResponse toExamPartResponse(ExamPart examPart);
}
