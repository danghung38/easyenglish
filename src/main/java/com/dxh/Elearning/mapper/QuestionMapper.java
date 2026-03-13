package com.dxh.Elearning.mapper;

import com.dxh.Elearning.dto.response.ExamPartResponse;
import com.dxh.Elearning.dto.response.QuestionResponse;
import com.dxh.Elearning.entity.ExamPart;
import com.dxh.Elearning.entity.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",uses = {OptionMapper.class})
public interface QuestionMapper {

    @Mapping(target = "examPartId", source = "examPart.id")
    @Mapping(target = "correctOptionId", source = "correctOption.id")
    @Mapping(target = "optionRes", source = "options")
    QuestionResponse toQuestionResponse(Question question);

}
