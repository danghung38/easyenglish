package com.dxh.Elearning.dto.request;


import com.dxh.Elearning.enums.SkillType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionRequest {


    @NotNull(message = "INVALID_NULL")
    Long examPartId;

    @NotNull(message = "INVALID_NULL")
    SkillType skillType;

    String content; // optional, especially for Reading questions (can be blank)

    String audioUrl; // optional, cho Listening

    String section; // optional, IELTS section (e.g., "Section 1", "Section 2")

    String explain; // optional, giải thích đáp án

    @NotNull(message = "INVALID_NULL")
    Double maxScore;

    List<OptionRequest> options; // Required cho READING/LISTENING (trắc nghiệm)

    String correctTempId; // tempId của đáp án đúng - cho câu trắc nghiệm
}
