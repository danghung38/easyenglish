import React, { useState, useEffect } from "react";
import { Modal, Form, Button, Alert } from "react-bootstrap";
import { createScheduleEvent } from "../../services/scheduleService";
import { useAlert } from "../../context/AlertContext";

const ScheduleEventModal = ({ show, handleClose, exam, onSuccess }) => {
  const { showSuccess, showError } = useAlert();
  const [formData, setFormData] = useState({
    testType: "FULL_TEST",
    scheduledDate: "",
    scheduledTime: ""
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const testTypes = [
    { value: "FULL_TEST", label: "Full Test", icon: "fe-layers", color: "primary" },
    { value: "LISTENING", label: "Listening", icon: "fe-headphones", color: "primary" },
    { value: "READING", label: "Reading", icon: "fe-book-open", color: "success" },
    { value: "WRITING", label: "Writing", icon: "fe-edit", color: "warning" },
    { value: "SPEAKING", label: "Speaking", icon: "fe-mic", color: "danger" }
  ];

  useEffect(() => {
    if (show) {
      // Set default date to tomorrow
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      setFormData(prev => ({
        ...prev,
        scheduledDate: tomorrow.toISOString().split('T')[0]
      }));
    }
  }, [show]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      if (!formData.scheduledDate || !formData.scheduledTime) {
        throw new Error("Vui lòng chọn ngày giờ");
      }

      const scheduledDateTime = `${formData.scheduledDate}T${formData.scheduledTime}:00`;

      const requestData = {
        examId: exam.id,
        testType: formData.testType,
        scheduledTime: scheduledDateTime
      };

      const response = await createScheduleEvent(requestData);
      
      // Show success with custom alert
      showSuccess('🎉 Đã lên lịch thi thành công!', 3000);
      
      if (onSuccess) onSuccess();
      handleClose();
      resetForm();
    } catch (err) {
      console.error("Error saving schedule:", err);
      const errorMessage = err.response?.data?.message || err.message || "Có lỗi xảy ra khi lên lịch thi";
      showError(errorMessage);
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setFormData({
      testType: "FULL_TEST",
      scheduledDate: "",
      scheduledTime: ""
    });
    setError(null);
  };

  return (
    <Modal show={show} onHide={handleClose} centered>
      <Modal.Header closeButton>
        <Modal.Title>Lên lịch thi</Modal.Title>
      </Modal.Header>
      <Form onSubmit={handleSubmit}>
        <Modal.Body>
          {error && <Alert variant="danger">{error}</Alert>}

          {/* Exam Info */}
          <Alert variant="info" className="mb-3">
            <div className="d-flex align-items-center">
              <i className="fe fe-book-open me-2" style={{ fontSize: '24px' }}></i>
              <div>
                <strong className="d-block">{exam?.title}</strong>
                <small className="text-muted">
                  <i className="fe fe-clock me-1"></i>
                  {exam?.totalDuration} phút
                </small>
              </div>
            </div>
          </Alert>

          {/* Test Type Selection */}
          <Form.Group className="mb-3">
            <Form.Label className="fw-bold">Loại bài thi <span className="text-danger">*</span></Form.Label>
            <div className="d-flex flex-wrap gap-2">
              {testTypes.map((type) => (
                <div 
                  key={type.value}
                  className={`border rounded px-3 py-2 ${formData.testType === type.value ? `border-${type.color} border-2 bg-${type.color} bg-opacity-10` : 'border-1'}`}
                  style={{ cursor: 'pointer', transition: 'all 0.2s', flex: '1 1 auto', minWidth: '140px' }}
                  onClick={() => setFormData(prev => ({ ...prev, testType: type.value }))}
                >
                  <Form.Check
                    type="radio"
                    id={`testType-${type.value}`}
                    name="testType"
                    value={type.value}
                    checked={formData.testType === type.value}
                    onChange={handleChange}
                    label={
                      <div className="d-flex align-items-center">
                        <i className={`fe ${type.icon} text-${type.color} me-2`} style={{ fontSize: '18px' }}></i>
                        <span className="fw-bold" style={{ fontSize: '14px' }}>{type.label}</span>
                      </div>
                    }
                    className="mb-0"
                  />
                </div>
              ))}
            </div>
          </Form.Group>

          {/* Date */}
          <Form.Group className="mb-3">
            <Form.Label>Ngày làm bài <span className="text-danger">*</span></Form.Label>
            <Form.Control
              type="date"
              name="scheduledDate"
              value={formData.scheduledDate}
              onChange={handleChange}
              min={new Date().toISOString().split('T')[0]}
              required
            />
          </Form.Group>

          {/* Time */}
          <Form.Group className="mb-3">
            <Form.Label>Giờ bắt đầu <span className="text-danger">*</span></Form.Label>
            <Form.Control
              type="time"
              name="scheduledTime"
              value={formData.scheduledTime}
              onChange={handleChange}
              required
            />
          </Form.Group>
        </Modal.Body>

        <Modal.Footer>
          <Button variant="secondary" onClick={handleClose} disabled={loading}>
            Hủy
          </Button>
          <Button variant="primary" type="submit" disabled={loading}>
            {loading ? "Đang lưu..." : "Lên lịch"}
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>
  );
};

export default ScheduleEventModal;
