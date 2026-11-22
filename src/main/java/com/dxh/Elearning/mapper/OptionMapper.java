package com.dxh.Elearning.mapper;

import com.dxh.Elearning.dto.response.ExamPartResponse;
import com.dxh.Elearning.dto.response.OptionResponse;
import com.dxh.Elearning.entity.ExamPart;
import com.dxh.Elearning.entity.Option;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OptionMapper {
    OptionResponse toOptionResponse(Option option);
}
