package com.dxh.Elearning.service.interfac;

import com.dxh.Elearning.dto.response.EvaluationJobResponse;

import java.util.Map;

public interface EvaluationJobService {
    EvaluationJobResponse create(String skill, Map<String, Object> request);

    EvaluationJobResponse get(String jobId);

    EvaluationJobResponse cancel(String jobId);

    EvaluationJobResponse requestFeedback(String jobId);
}
