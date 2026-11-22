package com.dxh.Elearning.mapper;

import com.dxh.Elearning.dto.response.UserExamResponse;
import com.dxh.Elearning.entity.UserExam;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",uses = {UserExamPartMapper.class})
public interface UserExamMapper {

    @Mapping(target = "examId", source = "exam.id")
    @Mapping(target = "userId", source = "user.id")
    UserExamResponse toUserExamResponse(UserExam userExam);
}
