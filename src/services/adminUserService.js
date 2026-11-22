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

// Get all users with pagination
export const getAllUsers = async (pageNo = 1, pageSize = 20, sortBy = "id") => {
  try {
    const response = await axios.get(`${DJANGO_BASE_URL}/users/list`, {
      params: { pageNo, pageSize, sortBy },
      headers: getAuthHeaders(),
    });
    return response.data;
  } catch (error) {
    console.error("Error fetching users:", error);
    throw error;
  }
};

// Create new user
export const createUser = async (userData) => {
  try {
    const response = await axios.post(`${DJANGO_BASE_URL}/users`, userData, {
      headers: {
        ...getAuthHeaders(),
        "Content-Type": "application/json",
      },
    });
    return response.data;
  } catch (error) {
    console.error("Error creating user:", error);
    throw error;
  }
};

// Update user
export const updateUser = async (userId, userData) => {
  try {
    const formData = new FormData();
    formData.append("user", JSON.stringify(userData));
    
    const response = await axios.put(`${DJANGO_BASE_URL}/users`, formData, {
      headers: getAuthHeaders(),
    });
    return response.data;
  } catch (error) {
    console.error("Error updating user:", error);
    throw error;
  }
};

// Get user by ID (from the list)
export const getUserById = async (userId) => {
  try {
    // Since we don't have a direct endpoint, we'll search through the list
    const response = await getAllUsers(1, 1000, "id");
    if (response.code === 200 && response.result) {
      const user = response.result.items.find(u => u.id === userId);
      if (user) {
        return { code: 200, result: user };
      }
      throw new Error("User not found");
    }
    throw new Error("Failed to fetch users");
  } catch (error) {
    console.error("Error fetching user by ID:", error);
    throw error;
  }
};

// Delete user (Note: API might need to be added in backend)
export const deleteUser = async (userId) => {
  try {
    const response = await axios.delete(`${DJANGO_BASE_URL}/users/${userId}`, {
      headers: getAuthHeaders(),
    });
    return response.data;
  } catch (error) {
    console.error("Error deleting user:", error);
    throw error;
  }
};
