import React, { useState, useEffect } from "react";
import { getAllUserExams, getUserExamById, searchUserExams } from "../../services/adminUserExamService";
import { getAllExams } from "../../services/adminExamService";
import { getUserById, getAllUsers } from "../../services/adminUserService";

const UserExamsManagement = () => {
  const [userExams, setUserExams] = useState([]);
  const [exams, setExams] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [selectedExamDetail, setSelectedExamDetail] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(20);
  const [searching, setSearching] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      
      // Fetch user exams, exam list, and users in parallel
      const [userExamsResponse, examsResponse, usersResponse] = await Promise.all([
        getAllUserExams(),
        getAllExams(1, 100, "id"),
        getAllUsers(1, 1000, "id")
      ]);
      
      if (userExamsResponse.code === 200) {
        setUserExams(userExamsResponse.result || []);
      }
      
      if (examsResponse.code === 200 && examsResponse.result) {
        setExams(examsResponse.result.items || []);
      }
      
      if (usersResponse.code === 200 && usersResponse.result) {
        setUsers(usersResponse.result.items || []);
      }
      
      setError(null);
    } catch (err) {
      setError("Failed to fetch data: " + err.message);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    try {
      setSearching(true);
      setError(null);
      
      // Fetch exams and users if not already loaded
      const promises = [searchUserExams(searchQuery)];
      
      if (exams.length === 0) {
        promises.push(getAllExams(1, 100, "id"));
      }
      
      if (users.length === 0) {
        promises.push(getAllUsers(1, 1000, "id"));
      }
      
      const results = await Promise.all(promises);
      const userExamsResponse = results[0];
      
      if (userExamsResponse.code === 200) {
        setUserExams(userExamsResponse.result || []);
      }
      
      // Update exams and users if they were fetched
      if (results.length > 1 && exams.length === 0) {
        const examsResponse = results[1];
        if (examsResponse.code === 200 && examsResponse.result) {
          setExams(examsResponse.result.items || []);
        }
      }
      
      if (results.length > 2 && users.length === 0) {
        const usersResponse = results[2];
        if (usersResponse.code === 200 && usersResponse.result) {
          setUsers(usersResponse.result.items || []);
        }
      }
      
      setCurrentPage(1); // Reset to first page on search
    } catch (err) {
      setError("Failed to search: " + err.message);
      console.error(err);
    } finally {
      setSearching(false);
    }
  };

  const getExamTitle = (examId) => {
    const exam = exams.find(e => e.id === examId);
    return exam ? exam.title : `Exam #${examId}`;
  };

  const getUserName = (userId) => {
    const user = users.find(u => u.id === userId);
    return user ? user.username : `User #${userId}`;
  };

  const getUserEmail = (userId) => {
    const user = users.find(u => u.id === userId);
    return user ? user.email : "N/A";
  };

  const handleViewDetails = async (userExamId) => {
    try {
      setDetailLoading(true);
      const response = await getUserExamById(userExamId);
      
      if (response.code === 200) {
        setSelectedExamDetail(response.result);
        setShowDetailModal(true);
      }
    } catch (err) {
      setError("Failed to fetch exam details: " + err.message);
      console.error(err);
    } finally {
      setDetailLoading(false);
    }
  };

  const handleCloseDetailModal = () => {
    setShowDetailModal(false);
    setSelectedExamDetail(null);
  };

  const formatDateTime = (dateTimeString) => {
    if (!dateTimeString) return "N/A";
    const date = new Date(dateTimeString);
    return date.toLocaleString();
  };

  const getSkillTypeBadgeClass = (skillType) => {
    const badgeClasses = {
      READING: "badge-reading",
      LISTENING: "badge-listening",
      WRITING: "badge-writing",
      SPEAKING: "badge-speaking"
    };
    return badgeClasses[skillType] || "badge-default";
  };

  const calculateOverallScore = (parts) => {
    if (!parts || parts.length === 0) return "N/A";
    
    const submittedParts = parts.filter(p => p.submitted && p.score !== null);
    if (submittedParts.length === 0) return "Not submitted";
    
    const totalScore = submittedParts.reduce((sum, part) => sum + (part.score || 0), 0);
    const avgScore = totalScore / submittedParts.length;
    return avgScore.toFixed(1);
  };

  const getSubmissionStatus = (parts) => {
    if (!parts || parts.length === 0) return "No parts";
    
    const submittedCount = parts.filter(p => p.submitted).length;
    const totalCount = parts.length;
    
    if (submittedCount === 0) return "Not started";
    if (submittedCount === totalCount) return "Completed";
    return `In progress (${submittedCount}/${totalCount})`;
  };

  // Filter user exams based on search query
  const filteredUserExams = userExams; // No client-side filtering, search is done via API

  // Reset to page 1 when search query changes - removed since we handle it in handleSearch
  // useEffect(() => {
  //   setCurrentPage(1);
  // }, [searchQuery]);

  // Calculate pagination
  const totalItems = filteredUserExams.length;
  const totalPages = Math.ceil(totalItems / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const paginatedUserExams = filteredUserExams.slice(startIndex, endIndex);

  // Pagination handlers
  const handlePageChange = (pageNumber) => {
    setCurrentPage(pageNumber);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleItemsPerPageChange = (e) => {
    setItemsPerPage(Number(e.target.value));
    setCurrentPage(1);
  };

  const handlePreviousPage = () => {
    if (currentPage > 1) {
      handlePageChange(currentPage - 1);
    }
  };

  const handleNextPage = () => {
    if (currentPage < totalPages) {
      handlePageChange(currentPage + 1);
    }
  };

  // Generate page numbers to display
  const getPageNumbers = () => {
    const pages = [];
    const maxPagesToShow = 5;
    
    if (totalPages <= maxPagesToShow) {
      for (let i = 1; i <= totalPages; i++) {
        pages.push(i);
      }
    } else {
      if (currentPage <= 3) {
        for (let i = 1; i <= 4; i++) {
          pages.push(i);
        }
        pages.push('...');
        pages.push(totalPages);
      } else if (currentPage >= totalPages - 2) {
        pages.push(1);
        pages.push('...');
        for (let i = totalPages - 3; i <= totalPages; i++) {
          pages.push(i);
        }
      } else {
        pages.push(1);
        pages.push('...');
        pages.push(currentPage - 1);
        pages.push(currentPage);
        pages.push(currentPage + 1);
        pages.push('...');
        pages.push(totalPages);
      }
    }
    
    return pages;
  };

  if (loading || searching) {
    return <div className="loading">{searching ? "Searching..." : "Loading user exams..."}</div>;
  }

  return (
    <div className="admin-page">
      <div className="admin-page-header">
        <h1>User Exams Management</h1>
        <div className="header-actions">
          <div className="search-box">
            <input
              type="text"
              placeholder="Search by User ID, Name, Email, or Exam Title..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyPress={(e) => {
                if (e.key === 'Enter') {
                  if (searchQuery.trim()) {
                    handleSearch();
                  } else {
                    fetchData();
                  }
                }
              }}
              className="search-input"
            />
            {searchQuery && (
              <button 
                className="clear-search" 
                onClick={() => {
                  setSearchQuery("");
                  fetchData();
                }}
                title="Clear search"
              >
                ✕
              </button>
            )}
          </div>
          <button 
            className="btn-search" 
            onClick={() => {
              if (searchQuery.trim()) {
                handleSearch();
              } else {
                fetchData();
              }
            }}
            disabled={searching}
          >
            🔍 {searching ? 'Searching...' : 'Search'}
          </button>
          <button className="btn-primary" onClick={fetchData}>
            🔄 Refresh
          </button>
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}

      {/* Pagination Info and Controls - Top */}
      {filteredUserExams.length > 0 && (
        <div className="pagination-info">
          <div className="items-per-page">
            <label>Show:</label>
            <select value={itemsPerPage} onChange={handleItemsPerPageChange}>
              <option value={10}>10</option>
              <option value={20}>20</option>
              <option value={50}>50</option>
              <option value={100}>100</option>
            </select>
            <span>items per page</span>
          </div>
          <div className="showing-info">
            Showing {startIndex + 1}-{Math.min(endIndex, totalItems)} of {totalItems} results
          </div>
        </div>
      )}

      {filteredUserExams.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">📝</div>
          <h3>{searchQuery ? "No matching user exams found" : "No user exams found"}</h3>
          <p>{searchQuery ? "Try a different search term" : "User exam submissions will appear here"}</p>
        </div>
      ) : (
        <table className="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>User</th>
              <th>Exam</th>
              <th>Started At</th>
              <th>Status</th>
              <th>Overall Score</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {paginatedUserExams.map((userExam) => (
              <tr key={userExam.id}>
                <td>{userExam.id}</td>
                <td>
                  <div className="user-info">
                    <div className="user-id">#{userExam.userId}</div>
                    <div className="username">{getUserName(userExam.userId)}</div>
                  </div>
                </td>
                <td>{getExamTitle(userExam.examId)}</td>
                <td>{formatDateTime(userExam.startedAt)}</td>
                <td>
                  <span className={`status-badge ${
                    getSubmissionStatus(userExam.parts) === 'Completed' ? 'status-completed' :
                    getSubmissionStatus(userExam.parts) === 'Not started' ? 'status-pending' :
                    'status-in-progress'
                  }`}>
                    {getSubmissionStatus(userExam.parts)}
                  </span>
                </td>
                <td>
                  <strong>{calculateOverallScore(userExam.parts)}</strong>
                </td>
                <td>
                  <div className="action-buttons">
                    <button
                      className="btn-view"
                      onClick={() => handleViewDetails(userExam.id)}
                      disabled={detailLoading}
                    >
                      View Details
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {/* Pagination Controls - Bottom */}
      {filteredUserExams.length > 0 && totalPages > 1 && (
        <div className="pagination-container">
          <button 
            className="btn-pagination" 
            onClick={handlePreviousPage}
            disabled={currentPage === 1}
          >
            ← Previous
          </button>
          
          <div className="page-numbers">
            {getPageNumbers().map((page, index) => (
              page === '...' ? (
                <span key={`ellipsis-${index}`} className="pagination-ellipsis">...</span>
              ) : (
                <button
                  key={page}
                  className={`btn-page ${currentPage === page ? 'active' : ''}`}
                  onClick={() => handlePageChange(page)}
                >
                  {page}
                </button>
              )
            ))}
          </div>
          
          <button 
            className="btn-pagination" 
            onClick={handleNextPage}
            disabled={currentPage === totalPages}
          >
            Next →
          </button>
        </div>
      )}

      {showDetailModal && selectedExamDetail && (
        <div className="modal-overlay" onClick={handleCloseDetailModal}>
          <div className="modal-content modal-large" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>User Exam Details</h2>
              <button className="btn-close" onClick={handleCloseDetailModal}>×</button>
            </div>
            
            <div className="exam-detail-content">
              <div className="detail-section">
                <h3>Exam Information</h3>
                <div className="detail-grid">
                  <div className="detail-item">
                    <label>User Exam ID:</label>
                    <span>{selectedExamDetail.id}</span>
                  </div>
                  <div className="detail-item">
                    <label>User ID:</label>
                    <span>{selectedExamDetail.userId}</span>
                  </div>
                  <div className="detail-item">
                    <label>Exam:</label>
                    <span>{getExamTitle(selectedExamDetail.examId)}</span>
                  </div>
                  <div className="detail-item">
                    <label>Started At:</label>
                    <span>{formatDateTime(selectedExamDetail.startedAt)}</span>
                  </div>
                  <div className="detail-item">
                    <label>Overall Score:</label>
                    <span className="score-large">{calculateOverallScore(selectedExamDetail.parts)}</span>
                  </div>
                </div>
              </div>

              <div className="detail-section">
                <h3>Exam Parts</h3>
                {selectedExamDetail.parts && selectedExamDetail.parts.length > 0 ? (
                  <table className="parts-table">
                    <thead>
                      <tr>
                        <th>Part ID</th>
                        <th>Skill Type</th>
                        <th>Status</th>
                        <th>Score</th>
                      </tr>
                    </thead>
                    <tbody>
                      {selectedExamDetail.parts.map((part) => (
                        <tr key={part.id}>
                          <td>{part.id}</td>
                          <td>
                            <span className={`badge ${getSkillTypeBadgeClass(part.skillType)}`}>
                              {part.skillType}
                            </span>
                          </td>
                          <td>
                            <span className={`status-badge ${part.submitted ? 'status-completed' : 'status-pending'}`}>
                              {part.submitted ? 'Submitted' : 'Not Submitted'}
                            </span>
                          </td>
                          <td>
                            <strong>{part.score !== null ? part.score.toFixed(1) : 'N/A'}</strong>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                ) : (
                  <p>No parts found for this exam.</p>
                )}
              </div>
            </div>

            <div className="form-actions">
              <button className="btn-secondary" onClick={handleCloseDetailModal}>
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      <style jsx>{`
        .admin-page-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 20px;
        }

        .header-actions {
          display: flex;
          gap: 15px;
          align-items: center;
        }

        .search-box {
          position: relative;
          display: flex;
          align-items: center;
        }

        .search-input {
          padding: 10px 40px 10px 15px;
          border: 1px solid #d1d5db;
          border-radius: 8px;
          font-size: 14px;
          width: 350px;
          transition: all 0.2s;
        }

        .search-input:focus {
          outline: none;
          border-color: #3b82f6;
          box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }

        .clear-search {
          position: absolute;
          right: 10px;
          background: none;
          border: none;
          color: #6b7280;
          cursor: pointer;
          font-size: 18px;
          padding: 5px;
          display: flex;
          align-items: center;
          justify-content: center;
          width: 24px;
          height: 24px;
          border-radius: 50%;
          transition: all 0.2s;
        }

        .clear-search:hover {
          background-color: #f3f4f6;
          color: #374151;
        }

        .user-info {
          display: flex;
          flex-direction: column;
          gap: 4px;
        }

        .user-id {
          color: #6b7280;
          font-weight: 600;
          font-size: 13px;
        }

        .username {
          font-weight: 500;
          color: #374151;
          font-size: 14px;
        }

        .modal-large {
          max-width: 900px;
          width: 90%;
        }

        .modal-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 20px;
          padding-bottom: 15px;
          border-bottom: 1px solid #e0e0e0;
        }

        .btn-close {
          background: none;
          border: none;
          font-size: 28px;
          cursor: pointer;
          color: #666;
          padding: 0;
          width: 30px;
          height: 30px;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .btn-close:hover {
          color: #333;
        }

        .exam-detail-content {
          max-height: 70vh;
          overflow-y: auto;
        }

        .detail-section {
          margin-bottom: 30px;
        }

        .detail-section h3 {
          margin-bottom: 15px;
          color: #333;
          font-size: 18px;
          font-weight: 600;
        }

        .detail-grid {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
          gap: 15px;
          background: #f8f9fa;
          padding: 20px;
          border-radius: 8px;
        }

        .detail-item {
          display: flex;
          flex-direction: column;
        }

        .detail-item label {
          font-size: 12px;
          color: #666;
          font-weight: 600;
          margin-bottom: 5px;
          text-transform: uppercase;
        }

        .detail-item span {
          font-size: 14px;
          color: #333;
        }

        .score-large {
          font-size: 24px !important;
          font-weight: bold !important;
          color: #059669 !important;
        }

        .parts-table {
          width: 100%;
          border-collapse: collapse;
          margin-top: 10px;
        }

        .parts-table th,
        .parts-table td {
          padding: 12px;
          text-align: left;
          border-bottom: 1px solid #e0e0e0;
        }

        .parts-table th {
          background-color: #f8f9fa;
          font-weight: 600;
          color: #333;
        }

        .parts-table tbody tr:hover {
          background-color: #f8f9fa;
        }

        .badge {
          display: inline-block;
          padding: 4px 12px;
          border-radius: 12px;
          font-size: 12px;
          font-weight: 600;
          text-transform: uppercase;
        }

        .badge-reading {
          background-color: #dbeafe;
          color: #1e40af;
        }

        .badge-listening {
          background-color: #fce7f3;
          color: #9f1239;
        }

        .badge-writing {
          background-color: #fef3c7;
          color: #92400e;
        }

        .badge-speaking {
          background-color: #dcfce7;
          color: #166534;
        }

        .badge-default {
          background-color: #e5e7eb;
          color: #374151;
        }

        .status-badge {
          display: inline-block;
          padding: 4px 12px;
          border-radius: 12px;
          font-size: 12px;
          font-weight: 600;
        }

        .status-completed {
          background-color: #dcfce7;
          color: #166534;
        }

        .status-pending {
          background-color: #fef3c7;
          color: #92400e;
        }

        .status-in-progress {
          background-color: #dbeafe;
          color: #1e40af;
        }

        .btn-view {
          background-color: #3b82f6;
          color: white;
          border: none;
          padding: 8px 16px;
          border-radius: 6px;
          cursor: pointer;
          font-size: 14px;
          font-weight: 500;
        }

        .btn-view:hover:not(:disabled) {
          background-color: #2563eb;
        }

        .btn-view:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        .btn-search {
          background-color: #10b981;
          color: white;
          border: none;
          padding: 10px 20px;
          border-radius: 8px;
          cursor: pointer;
          font-size: 14px;
          font-weight: 600;
          transition: all 0.2s;
          display: flex;
          align-items: center;
          gap: 5px;
        }

        .btn-search:hover:not(:disabled) {
          background-color: #059669;
        }

        .btn-search:disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }

        /* Pagination Styles */
        .pagination-info {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 15px 20px;
          background-color: #f8f9fa;
          border-radius: 8px;
          margin-bottom: 15px;
          border: 1px solid #e5e7eb;
        }

        .items-per-page {
          display: flex;
          align-items: center;
          gap: 10px;
        }

        .items-per-page label {
          font-weight: 600;
          color: #374151;
          font-size: 14px;
        }

        .items-per-page select {
          padding: 6px 12px;
          border: 1px solid #d1d5db;
          border-radius: 6px;
          background-color: white;
          font-size: 14px;
          cursor: pointer;
          color: #374151;
        }

        .items-per-page select:focus {
          outline: none;
          border-color: #3b82f6;
          box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }

        .items-per-page span {
          color: #6b7280;
          font-size: 14px;
        }

        .showing-info {
          font-size: 14px;
          color: #6b7280;
          font-weight: 500;
        }

        .pagination-container {
          display: flex;
          justify-content: center;
          align-items: center;
          gap: 10px;
          padding: 20px;
          margin-top: 20px;
          background-color: #f8f9fa;
          border-radius: 8px;
          border: 1px solid #e5e7eb;
        }

        .btn-pagination {
          padding: 8px 16px;
          border: 1px solid #d1d5db;
          background-color: white;
          color: #374151;
          border-radius: 6px;
          cursor: pointer;
          font-size: 14px;
          font-weight: 500;
          transition: all 0.2s;
        }

        .btn-pagination:hover:not(:disabled) {
          background-color: #3b82f6;
          color: white;
          border-color: #3b82f6;
        }

        .btn-pagination:disabled {
          opacity: 0.5;
          cursor: not-allowed;
          background-color: #f3f4f6;
          color: #9ca3af;
        }

        .page-numbers {
          display: flex;
          gap: 5px;
          align-items: center;
        }

        .btn-page {
          min-width: 36px;
          height: 36px;
          padding: 0 10px;
          border: 1px solid #d1d5db;
          background-color: white;
          color: #374151;
          border-radius: 6px;
          cursor: pointer;
          font-size: 14px;
          font-weight: 500;
          transition: all 0.2s;
        }

        .btn-page:hover {
          background-color: #f3f4f6;
          border-color: #9ca3af;
        }

        .btn-page.active {
          background-color: #3b82f6;
          color: white;
          border-color: #3b82f6;
          font-weight: 600;
        }

        .pagination-ellipsis {
          padding: 0 8px;
          color: #6b7280;
          font-weight: 500;
        }
      `}</style>
    </div>
  );
};

export default UserExamsManagement;
