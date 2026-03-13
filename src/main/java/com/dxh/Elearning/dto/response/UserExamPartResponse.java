package com.dxh.Elearning.dto.response;

import com.dxh.Elearning.enums.SkillType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserExamPartResponse {
    Long id;

    Long userExamId;

    SkillType skillType;         // READING, LISTENING, WRITING, SPEAKING

    Boolean submitted;           // mặc định false

    Double score;
}
