package com.dxh.Elearning.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WritingResultResponse {

    private Double overallBand;
    private Double taskAchievement;
    private Double coherenceCohesion;
    private Double lexicalResource;
    private Double grammaticalRange;

    private String detailedFeedback;
    private String examinerFeedback;
}

