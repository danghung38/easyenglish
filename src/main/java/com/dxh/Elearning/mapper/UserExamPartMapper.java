package com.dxh.Elearning.mapper;


import com.dxh.Elearning.dto.response.SubmitRLPartResponse;
import com.dxh.Elearning.dto.response.UserExamPartResponse;
import com.dxh.Elearning.entity.UserExamPart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserAnswerMapper.class})
public interface UserExamPartMapper {
    @Mapping(target = "userExamId", source = "userExam.id")
    UserExamPartResponse toUserExamResponse(UserExamPart userExamPart);

    @Mapping(target = "userExamId", source = "userExam.id")
    @Mapping(target = "answers", source = "answers")
    SubmitRLPartResponse toSubmitRLPartResponse(UserExamPart save);
}
