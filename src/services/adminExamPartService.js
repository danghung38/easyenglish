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

// Get all exam parts by exam id
export const getAllExamParts = async (examId) => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/examparts/list`, {
      params: { examId },
      headers: getAuthHeaders(),
    });
    return response.data;
  } catch (error) {
    console.error("Error fetching exam parts:", error);
    throw error;
  }
};

// Create new exam part
export const createExamPart = async (examPartData) => {
  try {
    const response = await axios.post(`${DJANGO_BASE_URL}/examparts`, examPartData, {
      headers: {
        ...getAuthHeaders(),
        "Content-Type": "application/json",
      },
    });
    return response.data;
  } catch (error) {
    console.error("Error creating exam part:", error);
    throw error;
  }
};

// Update exam part
export const updateExamPart = async (examPartId, examPartData) => {
  try {
    const response = await axios.put(`${DJANGO_BASE_URL}/examparts/${examPartId}`, examPartData, {
      headers: {
        ...getAuthHeaders(),
        "Content-Type": "application/json",
      },
    });
    return response.data;
  } catch (error) {
    console.error("Error updating exam part:", error);
    throw error;
  }
};

// Delete exam part
export const deleteExamPart = async (examPartId) => {
  try {
    const response = await axios.delete(`${DJANGO_BASE_URL}/examparts/${examPartId}`, {
      headers: getAuthHeaders(),
    });
    return response.data;
  } catch (error) {
    console.error("Error deleting exam part:", error);
    throw error;
  }
};
