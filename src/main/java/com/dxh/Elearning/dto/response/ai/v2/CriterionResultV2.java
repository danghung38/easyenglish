package com.dxh.Elearning.dto.response.ai.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CriterionResultV2 {
    private Double band;
    private Double confidence;
    private List<String> evidenceIds;
    private String feedback;
}
