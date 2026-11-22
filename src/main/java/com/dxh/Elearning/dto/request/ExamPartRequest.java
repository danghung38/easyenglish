package com.dxh.Elearning.dto.request;


import com.dxh.Elearning.enums.SkillType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamPartRequest {

    @NotNull(message = "INVALID_NULL")
    Long examId;               // id của Exam mà phần thi này thuộc về

    SkillType skillType;       // READING, LISTENING, WRITING, SPEAKING

    @NotNull(message = "INVALID_NULL")
    Integer duration; // phút
}
