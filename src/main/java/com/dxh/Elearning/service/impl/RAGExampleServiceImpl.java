package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.dto.request.RAGExampleRequest;
import com.dxh.Elearning.dto.response.RAGExampleResponse;
import com.dxh.Elearning.dto.response.RAGStatsResponse;
import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import com.dxh.Elearning.service.interfac.RAGExampleService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RAGExampleServiceImpl implements RAGExampleService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ielts-sensei.rag-examples.path:py_service/pyhton_service/rag_examples.json}")
    private String ragExamplesPath;

    @Value("${ielts-sensei.service.url:http://localhost:5000}")
    private String ieltsSenseiUrl;

    @Override
    public List<RAGExampleResponse> getAllExamples(String type, Double band, String task) {
        try {
            Map<String, List<Map<String, Object>>> data = readRagFile();
            
            List<RAGExampleResponse> allExamples = new ArrayList<>();
            
            // Convert speaking examples
            if (type == null || "speaking".equals(type)) {
                List<Map<String, Object>> speaking = data.get("speaking");
                if (speaking != null) {
                    allExamples.addAll(convertToResponses(speaking, "speaking"));
                }
            }
            
            // Convert writing examples
            if (type == null || "writing".equals(type)) {
                List<Map<String, Object>> writing = data.get("writing");
                if (writing != null) {
                    allExamples.addAll(convertToResponses(writing, "writing"));
                }
            }
            
            // Apply filters
            return allExamples.stream()
                    .filter(ex -> band == null || Objects.equals(getBandFromMetadata(ex.getMetadata()), band))
                    .filter(ex -> task == null || task.equals(ex.getMetadata().get("task")))
                    .collect(Collectors.toList());
                    
        } catch (IOException e) {
            log.error("Error reading RAG examples file", e);
            throw new AppException(ErrorCode.RAG_FILE_READ_ERROR);
        }
    }

    @Override
    public RAGExampleResponse getExampleById(String id) {
        List<RAGExampleResponse> all = getAllExamples(null, null, null);
        return all.stream()
                .filter(ex -> ex.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.EXAMPLE_NOT_FOUND));
    }

    @Override
    public RAGExampleResponse createExample(RAGExampleRequest request) {
        try {
            Map<String, List<Map<String, Object>>> data = readRagFile();
            
            String type = (String) request.getMetadata().get("type");
            if (type == null || (!type.equals("speaking") && !type.equals("writing"))) {
                throw new AppException(ErrorCode.INVALID_EXAMPLE_TYPE);
            }
            
            List<Map<String, Object>> examples = data.get(type);
            if (examples == null) {
                examples = new ArrayList<>();
                data.put(type, examples);
            }
            
            // Create new example
            Map<String, Object> newExample = new HashMap<>();
            newExample.put("text", request.getText());
            Double metadataBand = getBandFromMetadata(request.getMetadata());
            newExample.put("evaluation", convertEvaluationToMap(request.getEvaluation(), metadataBand));
            newExample.put("metadata", request.getMetadata());
            
            examples.add(newExample);
            
            // Save to file
            writeRagFile(data);
            
            log.info("Created new RAG example - type: {}, band: {}", type, request.getMetadata().get("band"));
            
            // Reload AI service
            reloadAIService();
            
            // Return created example with generated ID
            RAGExampleResponse response = new RAGExampleResponse();
            response.setId(generateId(type, examples.size() - 1));
            response.setText(request.getText());
            response.setEvaluation(convertEvaluationToMap(request.getEvaluation(), metadataBand));
            response.setMetadata(request.getMetadata());
            
            return response;
            
        } catch (IOException e) {
            log.error("Error creating example", e);
            throw new AppException(ErrorCode.RAG_FILE_WRITE_ERROR);
        }
    }

    @Override
    public RAGExampleResponse updateExample(String id, RAGExampleRequest request) {
        try {
            Map<String, List<Map<String, Object>>> data = readRagFile();
            
            String[] parts = id.split("-");
            if (parts.length != 2) {
                throw new AppException(ErrorCode.INVALID_EXAMPLE_ID);
            }
            
            String type = parts[0];
            int index = Integer.parseInt(parts[1]);
            
            List<Map<String, Object>> examples = data.get(type);
            
            if (examples == null || index < 0 || index >= examples.size()) {
                throw new AppException(ErrorCode.EXAMPLE_NOT_FOUND);
            }
            
            // Update example
            Map<String, Object> example = examples.get(index);
            example.put("text", request.getText());
            Double metadataBand = getBandFromMetadata(request.getMetadata());
            example.put("evaluation", convertEvaluationToMap(request.getEvaluation(), metadataBand));
            example.put("metadata", request.getMetadata());
            
            // Save to file
            writeRagFile(data);
            
            log.info("Updated RAG example: {}", id);
            
            // Reload AI service
            reloadAIService();
            
            RAGExampleResponse response = new RAGExampleResponse();
            response.setId(id);
            response.setText(request.getText());
            response.setEvaluation(convertEvaluationToMap(request.getEvaluation(), metadataBand));
            response.setMetadata(request.getMetadata());
            
            return response;
            
        } catch (NumberFormatException e) {
            throw new AppException(ErrorCode.INVALID_EXAMPLE_ID);
        } catch (IOException e) {
            log.error("Error updating example", e);
            throw new AppException(ErrorCode.RAG_FILE_WRITE_ERROR);
        }
    }

    @Override
    public void deleteExample(String id) {
        try {
            Map<String, List<Map<String, Object>>> data = readRagFile();
            
            String[] parts = id.split("-");
            if (parts.length != 2) {
                throw new AppException(ErrorCode.INVALID_EXAMPLE_ID);
            }
            
            String type = parts[0];
            int index = Integer.parseInt(parts[1]);
            
            List<Map<String, Object>> examples = data.get(type);
            
            if (examples == null || index < 0 || index >= examples.size()) {
                throw new AppException(ErrorCode.EXAMPLE_NOT_FOUND);
            }
            
            examples.remove(index);
            
            // Save to file
            writeRagFile(data);
            
            log.info("Deleted RAG example: {}", id);
            
            // Reload AI service
            reloadAIService();
            
        } catch (NumberFormatException e) {
            throw new AppException(ErrorCode.INVALID_EXAMPLE_ID);
        } catch (IOException e) {
            log.error("Error deleting example", e);
            throw new AppException(ErrorCode.RAG_FILE_WRITE_ERROR);
        }
    }

    @Override
    public void reloadAIService() {
        try {
            String reloadUrl = ieltsSenseiUrl + "/api/admin/reload-examples";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> request = new HttpEntity<>("{}", headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    reloadUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("AI service reloaded successfully");
            } else {
                log.warn("AI service reload returned status: {}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error reloading AI service", e);
            // Don't throw exception - file was saved successfully
            // AI service will reload on next restart
        }
    }

    @Override
    public RAGStatsResponse getStatistics() {
        try {
            String statsUrl = ieltsSenseiUrl + "/api/admin/examples/stats";
            ResponseEntity<Map> response = restTemplate.getForEntity(statsUrl, Map.class);
            
            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE);
            }
            
            return objectMapper.convertValue(body, RAGStatsResponse.class);
            
        } catch (Exception e) {
            log.error("Error getting statistics from AI service", e);
            throw new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }
    }

    // Helper methods
    
    private Map<String, List<Map<String, Object>>> readRagFile() throws IOException {
        File file = new File(ragExamplesPath);
        if (!file.exists()) {
            log.warn("RAG examples file not found, creating new one");
            Map<String, List<Map<String, Object>>> emptyData = new HashMap<>();
            emptyData.put("speaking", new ArrayList<>());
            emptyData.put("writing", new ArrayList<>());
            writeRagFile(emptyData);
            return emptyData;
        }
        return objectMapper.readValue(file, new TypeReference<>() {});
    }

    private void writeRagFile(Map<String, List<Map<String, Object>>> data) throws IOException {
        File file = new File(ragExamplesPath);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
    }

    private List<RAGExampleResponse> convertToResponses(List<Map<String, Object>> examples, String type) {
        List<RAGExampleResponse> responses = new ArrayList<>();
        for (int i = 0; i < examples.size(); i++) {
            Map<String, Object> ex = examples.get(i);
            RAGExampleResponse response = new RAGExampleResponse();
            response.setId(generateId(type, i));
            response.setText((String) ex.get("text"));
            response.setEvaluation((Map<String, Object>) ex.get("evaluation"));
            response.setMetadata((Map<String, Object>) ex.get("metadata"));
            responses.add(response);
        }
        return responses;
    }

    private String generateId(String type, int index) {
        return type + "-" + index;
    }

    private Double getBandFromMetadata(Map<String, Object> metadata) {
        Object band = metadata.get("band");
        if (band instanceof Number) {
            return ((Number) band).doubleValue();
        }
        return null;
    }

    private Map<String, Object> convertEvaluationToMap(RAGExampleRequest.EvaluationData evaluation, Double metadataBand) {
        Map<String, Object> map = new HashMap<>();
        
        // Use metadata.band if provided, otherwise use evaluation.overallBand
        Double overallBand = (metadataBand != null) ? metadataBand : evaluation.getOverallBand();
        map.put("overall_band", overallBand);
        
        // Auto-generate criteria_scores if not provided
        Map<String, Object> criteriaScores = null;
        if (evaluation.getCriteriaScores() != null && !evaluation.getCriteriaScores().isEmpty()) {
            // Copy to Map<String, Object> to avoid type mismatch
            criteriaScores = new HashMap<>(evaluation.getCriteriaScores());
        } else {
            criteriaScores = generateDefaultCriteriaScores(overallBand);
        }
        map.put("criteria_scores", criteriaScores);
        
        // Auto-generate detailed_feedback if not provided
        Object detailedFeedback = evaluation.getDetailedFeedback();
        if (detailedFeedback == null || 
            (detailedFeedback instanceof String && ((String) detailedFeedback).isEmpty()) ||
            (detailedFeedback instanceof Map && ((Map) detailedFeedback).isEmpty())) {
            detailedFeedback = generateDefaultDetailedFeedback(evaluation.getExaminerFeedback());
        }
        map.put("detailed_feedback", detailedFeedback);
        
        // Examiner feedback (use provided or empty string)
        String examinerFeedback = evaluation.getExaminerFeedback();
        if (examinerFeedback == null || examinerFeedback.isEmpty()) {
            examinerFeedback = "Feedback cần được bổ sung.";
        }
        map.put("examiner_feedback", examinerFeedback);
        
        // Auto-generate strengths if not provided
        List<String> strengths = evaluation.getStrengths();
        if (strengths == null || strengths.isEmpty()) {
            strengths = generateDefaultStrengths(overallBand);
        }
        map.put("strengths", strengths);
        
        // Auto-generate areas_for_improvement if not provided
        List<String> areasForImprovement = evaluation.getAreasForImprovement();
        if (areasForImprovement == null || areasForImprovement.isEmpty()) {
            areasForImprovement = generateDefaultAreasForImprovement(overallBand);
        }
        map.put("areas_for_improvement", areasForImprovement);
        
        return map;
    }
    
    // Auto-generation helper methods
    
    private Map<String, Object> generateDefaultCriteriaScores(Double overallBand) {
        Map<String, Object> scores = new HashMap<>();
        // Add small variations around overall band
        scores.put("fluency_coherence", roundToHalf(overallBand + 0.0));
        scores.put("lexical_resource", roundToHalf(overallBand - 0.5));
        scores.put("grammatical_range_accuracy", roundToHalf(overallBand + 0.5));
        scores.put("pronunciation", roundToHalf(overallBand + 0.0));
        return scores;
    }
    
    private String generateDefaultDetailedFeedback(String examinerFeedback) {
        if (examinerFeedback != null && !examinerFeedback.isEmpty()) {
            return "Chi tiết: " + examinerFeedback;
        }
        return "Đánh giá chi tiết cần được bổ sung.";
    }
    
    private List<String> generateDefaultStrengths(Double band) {
        List<String> strengths = new ArrayList<>();
        if (band >= 7.0) {
            strengths.add("Vocabulary phong phú và chính xác");
            strengths.add("Grammar structures đa dạng");
            strengths.add("Ideas được phát triển tốt");
        } else if (band >= 5.5) {
            strengths.add("Có thể truyền đạt ý chính");
            strengths.add("Vocabulary cơ bản phù hợp");
            strengths.add("Cố gắng sử dụng cấu trúc đa dạng");
        } else {
            strengths.add("Có cố gắng trả lời câu hỏi");
            strengths.add("Một số ý chính được đề cập");
        }
        return strengths;
    }
    
    private List<String> generateDefaultAreasForImprovement(Double band) {
        List<String> areas = new ArrayList<>();
        if (band >= 7.0) {
            areas.add("Có thể thêm idioms để nâng cao hơn");
            areas.add("Practice pronunciation để tự nhiên hơn");
        } else if (band >= 5.5) {
            areas.add("Cải thiện grammar accuracy");
            areas.add("Mở rộng vocabulary range");
            areas.add("Phát triển ideas chi tiết hơn");
        } else {
            areas.add("Sửa lỗi grammar cơ bản");
            areas.add("Học vocabulary thông dụng hơn");
            areas.add("Practice nói/viết dài hơn với more details");
        }
        return areas;
    }
    
    private Double roundToHalf(Double value) {
        if (value == null) return 5.0;
        // Ensure within 0-9 range
        value = Math.max(0.0, Math.min(9.0, value));
        // Round to nearest 0.5
        return Math.round(value * 2.0) / 2.0;
    }
}
