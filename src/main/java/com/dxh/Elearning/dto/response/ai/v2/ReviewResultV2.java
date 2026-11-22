package com.dxh.Elearning.dto.response.ai.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewResultV2 {
    private Boolean required;
    private Boolean performed;
    private List<String> reasonCodes;
}
