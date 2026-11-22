/**
 * Exam Service - Handles all exam-related API calls
 * Integrates with Spring Boot backend exam APIs
 */

import axios from 'axios';
import { DJANGO_BASE_URL } from '../utils/config';

// Get axios instance with auth token
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
 * Get paginated list of exams
 * @param {number} pageNo - Page number (default: 1)
 * @param {number} pageSize - Items per page (default: 20)
 * @param {string} sortBy - Sort field (optional)
 * @returns {Promise} ApiResponse with PageResponse<List<ExamResponse>>
 */
export const getExamList = async (pageNo = 1, pageSize = 20, sortBy = null) => {
  try {
    const params = { pageNo, pageSize };
    if (sortBy) params.sortBy = sortBy;

    console.log('Fetching exams with params:', params);
    console.log('Auth headers:', getAuthHeaders());

    const response = await axios.get(`${DJANGO_BASE_URL}/exams/list`, {
      headers: getAuthHeaders(),
      params
    });
    
    console.log('Exams response:', response.data);
    return response.data;
  } catch (error) {
    console.error('Error fetching exam list:', error);
    console.error('Error response:', error.response?.data);
    throw error;
  }
};

/**
 * Get specific exam by ID
 * Note: Backend doesn't have this endpoint yet, but we can get from list
 * @param {number} examId - Exam ID
 * @returns {Promise} Exam details
 */
export const getExamById = async (examId) => {
  try {
    // For now, get from list and filter
    const response = await getExamList(1, 100);
    // PageResponse has 'items' field
    const exam = response.result?.items?.find(e => e.id === examId);
    return exam;
  } catch (error) {
    console.error('Error fetching exam by ID:', error);
    throw error;
  }
};

/**
 * Create a new user exam session
 * POST /userexams
 * @param {number} examId - ID of the exam to start
 * @param {string} skillType - Optional: Specific skill type to create (LISTENING, READING, WRITING, SPEAKING). If null, creates Full Test (all parts)
 * @returns {Promise} ApiResponse<UserExamResponse>
 * Response includes: { id, userId, examId, startedAt, parts: [UserExamPartResponse] }
 */
export const createUserExam = async (examId, skillType = null) => {
  try {
    // Validate examId
    if (!examId || isNaN(examId)) {
      throw new Error('Invalid examId: examId must be a valid number');
    }
    
    const requestBody = { examId: Number(examId) };
    if (skillType) {
      requestBody.skillType = skillType;
    }
    
    const response = await axios.post(
      `${DJANGO_BASE_URL}/userexams`,
      requestBody,
      { headers: getAuthHeaders() }
    );
    return response.data;
  } catch (error) {
    console.error('Error creating user exam:', error);
    throw error;
  }
};

/**
 * Get current user's exam history
 * GET /userexams/my-exams
 * @returns {Promise} ApiResponse<List<UserExamResponse>>
 */
export const getMyExams = async () => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/userexams/my-exams`, {
      headers: getAuthHeaders()
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching user exams:', error);
    throw error;
  }
};

/**
 * Get a specific UserExam by ID
 * GET /userexams/{id}
 * @param {number} userExamId - ID of the UserExam
 * @returns {Promise} ApiResponse<UserExamResponse>
 */
export const getUserExamById = async (userExamId) => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/userexams/${userExamId}`, {
      headers: getAuthHeaders()
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching user exam by ID:', error);
    throw error;
  }
};

/**
 * Submit Reading or Listening part answers
 * POST /userexamparts
 * @param {number} userExamPartId - ID of the UserExamPart
 * @param {Array} answers - Array of {questionId, selectedOptionId}
 * @returns {Promise} ApiResponse<SubmitRLPartResponse>
 * Response includes: { id, userExamId, skillType, submitted, score, answers }
 */
export const submitRLPart = async (userExamPartId, answers) => {
  try {
    const response = await axios.post(
      `${DJANGO_BASE_URL}/userexamparts`,
      {
        userExamPartId,
        answers
      },
      { headers: getAuthHeaders() }
    );
    return response.data;
  } catch (error) {
    console.error('Error submitting RL part:', error);
    throw error;
  }
};

/**
 * Helper function to find Listening part from UserExam response
 * @param {Object} userExamResponse - UserExamResponse object
 * @returns {Object} UserExamPartResponse for LISTENING skill
 */
export const getListeningPart = (userExamResponse) => {
  if (!userExamResponse || !userExamResponse.parts) return null;
  return userExamResponse.parts.find(part => part.skillType === 'LISTENING');
};

/**
 * Helper function to find Reading part from UserExam response
 * @param {Object} userExamResponse - UserExamResponse object
 * @returns {Object} UserExamPartResponse for READING skill
 */
export const getReadingPart = (userExamResponse) => {
  if (!userExamResponse || !userExamResponse.parts) return null;
  return userExamResponse.parts.find(part => part.skillType === 'READING');
};

/**
 * Get exam parts by exam ID
 * GET /examparts/list?examId={examId}
 * @param {number} examId - Exam ID
 * @returns {Promise} ApiResponse<List<ExamPartResponse>>
 */
export const getExamParts = async (examId) => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/examparts/list`, {
      headers: getAuthHeaders(),
      params: { examId }
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching exam parts:', error);
    throw error;
  }
};

/**
 * Get questions by exam part ID
 * GET /questions/list?examPartId={examPartId}&pageNo=0&pageSize=100
 * @param {number} examPartId - ExamPart ID
 * @param {number} pageNo - Page number (default: 0)
 * @param {number} pageSize - Items per page (default: 100)
 * @returns {Promise} ApiResponse<PageResponse<List<QuestionResponse>>>
 */
export const getQuestionsByExamPart = async (examPartId, pageNo = 0, pageSize = 100) => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/questions/list`, {
      headers: getAuthHeaders(),
      params: { examPartId, pageNo, pageSize }
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching questions:', error);
    throw error;
  }
};

/**
 * Get all speaking questions by exam ID
 * GET /questions/exam/{examId}/skill-type/SPEAKING
 * @param {number} examId - Exam ID
 * @returns {Promise} ApiResponse<List<QuestionResponse>>
 */
export const getSpeakingQuestionsByExamId = async (examId) => {
  try {
    const response = await axios.get(
      `${DJANGO_BASE_URL}/questions/exam/${examId}/skill-type/SPEAKING`,
      { headers: getAuthHeaders() }
    );
    return response.data;
  } catch (error) {
    console.error('Error fetching speaking questions by exam ID:', error);
    throw error;
  }
};

/**
 * Get all speaking questions by exam part ID
 * GET /questions/speaking/exam-part/{examPartId}
 * @param {number} examPartId - ExamPart ID with skillType='SPEAKING'
 * @returns {Promise} ApiResponse<List<QuestionResponse>>
 */
export const getSpeakingQuestions = async (examPartId) => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/questions/speaking/exam-part/${examPartId}`, {
      headers: getAuthHeaders()
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching speaking questions:', error);
    throw error;
  }
};

/**
 * Get user answers by UserExamPart ID
 * GET /useranswers/answers/{userExamPartId}
 * @param {number} userExamPartId - UserExamPart ID
 * @returns {Promise} ApiResponse<List<UserAnswerResponse>>
 */
export const getUserAnswers = async (userExamPartId) => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/useranswers/answers/${userExamPartId}`, {
      headers: getAuthHeaders()
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching user answers:', error);
    throw error;
  }
};

/**
 * Create a new LISTENING question with audio file
 * POST /questions/listening (multipart/form-data)
 * @param {Object} questionData - Question data {examPartId, skillType, type, content, maxScore, options, correctTempId}
 * @param {File} audioFile - Audio file (MP3/WAV)
 * @returns {Promise} ApiResponse<QuestionResponse>
 */
export const createListeningQuestion = async (questionData, audioFile) => {
  try {
    const formData = new FormData();
    
    // Add question JSON as blob with correct content type
    formData.append('question', new Blob([JSON.stringify(questionData)], {
      type: 'application/json'
    }));
    
    // Add audio file
    formData.append('audio', audioFile);

    const response = await axios.post(
      `${DJANGO_BASE_URL}/questions/listening`,
      formData,
      {
        headers: {
          ...getAuthHeaders(),
          'Content-Type': 'multipart/form-data'
        }
      }
    );
    
    return response.data;
  } catch (error) {
    console.error('Error creating listening question:', error);
    throw error;
  }
};

/**
 * Create a new READING question
 * POST /questions/reading (application/json)
 * @param {Object} questionData - Question data {examPartId, skillType, type, content, maxScore, options, correctTempId}
 * @returns {Promise} ApiResponse<QuestionResponse>
 */
export const createReadingQuestion = async (questionData) => {
  try {
    const response = await axios.post(
      `${DJANGO_BASE_URL}/questions/reading`,
      questionData,
      { headers: getAuthHeaders() }
    );
    return response.data;
  } catch (error) {
    console.error('Error creating reading question:', error);
    throw error;
  }
};

/**
 * Get all listening sections for an examPart with complete questions
 * GET /questions/listening/sections?examPartId={examPartId}
 * @param {number} examPartId - ExamPart ID
 * @returns {Promise} ApiResponse<List<SectionResponse>>
 * Response includes: [{sectionName, questionCount, audioUrl, instructions, questions}]
 */
export const getAllListeningSections = async (examPartId) => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/questions/listening/sections`, {
      headers: getAuthHeaders(),
      params: { examPartId }
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching listening sections:', error);
    throw error;
  }
};

/**
 * Get all writing questions by exam ID
 * GET /questions/exam/{examId}/skill-type/WRITING
 * @param {number} examId - Exam ID
 * @returns {Promise} ApiResponse<List<QuestionResponse>>
 */
export const getWritingQuestionsByExamId = async (examId) => {
  try {
    const url = `${DJANGO_BASE_URL}/questions/exam/${examId}/skill-type/WRITING`;
    console.log('🔍 Fetching writing questions from URL:', url);
    console.log('🔑 Auth headers:', getAuthHeaders());
    
    const response = await axios.get(url, { 
      headers: getAuthHeaders() 
    });
    
    console.log('✅ Writing questions response:', response.data);
    console.log('📊 Questions count:', response.data?.result?.length || 0);
    
    return response.data;
  } catch (error) {
    console.error('❌ Error fetching writing questions by exam ID:', error);
    console.error('📍 Request URL:', `${DJANGO_BASE_URL}/questions/exam/${examId}/skill-type/WRITING`);
    console.error('📋 Error response:', error.response?.data);
    console.error('🔢 Status code:', error.response?.status);
    throw error;
  }
};

/**
 * Get all listening questions by exam ID
 * GET /questions/exam/{examId}/skill-type/LISTENING
 * @param {number} examId - Exam ID
 * @returns {Promise} ApiResponse<List<QuestionResponse>>
 */
export const getListeningQuestionsByExamId = async (examId) => {
  try {
    const url = `${DJANGO_BASE_URL}/questions/exam/${examId}/skill-type/LISTENING`;
    console.log('🔍 Fetching listening questions from URL:', url);
    
    const response = await axios.get(url, { 
      headers: getAuthHeaders() 
    });
    
    console.log('✅ Listening questions response:', response.data);
    console.log('📊 Questions count:', response.data?.result?.length || 0);
    
    return response.data;
  } catch (error) {
    console.error('❌ Error fetching listening questions by exam ID:', error);
    throw error;
  }
};

/**
 * Get all reading questions by exam ID
 * GET /questions/exam/{examId}/skill-type/READING
 * @param {number} examId - Exam ID
 * @returns {Promise} ApiResponse<List<QuestionResponse>>
 */
export const getReadingQuestionsByExamId = async (examId) => {
  try {
    const url = `${DJANGO_BASE_URL}/questions/exam/${examId}/skill-type/READING`;
    console.log('🔍 Fetching reading questions from URL:', url);
    
    const response = await axios.get(url, { 
      headers: getAuthHeaders() 
    });
    
    console.log('✅ Reading questions response:', response.data);
    console.log('📊 Questions count:', response.data?.result?.length || 0);
    
    return response.data;
  } catch (error) {
    console.error('❌ Error fetching reading questions by exam ID:', error);
    throw error;
  }
};

/**
 * Evaluate writing by question ID
 * POST /ielts/writing/evaluate-by-question
 * @param {string} essayText - The essay text to evaluate
 * @param {number} questionId - Question ID
 * @param {number} wordCount - Word count (optional)
 * @param {number} duration - Duration in minutes (optional)
 * @returns {Promise} ApiResponse<IELTSWritingResponse>
 */
export const evaluateWriting = async (essayText, questionId, wordCount = null, duration = null) => {
  try {
    const params = new URLSearchParams();
    params.append('essayText', essayText);
    params.append('questionId', questionId);
    if (wordCount !== null) params.append('wordCount', wordCount);
    if (duration !== null) params.append('duration', duration);

    const response = await axios.post(
      `${DJANGO_BASE_URL}/ielts/writing/evaluate-by-question`,
      null,
      {
        headers: getAuthHeaders(),
        params
      }
    );
    return response.data;
  } catch (error) {
    console.error('Error evaluating writing:', error);
    throw error;
  }
};

/**
 * Submit Writing exam with AI evaluation results
 * POST /userexamparts/writing/submit
 * @param {number} userExamPartId - User exam part ID
 * @param {number} overallBand - Overall IELTS band score (0-9)
 * @param {Array} taskResults - Array of writing task results with scores
 * @returns {Promise} ApiResponse
 */
export const submitWritingExam = async (userExamPartId, overallBand, taskResults) => {
  try {
    const response = await axios.post(
      `${DJANGO_BASE_URL}/userexamparts/writing/submit`,
      { userExamPartId, overallBand, taskResults },
      { headers: getAuthHeaders() }
    );
    return response.data;
  } catch (error) {
    console.error('Error submitting writing exam:', error);
    throw error;
  }
};

/**
 * Get Listening result by userExamPartId
 * @param {number} userExamPartId - UserExamPart ID
 * @returns {Promise} Result data including band, score, correctAnswers
 */
export const getListeningResult = async (userExamPartId) => {
  try {
    if (!userExamPartId) {
      throw new Error('No userExamPartId provided');
    }

    // Get user answers to calculate stats
    const answersResponse = await getUserAnswers(userExamPartId);
    const answers = answersResponse.result || [];
    
    const correctAnswers = answers.filter(a => a.score === 1.0).length;
    const totalQuestions = answers.length;
    const percentage = totalQuestions > 0 ? ((correctAnswers / totalQuestions) * 100) : 0;
    
    // Calculate band score
    const band = calculateBandScore(correctAnswers, totalQuestions);

    return {
      userExamPartId: userExamPartId,
      band: band,
      correctAnswers: correctAnswers,
      totalQuestions: totalQuestions,
      score: percentage
    };
  } catch (error) {
    console.error('Error fetching listening result:', error);
    throw error;
  }
};

/**
 * Get Reading result by userExamPartId
 * @param {number} userExamPartId - UserExamPart ID
 * @returns {Promise} Result data including band, score, correctAnswers
 */
export const getReadingResult = async (userExamPartId) => {
  try {
    if (!userExamPartId) {
      throw new Error('No userExamPartId provided');
    }

    const answersResponse = await getUserAnswers(userExamPartId);
    const answers = answersResponse.result || [];
    
    const correctAnswers = answers.filter(a => a.score === 1.0).length;
    const totalQuestions = answers.length;
    const percentage = totalQuestions > 0 ? ((correctAnswers / totalQuestions) * 100) : 0;
    const band = calculateBandScore(correctAnswers, totalQuestions);

    return {
      userExamPartId: userExamPartId,
      band: band,
      correctAnswers: correctAnswers,
      totalQuestions: totalQuestions,
      score: percentage
    };
  } catch (error) {
    console.error('Error fetching reading result:', error);
    throw error;
  }
};

/**
 * Get Writing result by userExamPartId
 * @param {number} userExamPartId - UserExamPart ID
 * @returns {Promise} Result data including overallBand, detailedFeedback
 */
export const getWritingResult = async (userExamPartId) => {
  try {
    if (!userExamPartId) {
      throw new Error('No userExamPartId provided');
    }

    // Get user answers which contain AI feedback
    const answersResponse = await getUserAnswers(userExamPartId);
    const answers = answersResponse.result || [];

    if (answers.length === 0) {
      throw new Error('No writing answers found');
    }

    // Calculate average score from all writing tasks
    let totalScore = 0;
    let scoreCount = 0;
    let detailedFeedback = {};
    let criteriaScores = {
      task_achievement: [],
      coherence_cohesion: [],
      lexical_resource: [],
      grammatical_range: []
    };

    answers.forEach(answer => {
      // Try to get score from aiScore field
      if (answer.aiScore) {
        totalScore += parseFloat(answer.aiScore);
        scoreCount++;
      }
      
      // Parse AI feedback to get criteria scores
      if (answer.aiFeedback) {
        try {
          const feedback = typeof answer.aiFeedback === 'string' 
            ? JSON.parse(answer.aiFeedback) 
            : answer.aiFeedback;
          
          // Collect criteria scores - support both formats:
          // 1. Nested in criteria_scores object
          // 2. Direct properties (taskAchievement, coherenceCohesion, etc.)
          let taskAch, cohCoh, lexRes, gramRange;
          
          if (feedback.criteria_scores) {
            // Format 1: nested object
            const cs = feedback.criteria_scores;
            taskAch = cs.task_achievement || cs.taskAchievement;
            cohCoh = cs.coherence_cohesion || cs.coherenceCohesion;
            lexRes = cs.lexical_resource || cs.lexicalResource;
            gramRange = cs.grammatical_range || cs.grammaticalRange;
          } else {
            // Format 2: direct properties
            taskAch = feedback.task_achievement || feedback.taskAchievement;
            cohCoh = feedback.coherence_cohesion || feedback.coherenceCohesion;
            lexRes = feedback.lexical_resource || feedback.lexicalResource;
            gramRange = feedback.grammatical_range || feedback.grammaticalRange;
          }
          
          // Push scores if they exist
          if (taskAch) criteriaScores.task_achievement.push(parseFloat(taskAch));
          if (cohCoh) criteriaScores.coherence_cohesion.push(parseFloat(cohCoh));
          if (lexRes) criteriaScores.lexical_resource.push(parseFloat(lexRes));
          if (gramRange) criteriaScores.grammatical_range.push(parseFloat(gramRange));
          
          // Get detailed feedback for display
          if (!detailedFeedback.taskAchievement) {
            detailedFeedback = {
              taskAchievement: taskAch || 'N/A',
              coherenceCohesion: cohCoh || 'N/A',
              lexicalResource: lexRes || 'N/A',
              grammaticalRange: gramRange || 'N/A'
            };
          }
        } catch (e) {
          console.error('Error parsing writing feedback:', e);
        }
      }
    });

    // Calculate overall band score
    let avgScore = 0;
    
    // Priority 1: Use aiScore if available
    if (scoreCount > 0) {
      avgScore = totalScore / scoreCount;
    } 
    // Priority 2: Calculate from criteria scores
    else {
      const criteriaAverages = [];
      Object.values(criteriaScores).forEach(scores => {
        if (scores.length > 0) {
          const avg = scores.reduce((a, b) => a + b, 0) / scores.length;
          criteriaAverages.push(avg);
        }
      });
      
      if (criteriaAverages.length > 0) {
        avgScore = criteriaAverages.reduce((a, b) => a + b, 0) / criteriaAverages.length;
      }
    }

    // Round to nearest 0.5
    const roundedScore = Math.round(avgScore * 2) / 2;
    
    // Calculate average criteria for display
    const avgCriteria = {};
    Object.keys(criteriaScores).forEach(key => {
      if (criteriaScores[key].length > 0) {
        const avg = criteriaScores[key].reduce((a, b) => a + b, 0) / criteriaScores[key].length;
        avgCriteria[key] = Math.round(avg * 2) / 2;
      }
    });

    return {
      userExamPartId: userExamPartId,
      overallBand: roundedScore > 0 ? roundedScore.toFixed(1) : '0.0',
      criteria_scores: Object.keys(avgCriteria).length > 0 ? avgCriteria : null,
      detailedFeedback: detailedFeedback
    };
  } catch (error) {
    console.error('Error fetching writing result:', error);
    throw error;
  }
};

/**
 * Get Speaking result by userExamPartId
 * @param {number} userExamPartId - UserExamPart ID
 * @returns {Promise} Result data including overallBand, detailedScores
 */
export const getSpeakingResult = async (userExamPartId) => {
  try {
    if (!userExamPartId) {
      throw new Error('No userExamPartId provided');
    }

    // Get user answers which contain AI scores
    const answersResponse = await getUserAnswers(userExamPartId);
    const answers = answersResponse.result || [];

    if (answers.length === 0) {
      throw new Error('No speaking answers found');
    }

    // Calculate average scores from all speaking parts
    const criteria = {
      fluency: [],
      vocabulary: [],
      grammar: [],
      pronunciation: []
    };

    let totalScore = 0;

    answers.forEach(answer => {
      if (answer.aiScore) {
        totalScore += parseFloat(answer.aiScore);
      }
      if (answer.aiFeedback) {
        try {
          const feedback = JSON.parse(answer.aiFeedback);
          if (feedback.fluency) criteria.fluency.push(parseFloat(feedback.fluency));
          if (feedback.vocabulary) criteria.vocabulary.push(parseFloat(feedback.vocabulary));
          if (feedback.grammar) criteria.grammar.push(parseFloat(feedback.grammar));
          if (feedback.pronunciation) criteria.pronunciation.push(parseFloat(feedback.pronunciation));
        } catch (e) {
          console.error('Error parsing speaking feedback:', e);
        }
      }
    });

    const avgScore = answers.length > 0 ? (totalScore / answers.length).toFixed(1) : '0.0';

    const detailedScores = {
      fluency: criteria.fluency.length > 0 ? (criteria.fluency.reduce((a,b) => a+b, 0) / criteria.fluency.length).toFixed(1) : 'N/A',
      vocabulary: criteria.vocabulary.length > 0 ? (criteria.vocabulary.reduce((a,b) => a+b, 0) / criteria.vocabulary.length).toFixed(1) : 'N/A',
      grammar: criteria.grammar.length > 0 ? (criteria.grammar.reduce((a,b) => a+b, 0) / criteria.grammar.length).toFixed(1) : 'N/A',
      pronunciation: criteria.pronunciation.length > 0 ? (criteria.pronunciation.reduce((a,b) => a+b, 0) / criteria.pronunciation.length).toFixed(1) : 'N/A'
    };

    return {
      userExamPartId: userExamPartId,
      overallBand: avgScore,
      detailedScores: detailedScores
    };
  } catch (error) {
    console.error('Error fetching speaking result:', error);
    throw error;
  }
};

/**
 * Calculate IELTS band score from correct answers
 * This is a simplified calculation - adjust based on your actual band conversion table
 */
const calculateBandScore = (correct, total) => {
  const percentage = (correct / total) * 100;
  
  if (percentage >= 90) return '9.0';
  if (percentage >= 82) return '8.5';
  if (percentage >= 75) return '8.0';
  if (percentage >= 67) return '7.5';
  if (percentage >= 60) return '7.0';
  if (percentage >= 52) return '6.5';
  if (percentage >= 45) return '6.0';
  if (percentage >= 37) return '5.5';
  if (percentage >= 30) return '5.0';
  if (percentage >= 22) return '4.5';
  if (percentage >= 15) return '4.0';
  return '3.5';
};

/**
 * Get current user's info including profile details
 * GET /users/myInfo
 * @returns {Promise} User info object
 */
export const getUserInfo = async () => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/users/myInfo`, {
      headers: getAuthHeaders()
    });
    
    if (response.data.code === 200) {
      return response.data.result;
    }
    
    return null;
  } catch (error) {
    console.error('Error fetching user info:', error);
    return null;
  }
};

/**
 * Get user statistics (average scores, recent tests, etc.)
 * Calculates from user's exam history
 * @returns {Promise} User statistics object
 */
/**
 * Get recent test history from new dedicated API
 * Uses the /userexams/test-history endpoint
 * @param {number} limit - Number of recent tests to retrieve (default: 5)
 * @returns {Promise} Array of recent tests
 */
export const getRecentTests = async (limit = 5) => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/userexams/test-history`, {
      headers: getAuthHeaders(),
      params: { limit }
    });
    
    if (response.data.code !== 200 || !response.data.result) {
      return [];
    }

    const historyData = response.data.result;
    
    // Transform to frontend format
    return historyData.map(item => ({
      id: item.isFullTest ? `${item.userExamId}-fulltest` : `${item.userExamId}-${item.skillType}`,
      userExamId: item.userExamId,
      examId: item.examId,
      userExamPartId: item.userExamPartId,
      title: item.examName,
      skill: item.isFullTest ? 'FULL TEST' : item.skillType,
      score: item.score,
      date: new Date(item.testDate).toLocaleDateString('vi-VN'),
      timestamp: new Date(item.testDate),
      status: 'completed',
      variant: item.isFullTest ? 'info' : getSkillVariant(item.skillType),
      isFullTest: item.isFullTest,
      parts: item.partScores || []
    }));
      
  } catch (error) {
    console.error('Error fetching recent tests:', error);
    return [];
  }
};

// Helper function to get badge variant for skill type
const getSkillVariant = (skillType) => {
  const variants = {
    'LISTENING': 'primary',
    'READING': 'warning',
    'WRITING': 'success',
    'SPEAKING': 'danger'
  };
  return variants[skillType] || 'secondary';
};

export const getUserStats = async () => {
  try {
    const response = await getMyExams();
    
    if (response.code !== 200 || !response.result) {
      return {
        overallScore: 0,
        skills: {
          LISTENING: 0,
          READING: 0,
          WRITING: 0,
          SPEAKING: 0
        },
        recentTests: [],
        totalTests: 0
      };
    }

    const userExams = response.result;
    const skillScores = {
      LISTENING: [],
      READING: [],
      WRITING: [],
      SPEAKING: []
    };

    // Collect ALL submitted part scores (both individual parts and full tests)
    userExams.forEach(exam => {
      if (exam.parts) {
        exam.parts.forEach(part => {
          if (part.submitted && part.score > 0) {
            skillScores[part.skillType]?.push(part.score);
          }
        });
      }
    });

    // Calculate averages
    const calculateAvg = (scores) => {
      if (scores.length === 0) return 0;
      return scores.reduce((sum, score) => sum + score, 0) / scores.length;
    };

    const avgSkills = {
      LISTENING: calculateAvg(skillScores.LISTENING),
      READING: calculateAvg(skillScores.READING),
      WRITING: calculateAvg(skillScores.WRITING),
      SPEAKING: calculateAvg(skillScores.SPEAKING)
    };

    const overallScore = (avgSkills.LISTENING + avgSkills.READING + avgSkills.WRITING + avgSkills.SPEAKING) / 4;

    // Get recent full tests (exams with all 4 parts submitted)
    const recentTests = userExams
      .filter(exam => {
        const submittedParts = exam.parts?.filter(p => p.submitted) || [];
        return submittedParts.length === 4;
      })
      .sort((a, b) => new Date(b.startedAt) - new Date(a.startedAt))
      .slice(0, 5)
      .map(exam => ({
        id: exam.id,
        date: exam.startedAt,
        score: exam.totalScore || 0,
        parts: exam.parts
      }));

    return {
      overallScore: parseFloat(overallScore.toFixed(1)),
      skills: avgSkills,
      recentTests,
      totalTests: userExams.length  // Total count of all exams
    };
  } catch (error) {
    console.error('Error fetching user stats:', error);
    return {
      overallScore: 0,
      skills: {
        LISTENING: 0,
        READING: 0,
        WRITING: 0,
        SPEAKING: 0
      },
      recentTests: [],
      totalTests: 0
    };
  }
};

export default {
  getExamList,
  getExamById,
  createUserExam,
  getMyExams,
  getUserExamById,
  submitRLPart,
  getListeningPart,
  getReadingPart,
  getExamParts,
  getQuestionsByExamPart,
  getSpeakingQuestions,
  getSpeakingQuestionsByExamId,
  getWritingQuestionsByExamId,
  getListeningQuestionsByExamId,
  getReadingQuestionsByExamId,
  evaluateWriting,
  submitWritingExam,
  getUserAnswers,
  createListeningQuestion,
  createReadingQuestion,
  getAllListeningSections,
  getListeningResult,
  getReadingResult,
  getWritingResult,
  getSpeakingResult,
  getUserStats,
  getRecentTests,
  getUserInfo
};

