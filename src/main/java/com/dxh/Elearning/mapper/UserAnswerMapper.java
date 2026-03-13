package com.dxh.Elearning.mapper;

import com.dxh.Elearning.dto.response.AnswerRLPartResponse;
import com.dxh.Elearning.entity.UserAnswer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {QuestionMapper.class})
public interface UserAnswerMapper {

    @Mapping(target = "questionResponse", source = "question")
    AnswerRLPartResponse toAnswerRLPartResponse(UserAnswer userAnswer);
}
