import React, { useState, useEffect } from "react";
import {
  getAllVocabularyTopics,
  getVocabulariesByTopic,
  createVocabulary,
  updateVocabulary,
  deleteVocabulary
} from "../../services/vocabularyService";

const VocabularyManagement = () => {
  const [vocabularies, setVocabularies] = useState([]);
  const [topics, setTopics] = useState([]);
  const [selectedTopic, setSelectedTopic] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editingVocab, setEditingVocab] = useState(null);

  const [formData, setFormData] = useState({
    word: "",
    meaning: "",
    pronunciation: "",
    example: "",
    topic: "",
  });
  const [audioFile, setAudioFile] = useState(null);
  const [imageFile, setImageFile] = useState(null);
  const [currentAudioUrl, setCurrentAudioUrl] = useState(null);
  const [currentImageUrl, setCurrentImageUrl] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);

  useEffect(() => {
    fetchTopics();
  }, []);

  useEffect(() => {
    if (selectedTopic) {
      fetchVocabularies(selectedTopic);
    }
  }, [selectedTopic]);

  const fetchTopics = async () => {
    try {
      const response = await getAllVocabularyTopics();
      if (response.code === 200) {
        setTopics(response.result || []);
        if (response.result && response.result.length > 0) {
          setSelectedTopic(response.result[0].title);
        }
      }
    } catch (err) {
      console.error("Error fetching topics:", err);
      setError("Failed to fetch topics");
    }
  };

  const fetchVocabularies = async (topic) => {
    try {
      setLoading(true);
      const response = await getVocabulariesByTopic(topic);
      if (response.code === 200) {
        setVocabularies(response.result || []);
      }
      setError(null);
    } catch (err) {
      setError("Failed to fetch vocabularies: " + err.message);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenModal = (vocab = null) => {
    if (vocab) {
      setEditingVocab(vocab);
      setFormData({
        word: vocab.word || "",
        meaning: vocab.meaning || "",
        pronunciation: vocab.pronunciation || "",
        example: vocab.example || "",
        topic: vocab.topic || "",
      });
      setCurrentAudioUrl(vocab.pronunciationAudioUrl || null);
      setCurrentImageUrl(vocab.imageUrl || null);
    } else {
      setEditingVocab(null);
      setFormData({
        word: "",
        meaning: "",
        pronunciation: "",
        example: "",
        topic: selectedTopic || "",
      });
      setCurrentAudioUrl(null);
      setCurrentImageUrl(null);
    }
    setAudioFile(null);
    setImageFile(null);
    setImagePreview(null);
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingVocab(null);
    setAudioFile(null);
    setImageFile(null);
    setCurrentAudioUrl(null);
    setCurrentImageUrl(null);
    setImagePreview(null);
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
      const formDataToSend = new FormData();
      formDataToSend.append("data", new Blob([JSON.stringify(formData)], { type: 'application/json' }));
      
      if (audioFile) {
        formDataToSend.append("audioFile", audioFile);
      }
      if (imageFile) {
        formDataToSend.append("imageFile", imageFile);
      }

      if (editingVocab) {
        await updateVocabulary(editingVocab.id, formDataToSend);
      } else {
        await createVocabulary(formDataToSend);
      }
      
      handleCloseModal();
      fetchVocabularies(selectedTopic);
      fetchTopics();
    } catch (err) {
      setError("Failed to save vocabulary: " + err.message);
      console.error(err);
    }
  };

  const handleDelete = async (vocabId) => {
    if (window.confirm("Are you sure you want to delete this vocabulary?")) {
      try {
        await deleteVocabulary(vocabId);
        fetchVocabularies(selectedTopic);
        fetchTopics();
      } catch (err) {
        setError("Failed to delete vocabulary: " + err.message);
        console.error(err);
      }
    }
  };

  return (
    <div className="admin-page">
      <div className="admin-page-header">
        <h1>Vocabulary Management</h1>
        <div style={{ display: "flex", gap: "10px", alignItems: "center" }}>
          <select
            value={selectedTopic}
            onChange={(e) => setSelectedTopic(e.target.value)}
            style={{
              padding: "10px 15px",
              borderRadius: "6px",
              border: "1px solid #ddd",
              fontSize: "14px",
            }}
          >
            {topics.map((topic) => (
              <option key={topic.title} value={topic.title}>
                {topic.title} ({topic.wordCount})
              </option>
            ))}
          </select>
          <button className="btn-primary" onClick={() => handleOpenModal()}>
            + Add New Word
          </button>
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}

      {loading ? (
        <div className="loading">Loading...</div>
      ) : vocabularies.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">📚</div>
          <h3>No vocabularies found</h3>
          <p>Add your first vocabulary word</p>
        </div>
      ) : (
        <table className="admin-table">
          <thead>
            <tr>
              <th>Word</th>
              <th>Meaning</th>
              <th>Pronunciation</th>
              <th>Example</th>
              <th>Media</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {vocabularies.map((vocab) => (
              <tr key={vocab.id}>
                <td><strong>{vocab.word}</strong></td>
                <td>{vocab.meaning}</td>
                <td style={{ fontStyle: "italic", color: "#666" }}>
                  {vocab.pronunciation || "-"}
                </td>
                <td style={{ fontSize: "13px" }}>
                  {vocab.example || "-"}
                </td>
                <td>
                  {vocab.pronunciationAudioUrl && <span title="Audio">🔊 </span>}
                  {vocab.imageUrl && <span title="Image">🖼️</span>}
                </td>
                <td>
                  <div className="action-buttons">
                    <button
                      className="btn-edit"
                      onClick={() => handleOpenModal(vocab)}
                    >
                      Edit
                    </button>
                    <button
                      className="btn-delete"
                      onClick={() => handleDelete(vocab.id)}
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
            <h2>{editingVocab ? "Edit Vocabulary" : "Add New Vocabulary"}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Word *</label>
                <input
                  type="text"
                  name="word"
                  value={formData.word}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div className="form-group">
                <label>Meaning (Vietnamese) *</label>
                <input
                  type="text"
                  name="meaning"
                  value={formData.meaning}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div className="form-group">
                <label>Topic *</label>
                <input
                  type="text"
                  name="topic"
                  value={formData.topic}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div className="form-group">
                <label>Pronunciation (IPA)</label>
                <input
                  type="text"
                  name="pronunciation"
                  value={formData.pronunciation}
                  onChange={handleInputChange}
                  placeholder="/ɪnˈvaɪrənmənt/"
                />
              </div>

              <div className="form-group">
                <label>Example</label>
                <textarea
                  name="example"
                  value={formData.example}
                  onChange={handleInputChange}
                  rows="3"
                />
              </div>

              <div className="form-group">
                <label>
                  Audio File {currentAudioUrl ? "(Optional - Leave empty to keep current)" : ""}
                </label>
                <input
                  type="file"
                  accept="audio/*"
                  onChange={(e) => setAudioFile(e.target.files[0])}
                />
                {currentAudioUrl && !audioFile && (
                  <div style={{ marginTop: '10px', padding: '10px', backgroundColor: '#f0f9ff', borderRadius: '6px', border: '1px solid #bae6fd' }}>
                    <p style={{ margin: '0 0 8px 0', fontSize: '13px', color: '#0c4a6e' }}>
                      🔊 Current Audio:
                    </p>
                    <audio controls src={currentAudioUrl} style={{ width: '100%', maxWidth: '400px' }} />
                    <p style={{ margin: '8px 0 0 0', fontSize: '12px', color: '#64748b' }}>
                      Upload a new file to replace this audio
                    </p>
                  </div>
                )}
                {audioFile && (
                  <p style={{ marginTop: '8px', color: '#059669', fontSize: '13px' }}>
                    ✓ New audio selected: {audioFile.name}
                  </p>
                )}
              </div>

              <div className="form-group">
                <label>
                  Image File {currentImageUrl ? "(Optional - Leave empty to keep current)" : ""}
                </label>
                <input
                  type="file"
                  accept="image/*"
                  onChange={(e) => {
                    const file = e.target.files[0];
                    if (file) {
                      setImageFile(file);
                      const reader = new FileReader();
                      reader.onloadend = () => {
                        setImagePreview(reader.result);
                      };
                      reader.readAsDataURL(file);
                    }
                  }}
                />
                {currentImageUrl && !imageFile && (
                  <div style={{ marginTop: '10px', padding: '10px', backgroundColor: '#f0fdf4', borderRadius: '6px', border: '1px solid #86efac' }}>
                    <p style={{ margin: '0 0 8px 0', fontSize: '13px', color: '#166534' }}>
                      📸 Current Image:
                    </p>
                    <img
                      src={currentImageUrl}
                      alt="Current"
                      style={{ maxWidth: '200px', maxHeight: '150px', objectFit: 'contain', borderRadius: '6px', border: '1px solid #d1d5db' }}
                    />
                    <p style={{ margin: '8px 0 0 0', fontSize: '12px', color: '#64748b' }}>
                      Upload a new file to replace this image
                    </p>
                  </div>
                )}
                {imageFile && imagePreview && (
                  <div style={{ marginTop: '10px' }}>
                    <p style={{ marginBottom: '8px', color: '#059669', fontSize: '13px' }}>
                      ✓ New image selected:
                    </p>
                    <img
                      src={imagePreview}
                      alt="Preview"
                      style={{ maxWidth: '200px', maxHeight: '150px', objectFit: 'contain', borderRadius: '6px', border: '2px solid #059669' }}
                    />
                  </div>
                )}type="file"
                  accept="image/*"
                  onChange={(e) => setImageFile(e.target.files[0])}
                />
              </div>

              <div className="form-actions">
                <button type="button" className="btn-secondary" onClick={handleCloseModal}>
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  {editingVocab ? "Update" : "Create"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default VocabularyManagement;
