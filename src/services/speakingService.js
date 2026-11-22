import axios from 'axios';

// Spring (main backend) base URL
const DJANGO_BASE_URL = import.meta.env.VITE_BASE_URL || 'http://localhost:8080';
// Flask IELTSensei API base URL
const SENSEI_API_BASE = import.meta.env.VITE_SENSEI_API_BASE || 'http://localhost:5000';

const getAuthHeaders = () => {
  const authTokensStr = localStorage.getItem('authTokens');
  if (!authTokensStr) return {};
  
  try {
    const authTokens = JSON.parse(authTokensStr);
    const token = authTokens.token;
    return token ? { Authorization: `Bearer ${token}` } : {};
  } catch (e) {
    console.error('Error parsing auth tokens:', e);
    return {};
  }
};

/**
 * Evaluate IELTS Speaking by question ID
 * POST /ielts/speaking/evaluate-by-question
 * @param {File} audioFile - Audio recording file (webm, mp4, etc.)
 * @param {number} questionId - Question ID from database
 * @param {number} userExamPartId - UserExamPart ID to save the answer
 * @param {string} audioUrl - Optional audio URL (S3 or storage path)
 * @returns {Promise} ApiResponse<IELTSSpeakingResponse>
 */
export const evaluateSpeakingByQuestion = async (audioFile, questionId, userExamPartId, audioUrl = null) => {
  try {
    // Call Spring Boot backend which will:
    // 1. Call Flask IELTSensei for transcription + evaluation
    // 2. Save to database (UserAnswer table)
    const formData = new FormData();
    formData.append('file', audioFile);
    formData.append('questionId', questionId);
    formData.append('userExamPartId', userExamPartId);
    if (audioUrl) {
      formData.append('audioUrl', audioUrl);
    }

    console.log(`📤 Calling Spring Boot API: ${DJANGO_BASE_URL}/api/ielts/speaking/evaluate-by-question`);
    console.log(`📝 Parameters: questionId=${questionId}, userExamPartId=${userExamPartId}`);

    const response = await axios.post(
      `${DJANGO_BASE_URL}/api/ielts/speaking/evaluate-by-question`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
          ...getAuthHeaders()
        },
        timeout: 120000 // 2 minutes for transcription + evaluation
      }
    );

    if (response.data?.code === 200 || response.status === 200) {
      console.log('✅ Evaluation completed and saved to database');
      return {
        code: 200,
        result: response.data.result || response.data
      };
    }

    throw new Error('Invalid response from backend');
  } catch (error) {
    console.error('❌ Error evaluating speaking:', error);
    console.error('Error details:', {
      message: error.message,
      response: error.response?.data,
      status: error.response?.status,
      url: `${DJANGO_BASE_URL}/api/ielts/speaking/evaluate-by-question`
    });
    
    return Promise.reject(
      error?.response?.data || { code: 500, message: error.message || 'Evaluation failed' }
    );
  }
};

/**
 * Evaluate an entire part with multiple recordings at once
 * This sends ALL recordings to Flask API in ONE request for holistic evaluation
 * @param {Array} recordings - Array of { questionId, audioBlob, part, questionIndex }
 * @param {string} part - Part name (part1, part2, part3)
 * @returns {Promise} Single evaluation result for the entire part
 */
export const evaluatePartWithMultipleRecordings = async (recordings, part) => {
  try {
    if (!recordings || recordings.length === 0) {
      throw new Error('No recordings provided');
    }

    // Step 1: Transcribe all recordings SEQUENTIALLY (not parallel) to avoid Whisper threading issues
    const transcriptions = [];
    for (const { audioBlob, questionIndex } of recordings) {
      try {
        const transcribeForm = new FormData();
        transcribeForm.append('audio', audioBlob);
        transcribeForm.append('part', part);
        transcribeForm.append('question_index', questionIndex || 0);

        console.log(`🎤 Transcribing question ${questionIndex + 1}/${recordings.length}...`);

        const transcribeRes = await axios.post(
          `${SENSEI_API_BASE}/api/speaking/transcribe`,
          transcribeForm,
          {
            headers: { 'Content-Type': 'multipart/form-data' },
            timeout: 60000
          }
        );

        transcriptions.push({
          part: part,
          question_index: questionIndex || 0,
          text: transcribeRes.data.text || '',
          duration: transcribeRes.data.duration || 0,
          transcript: transcribeRes.data.text || '' // For UI display
        });

        console.log(`✅ Question ${questionIndex + 1} transcribed: ${transcribeRes.data.text?.substring(0, 50)}...`);
      } catch (err) {
        console.error(`❌ Failed to transcribe question ${questionIndex + 1}:`, err);
        // Continue with other recordings even if one fails
        transcriptions.push({
          part: part,
          question_index: questionIndex || 0,
          text: '',
          duration: 0,
          transcript: ''
        });
      }
    }

    // Validate all transcriptions succeeded
    const invalidTranscripts = transcriptions.filter(t => !t.text || !t.text.trim());
    if (invalidTranscripts.length > 0) {
      throw new Error(`${invalidTranscripts.length} recording(s) failed to transcribe`);
    }

    // Step 2: Send ALL transcripts to evaluate endpoint in ONE request
    const payload = {
      evaluate_type: 'single_part',
      part: part,
      responses: transcriptions // Array of all transcripts for this part
    };

    console.log(`🎯 Evaluating ${part} with ${transcriptions.length} recordings:`, payload);

    const evalRes = await axios.post(
      `${SENSEI_API_BASE}/api/speaking/evaluate`,
      payload,
      {
        headers: { 'Content-Type': 'application/json' },
        timeout: 120000
      }
    );

    // Return evaluation result with transcripts included
    return {
      code: 200,
      result: {
        ...evalRes.data,
        transcripts: transcriptions.reduce((acc, t) => {
          acc[t.question_index] = t.transcript;
          return acc;
        }, {})
      }
    };
  } catch (error) {
    console.error('Error evaluating part:', error);
    return Promise.reject(
      error?.response?.data || { code: 500, message: error.message || 'Evaluation failed' }
    );
  }
};

/**
 * Batch evaluate multiple speaking recordings (OLD METHOD - evaluates each separately)
 * @param {Array} recordings - Array of { questionId, audioBlob }
 * @returns {Promise} Array of evaluation results
 */
export const batchEvaluateSpeaking = async (recordings) => {
  try {
    const promises = recordings.map(({ questionId, audioBlob }) => {
      return evaluateSpeakingByQuestion(audioBlob, questionId);
    });

    const results = await Promise.all(promises);
    return results;
  } catch (error) {
    console.error('Error in batch evaluation:', error);
    throw error;
  }
};

/**
 * Submit the entire Speaking exam (all 3 parts) and update final score
 * POST /userexamparts/speaking/submit
 * @param {object} data - Submission data containing overall scores
 * @returns {Promise} ApiResponse
 */
export const submitSpeakingExam = async (data) => {
  try {
    console.log(`📤 Submitting Speaking exam to: ${DJANGO_BASE_URL}/api/userexamparts/speaking/submit`);
    console.log(`📊 Submission data:`, data);

    const response = await axios.post(
      `${DJANGO_BASE_URL}/api/userexamparts/speaking/submit`,
      data,
      {
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeaders()
        },
        timeout: 30000
      }
    );

    if (response.data?.code === 200 || response.status === 200) {
      console.log('✅ Speaking exam submitted successfully');
      return {
        code: 200,
        message: 'Speaking exam submitted successfully'
      };
    }

    throw new Error('Invalid response from backend');
  } catch (error) {
    console.error('❌ Error submitting speaking exam:', error);
    return Promise.reject(
      error?.response?.data || { code: 500, message: error.message || 'Submission failed' }
    );
  }
};

export default {
  evaluateSpeakingByQuestion,
  evaluatePartWithMultipleRecordings,
  batchEvaluateSpeaking,
  submitSpeakingExam
};
