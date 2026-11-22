package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.dto.response.EvaluationJobResponse;
import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import com.dxh.Elearning.service.interfac.EvaluationJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class EvaluationJobServiceImpl implements EvaluationJobService {
    private static final Set<String> SKILLS = Set.of("writing", "speaking");

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ielts-sensei.service.url:http://localhost:5000}")
    private String aiServiceUrl;

    @Override
    public EvaluationJobResponse create(String skill, Map<String, Object> request) {
        if (!SKILLS.contains(skill)
                || request.get("requestId") == null
                || request.get("idempotencyKey") == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        return exchange(
                aiServiceUrl + "/api/evaluations/" + skill,
                HttpMethod.POST,
                new HttpEntity<>(request));
    }

    @Override
    public EvaluationJobResponse get(String jobId) {
        return exchange(
                aiServiceUrl + "/api/evaluations/" + jobId,
                HttpMethod.GET,
                HttpEntity.EMPTY);
    }

    @Override
    public EvaluationJobResponse cancel(String jobId) {
        return exchange(
                aiServiceUrl + "/api/evaluations/" + jobId + "/cancel",
                HttpMethod.POST,
                HttpEntity.EMPTY);
    }

    @Override
    public EvaluationJobResponse requestFeedback(String jobId) {
        return exchange(
                aiServiceUrl + "/api/evaluations/" + jobId + "/feedback",
                HttpMethod.POST,
                HttpEntity.EMPTY);
    }

    private EvaluationJobResponse exchange(
            String url,
            HttpMethod method,
            HttpEntity<?> entity) {
        try {
            ResponseEntity<EvaluationJobResponse> response = restTemplate.exchange(
                    url, method, entity, EvaluationJobResponse.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new AppException(ErrorCode.EVALUATION_FAILED);
            }
            return response.getBody();
        } catch (RestClientException exception) {
            log.error("Evaluation job request failed: {} {}", method, url, exception);
            throw new AppException(ErrorCode.EVALUATION_FAILED);
        }
    }
}
