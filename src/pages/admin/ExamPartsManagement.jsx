import React, { useState, useEffect } from "react";
import { getAllExamParts, createExamPart, updateExamPart, deleteExamPart } from "../../services/adminExamPartService";
import { getAllExams } from "../../services/adminExamService";

const ExamPartsManagement = () => {
  const [examParts, setExamParts] = useState([]);
  const [exams, setExams] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editingExamPart, setEditingExamPart] = useState(null);
  const [selectedExamId, setSelectedExamId] = useState(null);

  const [formData, setFormData] = useState({
    examId: "",
    skillType: "READING",
    duration: 0,
  });

  useEffect(() => {
    fetchExams();
  }, []);

  useEffect(() => {
    if (selectedExamId) {
      fetchExamParts();
    }
  }, [selectedExamId]);

  const fetchExams = async () => {
    try {
      const response = await getAllExams(1, 100, "id");
      if (response.code === 200 && response.result) {
        setExams(response.result.items || []);
      }
    } catch (err) {
      console.error("Failed to fetch exams:", err);
    }
  };

  const fetchExamParts = async () => {
    if (!selectedExamId) return;
    
    try {
      setLoading(true);
      const response = await getAllExamParts(selectedExamId);
      if (response.code === 200) {
        setExamParts(response.result || []);
      }
      setError(null);
    } catch (err) {
      setError("Failed to fetch exam parts: " + err.message);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenModal = (examPart = null) => {
    if (examPart) {
      setEditingExamPart(examPart);
      setFormData({
        examId: examPart.examId || selectedExamId || "",
        skillType: examPart.skillType || "READING",
        duration: examPart.duration || 0,
      });
    } else {
      setEditingExamPart(null);
      setFormData({
        examId: selectedExamId || "",
        skillType: "READING",
        duration: 0,
      });
    }
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingExamPart(null);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingExamPart) {
        await updateExamPart(editingExamPart.id, formData);
      } else {
        await createExamPart(formData);
      }
      handleCloseModal();
      fetchExamParts();
    } catch (err) {
      setError("Failed to save exam part: " + err.message);
      console.error(err);
    }
  };

  const handleDelete = async (examPartId) => {
    if (window.confirm("Are you sure you want to delete this exam part?")) {
      try {
        await deleteExamPart(examPartId);
        fetchExamParts();
      } catch (err) {
        setError("Failed to delete exam part: " + err.message);
        console.error(err);
      }
    }
  };

  return (
    <div className="admin-page">
      <div className="admin-page-header">
        <h1>Exam Parts Management</h1>
        <button 
          className="btn-primary" 
          onClick={() => handleOpenModal()}
          disabled={!selectedExamId}
        >
          + Add New Exam Part
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="form-group" style={{ marginBottom: "20px", maxWidth: "400px" }}>
        <label>Select Exam:</label>
        <select
          value={selectedExamId || ""}
          onChange={(e) => setSelectedExamId(e.target.value)}
          style={{ padding: "10px", fontSize: "1rem" }}
        >
          <option value="">-- Select an Exam --</option>
          {exams.map((exam) => (
            <option key={exam.id} value={exam.id}>
              {exam.title}
            </option>
          ))}
        </select>
      </div>

      {!selectedExamId ? (
        <div className="empty-state">
          <div className="empty-icon">📋</div>
          <h3>Please select an exam</h3>
          <p>Choose an exam from the dropdown to manage its parts</p>
        </div>
      ) : loading ? (
        <div className="loading">Loading exam parts...</div>
      ) : examParts.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">📋</div>
          <h3>No exam parts found</h3>
          <p>Get started by adding a new exam part</p>
        </div>
      ) : (
        <table className="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Skill Type</th>
              <th>Duration (min)</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {examParts.map((examPart) => (
              <tr key={examPart.id}>
                <td>{examPart.id}</td>
                <td>{examPart.skillType}</td>
                <td>{examPart.duration}</td>
                <td>
                  <div className="action-buttons">
                    <button
                      className="btn-edit"
                      onClick={() => handleOpenModal(examPart)}
                    >
                      Edit
                    </button>
                    <button
                      className="btn-delete"
                      onClick={() => handleDelete(examPart.id)}
                    >
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {showModal && (
        <div className="modal-overlay" onClick={handleCloseModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>{editingExamPart ? "Edit Exam Part" : "Add New Exam Part"}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Exam *</label>
                <select
                  name="examId"
                  value={formData.examId}
                  onChange={handleInputChange}
                  required
                >
                  <option value="">-- Select Exam --</option>
                  {exams.map((exam) => (
                    <option key={exam.id} value={exam.id}>
                      {exam.title}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Skill Type *</label>
                <select
                  name="skillType"
                  value={formData.skillType}
                  onChange={handleInputChange}
                  required
                >
                  <option value="READING">READING</option>
                  <option value="LISTENING">LISTENING</option>
                  <option value="WRITING">WRITING</option>
                  <option value="SPEAKING">SPEAKING</option>
                </select>
              </div>

              <div className="form-group">
                <label>Duration (minutes)</label>
                <input
                  type="number"
                  name="duration"
                  value={formData.duration}
                  onChange={handleInputChange}
                  min="0"
                />
              </div>

              <div className="form-actions">
                <button type="button" className="btn-secondary" onClick={handleCloseModal}>
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  {editingExamPart ? "Update" : "Create"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ExamPartsManagement;
