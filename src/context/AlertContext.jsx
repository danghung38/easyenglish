import React, { createContext, useContext, useState, useCallback } from 'react';
import CustomAlert from '../components/common/CustomAlert';

const AlertContext = createContext();

export const useAlert = () => {
  const context = useContext(AlertContext);
  if (!context) {
    throw new Error('useAlert must be used within AlertProvider');
  }
  return context;
};

export const AlertProvider = ({ children }) => {
  const [alerts, setAlerts] = useState([]);

  const showAlert = useCallback((message, type = 'info', duration = 5000) => {
    const id = Date.now() + Math.random();
    const newAlert = {
      id,
      message,
      type, // 'success', 'error', 'warning', 'info'
      duration
    };

    setAlerts(prev => [...prev, newAlert]);

    if (duration > 0) {
      setTimeout(() => {
        removeAlert(id);
      }, duration);
    }

    return id;
  }, []);

  const removeAlert = useCallback((id) => {
    setAlerts(prev => prev.filter(alert => alert.id !== id));
  }, []);

  const showSuccess = useCallback((message, duration) => {
    return showAlert(message, 'success', duration);
  }, [showAlert]);

  const showError = useCallback((message, duration) => {
    return showAlert(message, 'error', duration);
  }, [showAlert]);

  const showWarning = useCallback((message, duration) => {
    return showAlert(message, 'warning', duration);
  }, [showAlert]);

  const showInfo = useCallback((message, duration) => {
    return showAlert(message, 'info', duration);
  }, [showAlert]);

  const value = {
    showAlert,
    showSuccess,
    showError,
    showWarning,
    showInfo,
    removeAlert
  };

  return (
    <AlertContext.Provider value={value}>
      {children}
      <CustomAlert alerts={alerts} onClose={removeAlert} />
    </AlertContext.Provider>
  );
};
