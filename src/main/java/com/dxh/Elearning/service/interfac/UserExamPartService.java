package com.dxh.Elearning.service.interfac;

import com.dxh.Elearning.dto.request.SubmitRLPartRequest;
import com.dxh.Elearning.dto.request.SubmitSpeakingExamRequest;
import com.dxh.Elearning.dto.request.SubmitWritingExamRequest;
import com.dxh.Elearning.dto.request.UserExamRequest;
import com.dxh.Elearning.dto.response.SubmitRLPartResponse;
import com.dxh.Elearning.dto.response.UserExamPartResponse;
import com.dxh.Elearning.dto.response.UserExamResponse;
import com.dxh.Elearning.dto.response.WritingResultResponse;

import java.util.List;

public interface UserExamPartService {

    SubmitRLPartResponse submitRLPart(SubmitRLPartRequest req);
    
    void submitSpeakingExam(SubmitSpeakingExamRequest req);

    WritingResultResponse submitWritingExam(SubmitWritingExamRequest req);

}
