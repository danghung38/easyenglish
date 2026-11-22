import React, { useState, useEffect } from "react";
import { getAllExams, createExam, updateExam, deleteExam } from "../../services/adminExamService";

const ExamsManagement = () => {
  const [exams, setExams] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editingExam, setEditingExam] = useState(null);
  const [pageNo, setPageNo] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  const [formData, setFormData] = useState({
    title: "",
    description: "",
    totalDuration: 0,
  });
  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);

  useEffect(() => {
    fetchExams();
  }, [pageNo]);

  const fetchExams = async () => {
    try {
      setLoading(true);
      const response = await getAllExams(pageNo, 20, "id");
      if (response.code === 200 && response.result) {
        setExams(response.result.items || []);
        setTotalPages(response.result.totalPage || 1);
      }
      setError(null);
    } catch (err) {
      setError("Failed to fetch exams: " + err.message);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenModal = (exam = null) => {
    if (exam) {
      setEditingExam(exam);
      setFormData({
        title: exam.title || "",
        description: exam.description || "",
        totalDuration: exam.totalDuration || 0,
      });
      setImagePreview(exam.imageUrl || null);
    } else {
      setEditingExam(null);
      setFormData({
        title: "",
        description: "",
        totalDuration: 0,
      });
      setImagePreview(null);
    }
    setImageFile(null);
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingExam(null);
    setImageFile(null);
    setImagePreview(null);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setImageFile(file);
      // Tạo preview URL
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreview(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingExam) {
        await updateExam(editingExam.id, formData, imageFile);
      } else {
        await createExam(formData, imageFile);
      }
      handleCloseModal();
      fetchExams();
    } catch (err) {
      setError("Failed to save exam: " + err.message);
      console.error(err);
    }
  };

  const handleDelete = async (examId) => {
    if (window.confirm("Are you sure you want to delete this exam?")) {
      try {
        await deleteExam(examId);
        fetchExams();
      } catch (err) {
        setError("Failed to delete exam: " + err.message);
        console.error(err);
      }
    }
  };

  if (loading) {
    return <div className="loading">Loading exams...</div>;
  }

  return (
    <div className="admin-page">
      <div className="admin-page-header">
        <h1>Exams Management</h1>
        <button className="btn-primary" onClick={() => handleOpenModal()}>
          + Add New Exam
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {exams.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">📝</div>
          <h3>No exams found</h3>
          <p>Get started by adding a new exam</p>
        </div>
      ) : (
        <>
          <table className="admin-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Image</th>
                <th>Title</th>
                <th>Description</th>
                <th>Total Duration (min)</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {exams.map((exam) => (
                <tr key={exam.id}>
                  <td>{exam.id}</td>
                  <td>
                    {exam.imageUrl ? (
                      <img src={exam.imageUrl} alt={exam.title} style={{width: '60px', height: '60px', objectFit: 'cover', borderRadius: '4px'}} />
                    ) : (
                      <span>No image</span>
                    )}
                  </td>
                  <td>{exam.title}</td>
                  <td>{exam.description}</td>
                  <td>{exam.totalDuration}</td>
                  <td>
                    <div className="action-buttons">
                      <button
                        className="btn-edit"
                        onClick={() => handleOpenModal(exam)}
                      >
                        Edit
                      </button>
                      <button
                        className="btn-delete"
                        onClick={() => handleDelete(exam.id)}
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="pagination">
            <button
              disabled={pageNo === 1}
              onClick={() => setPageNo((prev) => prev - 1)}
            >
              Previous
            </button>
            <span>
              Page {pageNo} of {totalPages}
            </span>
            <button
              disabled={pageNo >= totalPages}
              onClick={() => setPageNo((prev) => prev + 1)}
            >
              Next
            </button>
          </div>
        </>
      )}

      {showModal && (
        <div className="modal-overlay" onClick={handleCloseModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>{editingExam ? "Edit Exam" : "Add New Exam"}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Title *</label>
                <input
                  type="text"
                  name="title"
                  value={formData.title}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div className="form-group">
                <label>Description *</label>
                <textarea
                  name="description"
                  value={formData.description}
                  onChange={handleInputChange}
                  rows="4"
                  required
                />
              </div>

              <div className="form-group">
                <label>Total Duration (minutes) *</label>
                <input
                  type="number"
                  name="totalDuration"
                  value={formData.totalDuration}
                  onChange={handleInputChange}
                  min="0"
                  required
                />
              </div>

              <div className="form-group">
                <label>Exam Image</label>
                <input
                  type="file"
                  accept="image/*"
                  onChange={handleImageChange}
                />
                {imagePreview && (
                  <div style={{marginTop: '10px'}}>
                    <img 
                      src={imagePreview} 
                      alt="Preview" 
                      style={{maxWidth: '200px', maxHeight: '200px', objectFit: 'contain', borderRadius: '8px'}} 
                    />
                  </div>
                )}
              </div>

              <div className="form-actions">
                <button type="button" className="btn-secondary" onClick={handleCloseModal}>
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  {editingExam ? "Update" : "Create"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ExamsManagement;
