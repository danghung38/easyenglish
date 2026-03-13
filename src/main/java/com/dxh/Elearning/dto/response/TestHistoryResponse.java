package com.dxh.Elearning.dto.response;

import com.dxh.Elearning.enums.SkillType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TestHistoryResponse {
    Long userExamId;
    Long examId;
    Long userExamPartId;  // For single parts
    String examName;
    LocalDateTime testDate;
    Boolean isFullTest;  // true if all 4 parts submitted, false if single part
    Double score;  // average score for full test, or single part score
    SkillType skillType;  // null for full test, specific skill for single part
    List<PartScore> partScores;  // detailed scores for full test
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PartScore {
        SkillType skillType;
        Double score;
    }
}
