package com.dxh.Elearning.dto.response.ai.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionSnapshotV2 {
    private String model;
    private String rubric;
    private String prompts;
    private String retrievalIndex;
}
