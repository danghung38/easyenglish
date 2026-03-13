package com.dxh.Elearning.dto.response;

import com.dxh.Elearning.enums.SkillType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamPartResponse {
    Long id;                   // id của ExamPart sinh ra
    Long examId;               // id của Exam cha
    SkillType skillType;
    Integer duration;
}
