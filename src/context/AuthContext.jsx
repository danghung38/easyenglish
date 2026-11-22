import { useContext, useState, useEffect, createContext } from "react";
import jwt_decode from "jwt-decode";
import { useNavigate } from "react-router-dom";
import NotificationContext from "./layout/NotificationContext";
import useAxios from "../utils/useAxios";
import axios from "axios";
import LoadingContext from "./layout/LoadingContext";
import { DJANGO_BASE_URL } from "../utils/config";
import usePublicAxios from "../utils/usePublicAxios";

const AuthContext = createContext();
export const AuthProvider = ({ children }) => {
  const api = usePublicAxios();
  const [loading, setLoading] = useState(true);
  const [loadingBar, setLoadingBar] = useContext(LoadingContext);
  const [registrationError, setRegistrationError] = useState(null);
  const [loginError, setLoginError] = useState(null);

  const [user, setUser] = useState(() =>
    localStorage.getItem("user")
      ? JSON.parse(localStorage.getItem("user"))
      : null
  );
  const [notification, setNotification] = useContext(NotificationContext);

  let navigate = useNavigate();

  const [authTokens, setAuthTokens] = useState(() =>
    localStorage.getItem("authTokens")
      ? JSON.parse(localStorage.getItem("authTokens"))
      : null
  );

  useEffect(() => {
    if (authTokens && authTokens.token) {
      try {
        // Decode JWT token to get user info
        const decodedToken = jwt_decode(authTokens.token);
        setUser({
          userId: authTokens.userId,
          username: decodedToken.sub, // subject contains username
          fullName: authTokens.fullName,
          role: authTokens.role,
          scope: decodedToken.scope, // roles and permissions
          exp: decodedToken.exp, // expiration time
        });
      } catch (error) {
        console.error("Error decoding token:", error);
      }
      setLoading(false);
    } else {
      setLoading(false);
    }
  }, [authTokens]);

  const loginUser = async (values, handleSuccess) => {
    setLoadingBar(true);
    setLoginError(null); // Clear previous errors
    try {
      console.log("Attempting login with email:", values.email);
      
      // Backend sẽ tìm user bằng email hoặc username
      const response = await api.post(`${DJANGO_BASE_URL}/auth/login`, {
        username: values.email, // Send email as username, backend will handle both
        password: values.password,
      });

      console.log("Login response:", response.data);
      
      const { result } = response.data;
      
      if (!result || !result.token) {
        throw new Error("Invalid response structure from server");
      }
      
      // Decode JWT token to get claims
      const decodedToken = jwt_decode(result.token);
      console.log("Decoded token:", decodedToken);
      
      // Create auth tokens object
      const authData = {
        token: result.token,
        userId: result.userId || null,
        role: result.role,
        username: result.username,
        fullName: result.fullName
      };

      // Create user object with decoded token info
      const userData = {
        userId: result.userId || null,
        username: decodedToken.sub, // subject from JWT
        fullName: result.fullName,
        role: result.role,
        scope: decodedToken.scope, // roles and permissions from JWT
        exp: decodedToken.exp, // expiration time
        iat: decodedToken.iat, // issued at
      };

      setAuthTokens(authData);
      setUser(userData);
      localStorage.setItem("authTokens", JSON.stringify(authData));
      localStorage.setItem("user", JSON.stringify(userData));

      console.log("Login successful, user data:", userData);
      
      handleSuccess();
      navigate("/"); // Navigate to the homepage on successful login
    } catch (error) {
      console.error("Login error:", error);
      if (error.response) {
        // The server responded with a status code outside the 2xx range
        console.error("Error response:", error.response.data);
        const errorMessage = error.response.data.message || "Invalid Credentials.";
        
        // Kiểm tra nếu là lỗi account chưa verify
        if (errorMessage.includes("not verified") || error.response.data.code === 401) {
          if (errorMessage.includes("not verified")) {
            setNotification({
              title: "Account Not Verified",
              message: "Please check your email to verify your account before logging in.",
              color: "warning",
            });
            // Redirect đến trang verify-email với email
            navigate("/verify-email?email=" + encodeURIComponent(values.email));
            return;
          }
        }
        
        setLoginError(errorMessage);
      } else if (error.request) {
        // The request was made but no response was received
        console.error("No response received:", error.request);
        setLoginError("There was a problem connecting to the server.");
        setNotification({
          title: "Network Error",
          message:
            "There was a problem connecting to the server. Please try again later.",
          color: "danger",
        });
      } else {
        // Something happened in setting up the request that triggered an Error
        console.error("Error:", error.message);
        setLoginError(error.message || "An unexpected error occurred.");
      }
    } finally {
      setLoadingBar(false); // Hide loading bar after the request is completed
    }
  };

  const loginUserWithToken = (tokenData) => {
    // Decode JWT token to get user info
    const decodedToken = jwt_decode(tokenData.token);
    
    const userData = {
      userId: tokenData.userId,
      username: decodedToken.sub,
      fullName: tokenData.fullName,
      role: tokenData.role,
      scope: decodedToken.scope,
      exp: decodedToken.exp,
    };

    setAuthTokens(tokenData);
    setUser(userData); // Update the user state with the new userData
    localStorage.setItem("authTokens", JSON.stringify(tokenData));
    localStorage.setItem("user", JSON.stringify(userData)); // Store the updated userData in localStorage
  };

  const logoutUser = async () => {
    // Call backend logout endpoint to invalidate token
    if (authTokens?.token) {
      try {
        await api.post(`${DJANGO_BASE_URL}/auth/logout`, {
          token: authTokens.token,
        });
      } catch (error) {
        console.error("Logout error:", error);
        // Continue with local logout even if backend call fails
      }
    }

    setAuthTokens(null);
    setUser(null);

    // Wrap localStorage removal in a Promise
    await new Promise((resolve) => {
      localStorage.removeItem("authTokens");
      localStorage.removeItem("user");
      resolve();
    });

    // Navigate after localStorage operations are complete
    navigate("/?alert=You're successfully logged out&variant=success");
  };
  const registerUser = async (values, handleSuccess) => {
    setLoadingBar(true);

    try {
      // Format date to yyyy/MM/dd
      const dobFormatted = values.dob 
        ? new Date(values.dob).toISOString().split('T')[0].replace(/-/g, '/')
        : "2000/01/01";
        
      const response = await api.post(DJANGO_BASE_URL + "/users", {
        username: values.email,
        password: values.password1,
        fullName: values.first_name + " " + values.last_name,
        phoneNumber: values.phoneNumber || "0000000000",
        email: values.email,
        gender: values.gender || "OTHER",
        dob: dobFormatted
      });
      
      handleSuccess();
      setNotification({
        title: "Registration Successful",
        message: "Please check your email to verify your account. The verification link will expire in 30 minutes.",
        color: "success",
      });
      navigate("/verify-email?email=" + encodeURIComponent(values.email));
    } catch (error) {
      if (error.response && error.response.data) {
        setRegistrationError(error.response.data.message || "Registration failed");
      } else {
        setRegistrationError("There was a problem connecting to the server.");
      }
    } finally {
      setLoadingBar(false);
    }
  };

  const contextData = {
    user: user,
    authTokens: authTokens,
    registrationError: registrationError,
    loginError: loginError,
    setAuthTokens: setAuthTokens,
    setUser: setUser,
    loginUser: loginUser,
    logoutUser: logoutUser,
    registerUser: registerUser,
    loginUserWithToken: loginUserWithToken,
  };

  return (
    <AuthContext.Provider value={contextData}>{children}</AuthContext.Provider>
  );
};

export const useAuth = () => {
  return useContext(AuthContext);
};

export default AuthContext;
