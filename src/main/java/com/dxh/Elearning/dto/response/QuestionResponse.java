package com.dxh.Elearning.dto.response;

import com.dxh.Elearning.enums.SkillType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionResponse {
    Long id;
    Long examPartId;
    SkillType skillType;
    Boolean isSection; // true nếu đây là section instruction
    String content;
    String audioUrl;
    String imageUrl; // For Reading sections - image URL from S3
    Double maxScore;
    Long correctOptionId; // Đáp án đúng cho câu trắc nghiệm (READING/LISTENING)
    List<OptionResponse> optionRes;
    String section; // Section name for IELTS
    String explain; // Giải thích đáp án
    String transcript; // Transcript cho audio của listening section
}
