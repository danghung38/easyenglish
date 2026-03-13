package com.dxh.Elearning.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RAGStatsResponse {
    private SpeakingStats speaking;
    private WritingStats writing;
    private Integer total;
    
    @JsonProperty("last_loaded")
    private String lastLoaded;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpeakingStats {
        private Integer total;
        
        @JsonProperty("by_band")
        private Map<String, Integer> byBand;
        
        @JsonProperty("by_part")
        private Map<String, Integer> byPart;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WritingStats {
        private Integer total;
        
        @JsonProperty("by_band")
        private Map<String, Integer> byBand;
        
        @JsonProperty("by_task")
        private Map<String, Integer> byTask;
    }
}
