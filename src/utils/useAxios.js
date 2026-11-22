import axios from "axios";
import jwt_decode from "jwt-decode";
import dayjs from "dayjs";
import AuthContext from "../context/AuthContext";
import { useContext, useEffect } from "react";
import LoadingContext from "../context/layout/LoadingContext";
import { DJANGO_BASE_URL } from "./config";
import ErrorContext from "../context/layout/ErrorContext";

export const baseURL = DJANGO_BASE_URL;

const useAxios = () => {
  const { authTokens, setUser, setAuthTokens } = useContext(AuthContext);
  const [loadingBar, setLoadingBar] = useContext(LoadingContext);
  const [error, setError] = useContext(ErrorContext);

  useEffect(() => {
    // This cleanup function will hide the loadingBar when the component is unmounted
    return () => {
      setLoadingBar(false);
    };
  }, [setLoadingBar]);

  const axiosInstance = axios.create({
    baseURL,
    headers: { Authorization: `Bearer ${authTokens?.token}` },
  });

  axiosInstance.interceptors.request.use(async (req) => {
    setLoadingBar(true);
    
    // For Spring Boot backend with JWT
    if (!authTokens?.token) {
      return req;
    }

    try {
      // Decode token to check expiration
      const decodedToken = jwt_decode(authTokens.token);
      const isExpired = dayjs.unix(decodedToken.exp).diff(dayjs()) < 1;

      if (!isExpired) {
        req.headers.Authorization = `Bearer ${authTokens.token}`;
        return req;
      }

      // Token expired, refresh it
      const response = await axios.post(`${baseURL}/auth/refresh`, {
        token: authTokens.token,
      });

      const { result } = response.data;
      const newAuthData = {
        token: result.token,
        userId: authTokens.userId,
        role: authTokens.role,
        username: authTokens.username,
        fullName: authTokens.fullName,
      };

      // Decode new token
      const newDecodedToken = jwt_decode(result.token);
      const newUserData = {
        userId: authTokens.userId,
        username: newDecodedToken.sub,
        fullName: authTokens.fullName,
        role: authTokens.role,
        scope: newDecodedToken.scope,
        exp: newDecodedToken.exp,
      };

      localStorage.setItem("authTokens", JSON.stringify(newAuthData));
      localStorage.setItem("user", JSON.stringify(newUserData));

      setAuthTokens(newAuthData);
      setUser(newUserData);

      req.headers.Authorization = `Bearer ${result.token}`;
      return req;
    } catch (error) {
      console.error("Token refresh failed:", error);
      // If refresh fails, user needs to login again
      req.headers.Authorization = `Bearer ${authTokens.token}`;
      return req;
    }
  });

  axiosInstance.interceptors.response.use(
    (response) => {
      setLoadingBar(false); // Hide the loadingBar when a response is received
      return response;
    },
    (error) => {
      console.error("Error:", error);
      setLoadingBar(false); // Hide the loadingBar if there is an error
      setError(
        "Something went wrong. Please refresh the page and try again. If the problem persists, contact team@engtech.com."
      );
      return Promise.reject(error);
    }
  );

  return axiosInstance;
};

export default useAxios;
