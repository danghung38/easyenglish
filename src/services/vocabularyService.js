/**
 * Vocabulary Service - Handles all vocabulary-related API calls
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
 * Get all vocabulary topics with word count
 * @returns {Promise} ApiResponse with List<VocabularyTopicResponse>
 */
export const getAllVocabularyTopics = async () => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/vocabulary/topics`, {
      headers: getAuthHeaders()
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching vocabulary topics:', error);
    throw error;
  }
};

/**
 * Get vocabularies by topic name
 * @param {string} topicName - Topic name (Environment, Technology, etc.)
 * @returns {Promise} ApiResponse with List<VocabularyResponse>
 */
export const getVocabulariesByTopic = async (topicName) => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/vocabulary/topics/${topicName}/words`, {
      headers: getAuthHeaders()
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching vocabularies by topic:', error);
    throw error;
  }
};

/**
 * Get vocabulary by ID
 * @param {number} id - Vocabulary ID
 * @returns {Promise} ApiResponse with VocabularyResponse
 */
export const getVocabularyById = async (id) => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/vocabulary/words/${id}`, {
      headers: getAuthHeaders()
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching vocabulary by ID:', error);
    throw error;
  }
};

/**
 * Create new vocabulary (Admin only)
 * @param {FormData} formData - Form data with vocabulary info and files
 * @returns {Promise} ApiResponse with VocabularyResponse
 */
export const createVocabulary = async (formData) => {
  try {
    const response = await axios.post(`${DJANGO_BASE_URL}/vocabulary/words`, formData, {
      headers: {
        ...getAuthHeaders(),
        'Content-Type': 'multipart/form-data'
      }
    });
    return response.data;
  } catch (error) {
    console.error('Error creating vocabulary:', error);
    throw error;
  }
};

/**
 * Update vocabulary (Admin only)
 * @param {number} id - Vocabulary ID
 * @param {FormData} formData - Form data with vocabulary info and files
 * @returns {Promise} ApiResponse with VocabularyResponse
 */
export const updateVocabulary = async (id, formData) => {
  try {
    const response = await axios.put(`${DJANGO_BASE_URL}/vocabulary/words/${id}`, formData, {
      headers: {
        ...getAuthHeaders(),
        'Content-Type': 'multipart/form-data'
      }
    });
    return response.data;
  } catch (error) {
    console.error('Error updating vocabulary:', error);
    throw error;
  }
};

/**
 * Delete vocabulary (Admin only)
 * @param {number} id - Vocabulary ID
 * @returns {Promise} ApiResponse
 */
export const deleteVocabulary = async (id) => {
  try {
    const response = await axios.delete(`${DJANGO_BASE_URL}/vocabulary/words/${id}`, {
      headers: getAuthHeaders()
    });
    return response.data;
  } catch (error) {
    console.error('Error deleting vocabulary:', error);
    throw error;
  }
};
