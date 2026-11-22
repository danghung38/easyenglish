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

// Get all user exams (admin can see all exams from all users)
export const getAllUserExams = async () => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/userexams/admin/all`, {
      headers: getAuthHeaders(),
    });
    return response.data;
  } catch (error) {
    console.error("Error fetching user exams:", error);
    throw error;
  }
};

// Search user exams by query (admin only)
export const searchUserExams = async (query) => {
  try {
    const params = query ? { query } : {};
    const response = await axios.get(`${DJANGO_BASE_URL}/userexams/admin/search`, {
      params,
      headers: getAuthHeaders(),
    });
    return response.data;
  } catch (error) {
    console.error("Error searching user exams:", error);
    throw error;
  }
};

// Get user exam by ID with details
export const getUserExamById = async (userExamId) => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/userexams/${userExamId}`, {
      headers: getAuthHeaders(),
    });
    return response.data;
  } catch (error) {
    console.error("Error fetching user exam details:", error);
    throw error;
  }
};

// Get leaderboard
export const getLeaderboard = async (limit = 10) => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/userexams/leaderboard`, {
      params: { limit },
      headers: getAuthHeaders(),
    });
    return response.data;
  } catch (error) {
    console.error("Error fetching leaderboard:", error);
    throw error;
  }
};
