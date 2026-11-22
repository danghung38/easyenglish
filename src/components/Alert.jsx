import React, { useEffect } from 'react';
import './Alert.css';

const Alert = ({ type = 'success', message, onClose, duration = 4000 }) => {
  useEffect(() => {
    if (duration) {
      const timer = setTimeout(() => {
        onClose();
      }, duration);
      return () => clearTimeout(timer);
    }
  }, [duration, onClose]);

  const icons = {
    success: '✓',
    error: '✕',
    warning: '⚠',
    info: 'ℹ'
  };

  const titles = {
    success: 'Success',
    error: 'Error',
    warning: 'Warning',
    info: 'Information'
  };

  return (
    <div className={`custom-alert custom-alert-${type}`}>
      <div className="custom-alert-icon">
        {icons[type]}
      </div>
      <div className="custom-alert-content">
        <div className="custom-alert-title">{titles[type]}</div>
        <div className="custom-alert-message">{message}</div>
      </div>
      <button className="custom-alert-close" onClick={onClose}>
        ×
      </button>
    </div>
  );
};

export default Alert;
