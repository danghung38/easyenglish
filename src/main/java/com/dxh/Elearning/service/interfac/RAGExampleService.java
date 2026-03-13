package com.dxh.Elearning.service.interfac;

import com.dxh.Elearning.dto.request.RAGExampleRequest;
import com.dxh.Elearning.dto.response.RAGExampleResponse;
import com.dxh.Elearning.dto.response.RAGStatsResponse;

import java.util.List;

public interface RAGExampleService {
    
    /**
     * Get all RAG examples with optional filters
     */
    List<RAGExampleResponse> getAllExamples(String type, Double band, String task);
    
    /**
     * Get a specific example by ID
     */
    RAGExampleResponse getExampleById(String id);
    
    /**
     * Create a new example
     */
    RAGExampleResponse createExample(RAGExampleRequest request);
    
    /**
     * Update an existing example
     */
    RAGExampleResponse updateExample(String id, RAGExampleRequest request);
    
    /**
     * Delete an example
     */
    void deleteExample(String id);
    
    /**
     * Reload AI service (rebuild FAISS index)
     */
    void reloadAIService();
    
    /**
     * Get statistics about examples
     */
    RAGStatsResponse getStatistics();
}
