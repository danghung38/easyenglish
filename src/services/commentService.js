/**
 * Comment Service - Handles all comment-related API calls
 * Integrates with Spring Boot backend comment APIs
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
 * Get all comments for a specific exam
 * @param {number} examId - Exam ID
 * @returns {Promise} ApiResponse with List<CommentResponse>
 */
export const getCommentsByExamId = async (examId) => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/comments/exam/${examId}`, {
      headers: getAuthHeaders()
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching comments:', error);
    throw error;
  }
};

/**
 * Get a specific comment by ID
 * @param {number} commentId - Comment ID
 * @returns {Promise} ApiResponse with CommentResponse
 */
export const getCommentById = async (commentId) => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/comments/${commentId}`, {
      headers: getAuthHeaders()
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching comment:', error);
    throw error;
  }
};

/**
 * Create a new comment
 * @param {Object} commentData - { examId, content }
 * @returns {Promise} ApiResponse with CommentResponse
 */
export const createComment = async (commentData) => {
  try {
    const response = await axios.post(`${DJANGO_BASE_URL}/comments`, commentData, {
      headers: {
        ...getAuthHeaders(),
        'Content-Type': 'application/json'
      }
    });
    return response.data;
  } catch (error) {
    console.error('Error creating comment:', error);
    throw error;
  }
};

/**
 * Update an existing comment
 * @param {number} commentId - Comment ID
 * @param {Object} commentData - { examId, content }
 * @returns {Promise} ApiResponse with CommentResponse
 */
export const updateComment = async (commentId, commentData) => {
  try {
    const response = await axios.put(`${DJANGO_BASE_URL}/comments/${commentId}`, commentData, {
      headers: {
        ...getAuthHeaders(),
        'Content-Type': 'application/json'
      }
    });
    return response.data;
  } catch (error) {
    console.error('Error updating comment:', error);
    throw error;
  }
};

/**
 * Delete a comment
 * @param {number} commentId - Comment ID
 * @returns {Promise} ApiResponse with void
 */
export const deleteComment = async (commentId) => {
  try {
    const response = await axios.delete(`${DJANGO_BASE_URL}/comments/${commentId}`, {
      headers: getAuthHeaders()
    });
    return response.data;
  } catch (error) {
    console.error('Error deleting comment:', error);
    throw error;
  }
};
