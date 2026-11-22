import React, { useEffect } from 'react';
import { FiCheckCircle, FiAlertCircle, FiAlertTriangle, FiInfo, FiX } from 'react-icons/fi';
import '../../styles/CustomAlert.css';

const CustomAlert = ({ alerts, onClose }) => {
  const getIcon = (type) => {
    switch (type) {
      case 'success':
        return <FiCheckCircle size={24} />;
      case 'error':
        return <FiAlertCircle size={24} />;
      case 'warning':
        return <FiAlertTriangle size={24} />;
      case 'info':
      default:
        return <FiInfo size={24} />;
    }
  };

  const getTitle = (type) => {
    switch (type) {
      case 'success':
        return 'Thành công!';
      case 'error':
        return 'Lỗi!';
      case 'warning':
        return 'Cảnh báo!';
      case 'info':
      default:
        return 'Thông báo';
    }
  };

  return (
    <div className="custom-alert-container">
      {alerts.map((alert) => (
        <div
          key={alert.id}
          className={`custom-alert custom-alert-${alert.type}`}
        >
          <div className="custom-alert-icon">
            {getIcon(alert.type)}
          </div>
          <div className="custom-alert-content">
            <div className="custom-alert-title">
              {getTitle(alert.type)}
            </div>
            <div className="custom-alert-message">
              {alert.message}
            </div>
          </div>
          <button
            className="custom-alert-close"
            onClick={() => onClose(alert.id)}
            aria-label="Close alert"
          >
            <FiX size={20} />
          </button>
        </div>
      ))}
    </div>
  );
};

export default CustomAlert;
