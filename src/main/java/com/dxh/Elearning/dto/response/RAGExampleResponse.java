package com.dxh.Elearning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RAGExampleResponse {
    private String id;
    private String text;
    private Map<String, Object> evaluation;
    private Map<String, Object> metadata;
}
