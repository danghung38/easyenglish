import axios from "axios";
import { DJANGO_BASE_URL } from "../utils/config";

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

// Get all exams with pagination
export const getAllExams = async (pageNo = 1, pageSize = 20, sortBy = "id") => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/exams/list`, {
      params: { pageNo, pageSize, sortBy },
      headers: getAuthHeaders(),
    });
    return response.data;
  } catch (error) {
    console.error("Error fetching exams:", error);
    throw error;
  }
};

// Create new exam
export const createExam = async (examData, imageFile) => {
  try {
    const formData = new FormData();
    
    // Thêm exam data dưới dạng JSON blob
    formData.append("exam", new Blob([JSON.stringify(examData)], {
      type: "application/json"
    }));
    
    // Thêm image file nếu có
    if (imageFile) {
      formData.append("image", imageFile);
    }
    
    const response = await axios.post(`${DJANGO_BASE_URL}/exams`, formData, {
      headers: {
        ...getAuthHeaders(),
        "Content-Type": "multipart/form-data",
      },
    });
    return response.data;
  } catch (error) {
    console.error("Error creating exam:", error);
    throw error;
  }
};

// Update exam
export const updateExam = async (examId, examData, imageFile) => {
  try {
    const formData = new FormData();
    
    // Thêm exam data dưới dạng JSON blob
    formData.append("exam", new Blob([JSON.stringify(examData)], {
      type: "application/json"
    }));
    
    // Thêm image file nếu có
    if (imageFile) {
      formData.append("image", imageFile);
    }
    
    const response = await axios.put(`${DJANGO_BASE_URL}/exams/${examId}`, formData, {
      headers: {
        ...getAuthHeaders(),
        "Content-Type": "multipart/form-data",
      },
    });
    return response.data;
  } catch (error) {
    console.error("Error updating exam:", error);
    throw error;
  }
};

// Delete exam
export const deleteExam = async (examId) => {
  try {
    const response = await axios.delete(`${DJANGO_BASE_URL}/exams/${examId}`, {
      headers: getAuthHeaders(),
    });
    return response.data;
  } catch (error) {
    console.error("Error deleting exam:", error);
    throw error;
  }
};

// Get exam parts by exam ID
export const getExamPartsByExamId = async (examId, pageNo = 1, pageSize = 100, sortBy = "id") => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/exams/${examId}/parts`, {
      params: { pageNo, pageSize, sortBy },
      headers: getAuthHeaders(),
    });
    return response.data;
  } catch (error) {
    console.error("Error fetching exam parts:", error);
    throw error;
  }
};
