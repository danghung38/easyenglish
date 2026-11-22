import axios from "axios";
import { DJANGO_BASE_URL } from "../utils/config";

// Get axios instance with auth token
const getAuthHeaders = () => {
  const authTokensStr = localStorage.getItem('authTokens');
  if (!authTokensStr) {
    console.warn('⚠️ No authTokens found in localStorage');
    return {};
  }
  
  try {
    const authTokens = JSON.parse(authTokensStr);
    const token = authTokens.token;
    
    if (!token) {
      console.warn('⚠️ No token field found in authTokens');
      return {};
    }
    
    // Log token format for debugging (only first/last few chars for security)
    console.log('🔑 Token format check:', token.substring(0, 20) + '...' + token.substring(token.length - 20));
    
    return token ? { Authorization: `Bearer ${token}` } : {};
  } catch (e) {
    console.error('Error parsing auth tokens:', e);
    return {};
  }
};

// Upload image for reading section (using SectionName enum)
export const createReadingSection = async (examPartId, sectionName, imageFile) => {
  try {
    const formData = new FormData();
    formData.append("examPartId", examPartId);
    formData.append("section", sectionName); // Changed from passageName to section (SECTION1-5)
    formData.append("image", imageFile);
    
    const response = await axios.post(`${DJANGO_BASE_URL}/questions/reading/section/image`, formData, {
      headers: getAuthHeaders(),
    });
    return response.data;
  } catch (error) {
    console.error("Error uploading reading section image:", error);
    throw error;
  }
};

// Add single reading question to section
export const addReadingQuestion = async (questionData) => {
  try {
    const response = await axios.post(`${DJANGO_BASE_URL}/questions/reading/question`, questionData, {
      headers: {
        ...getAuthHeaders(),
        "Content-Type": "application/json",
      },
    });
    return response.data;
  } catch (error) {
    console.error("Error adding reading question:", error);
    throw error;
  }
};

// Add multiple reading questions to section (batch)
export const addReadingQuestionsBatch = async (examPartId, section, questions) => {
  try {
    const requestData = {
      examPartId: examPartId,
      section: section,
      questions: questions
    };
    
    const response = await axios.post(`${DJANGO_BASE_URL}/questions/reading/questions-batch`, requestData, {
      headers: {
        ...getAuthHeaders(),
        "Content-Type": "application/json",
      },
    });
    return response.data;
  } catch (error) {
    console.error("Error adding reading questions batch:", error);
    throw error;
  }
};

// Add multiple listening questions to section (batch)
export const addListeningQuestionsBatch = async (examPartId, section, questions) => {
  try {
    const requestData = {
      examPartId: examPartId,
      section: section,
      questions: questions
    };
    
    const response = await axios.post(`${DJANGO_BASE_URL}/questions/listening/questions-batch`, requestData, {
      headers: {
        ...getAuthHeaders(),
        "Content-Type": "application/json",
      },
    });
    return response.data;
  } catch (error) {
    console.error("Error adding listening questions batch:", error);
    throw error;
  }
};

// Update reading question
export const updateReadingQuestion = async (questionId, questionData) => {
  try {
    // Find the correct option and create options array with tempId
    const filledOptions = questionData.options.filter(opt => opt && opt.trim() !== "");
    const optionsWithTempId = filledOptions.map((content, index) => ({
      tempId: `a${index + 1}`,
      content: content
    }));
    
    // Convert correctTempId from "temp-X" format to "aX" format
    let correctTempId = questionData.correctTempId;
    if (correctTempId && correctTempId.startsWith('temp-')) {
      const correctIndex = parseInt(correctTempId.replace("temp-", ""));
      correctTempId = `a${correctIndex + 1}`;
    }
    
    // Create update request matching UpdateQuestionReadingRequest DTO
    const updateRequest = {
      content: questionData.questionText,
      maxScore: questionData.points,
      options: optionsWithTempId,
      correctTempId: correctTempId,
      explain: questionData.explanation || null
    };
    
    const response = await axios.put(`${DJANGO_BASE_URL}/questions/reading/${questionId}`, updateRequest, {
      headers: {
        ...getAuthHeaders(),
        "Content-Type": "application/json",
      },
    });
    return response.data;
  } catch (error) {
    console.error("Error updating reading question:", error);
    throw error;
  }
};

// Create simple listening question (without audio)
export const createSimpleListeningQuestion = async (questionData) => {
  try {
    const response = await axios.post(`${DJANGO_BASE_URL}/questions/listening/simple`, questionData, {
      headers: {
        ...getAuthHeaders(),
        "Content-Type": "application/json",
      },
    });
    return response.data;
  } catch (error) {
    console.error("Error creating simple listening question:", error);
    throw error;
  }
};

// Update listening question
export const updateListeningQuestion = async (questionId, questionData, audioFile = null) => {
  try {
    // Backend expects @RequestPart which requires FormData
    const formData = new FormData();
    
    // Find the correct option and create options array with tempId
    const filledOptions = questionData.options.filter(opt => opt && opt.trim() !== "");
    const optionsWithTempId = filledOptions.map((content, index) => ({
      tempId: `a${index + 1}`,
      content: content
    }));
    
    // Convert correctTempId from "temp-X" format to "aX" format
    let correctTempId = questionData.correctTempId;
    if (correctTempId && correctTempId.startsWith('temp-')) {
      const correctIndex = parseInt(correctTempId.replace("temp-", ""));
      correctTempId = `a${correctIndex + 1}`;
    }
    
    // Create update request matching UpdateQuestionListeningRequest DTO
    const updateRequest = {
      content: questionData.questionText,
      maxScore: questionData.points,
      options: optionsWithTempId,
      correctTempId: correctTempId,
      explain: questionData.explanation || null
    };
    
    formData.append("question", new Blob([JSON.stringify(updateRequest)], { type: 'application/json' }));
    
    if (audioFile) {
      formData.append("audio", audioFile);
    }
    
    const response = await axios.put(`${DJANGO_BASE_URL}/questions/listening/${questionId}`, formData, {
      headers: getAuthHeaders(),
    });
    return response.data;
  } catch (error) {
    console.error("Error updating listening question:", error);
    throw error;
  }
};

// Delete question
export const deleteQuestion = async (questionId) => {
  try {
    const response = await axios.delete(`${DJANGO_BASE_URL}/questions/${questionId}`, {
      headers: getAuthHeaders(),
    });
    return response.data;
  } catch (error) {
    console.error("Error deleting question:", error);
    throw error;
  }
};

// Get questions by exam part id
export const getQuestionsByExamPartId = async (examPartId) => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/questions/list`, {
      params: {
        examPartId: examPartId,
        pageNo: 0,
        pageSize: 1000,
        sortBy: 'id'
      },
      headers: getAuthHeaders(),
    });
    return response.data;
  } catch (error) {
    console.error("Error fetching questions:", error);
    throw error;
  }
};

// Get questions by exam id and skill type
export const getQuestionsByExamIdAndSkillType = async (examId, skillType) => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/questions/exam/${examId}/skill-type/${skillType}`, {
      params: {
        pageNo: 0,
        pageSize: 1000,
        sortBy: 'id:asc'
      },
      headers: getAuthHeaders(),
    });
    return response.data;
  } catch (error) {
    console.error("Error fetching questions by exam and skill type:", error);
    throw error;
  }
};

// Upload audio for existing listening section (questions already created)
export const uploadSectionAudio = async (examPartId, sectionName, audioFile, transcript = null) => {
  try {
    const formData = new FormData();
    formData.append("audio", audioFile);
    if (transcript) {
      formData.append("transcript", transcript);
    }
    
    const response = await axios.post(
      `${DJANGO_BASE_URL}/questions/listening/section/audio`,
      formData,
      {
        params: { examPartId, sectionName },
        headers: getAuthHeaders(),
      }
    );
    return response.data;
  } catch (error) {
    console.error("Error uploading section audio:", error);
    throw error;
  }
};

// Create reading passage with multiple questions and image (all-in-one)


// Create writing question
export const createWritingQuestion = async (questionData, imageFile = null) => {
  try {
    const formData = new FormData();
    formData.append("question", JSON.stringify(questionData));
    if (imageFile) {
      formData.append("image", imageFile);
    }
    
    const response = await axios.post(`${DJANGO_BASE_URL}/questions/writing`, formData, {
      headers: getAuthHeaders(),
    });
    return response.data;
  } catch (error) {
    console.error("Error creating writing question:", error);
    throw error;
  }
};

// Update writing question
export const updateWritingQuestion = async (questionId, questionData, imageFile = null) => {
  try {
    const formData = new FormData();
    formData.append("question", new Blob([JSON.stringify(questionData)], { type: 'application/json' }));
    if (imageFile) {
      formData.append("image", imageFile);
    }
    
    const response = await axios.put(`${DJANGO_BASE_URL}/questions/writing/${questionId}`, formData, {
      headers: getAuthHeaders(),
    });
    return response.data;
  } catch (error) {
    console.error("Error updating writing question:", error);
    throw error;
  }
};

// Create speaking question
export const createSpeakingQuestion = async (questionData) => {
  try {
    const response = await axios.post(`${DJANGO_BASE_URL}/questions/speaking`, questionData, {
      headers: {
        ...getAuthHeaders(),
        "Content-Type": "application/json",
      },
    });
    return response.data;
  } catch (error) {
    console.error("Error creating speaking question:", error);
    throw error;
  }
};

// Update speaking question
export const updateSpeakingQuestion = async (questionId, questionData) => {
  try {
    const response = await axios.put(`${DJANGO_BASE_URL}/questions/speaking/${questionId}`, questionData, {
      headers: {
        ...getAuthHeaders(),
        "Content-Type": "application/json",
      },
    });
    return response.data;
  } catch (error) {
    console.error("Error updating speaking question:", error);
    throw error;
  }
};
