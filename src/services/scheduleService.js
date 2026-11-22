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

export const createScheduleEvent = async (data) => {
  try {
    const response = await axios.post(`${DJANGO_BASE_URL}/schedule`, data, {
      headers: getAuthHeaders()
    });
    return response.data;
  } catch (error) {
    console.error('Error creating schedule event:', error);
    throw error;
  }
};

export const getMyScheduleEvents = async () => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/schedule`, {
      headers: getAuthHeaders()
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching schedule events:', error);
    throw error;
  }
};

export const getUpcomingEvents = async () => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/schedule/upcoming`, {
      headers: getAuthHeaders()
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching upcoming events:', error);
    throw error;
  }
};

export const markAsCompleted = async (id) => {
  try {
    const response = await axios.patch(`${DJANGO_BASE_URL}/schedule/${id}/complete`, {}, {
      headers: getAuthHeaders()
    });
    return response.data;
  } catch (error) {
    console.error('Error marking as completed:', error);
    throw error;
  }
};

export const deleteScheduleEvent = async (id) => {
  try {
    const response = await axios.delete(`${DJANGO_BASE_URL}/schedule/${id}`, {
      headers: getAuthHeaders()
    });
    return response.data;
  } catch (error) {
    console.error('Error deleting schedule event:', error);
    throw error;
  }
};
