import React, { useState, useEffect } from "react";
import { Card, Badge, Spinner, Modal, Button, Table } from "react-bootstrap";
import axios from "axios";
import { DJANGO_BASE_URL } from "../../utils/config";

const LeaderboardCard = () => {
  const [leaderboard, setLeaderboard] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showFullLeaderboard, setShowFullLeaderboard] = useState(false);
  const [fullLeaderboard, setFullLeaderboard] = useState([]);
  const [loadingFull, setLoadingFull] = useState(false);

  useEffect(() => {
    fetchLeaderboard();
  }, []);

  const getAuthHeaders = () => {
    const authTokensStr = localStorage.getItem('authTokens');
    if (!authTokensStr) return {};
    
    try {
      const authTokens = JSON.parse(authTokensStr);
      const token = authTokens.token;
      return token ? { Authorization: `Bearer ${token}` } : {};
    } catch (e) {
      console.error('Error parsing auth tokens:', e);
      return {};
    }
  };

  const fetchLeaderboard = async () => {
    try {
      setLoading(true);
      const response = await axios.get(
        `${DJANGO_BASE_URL}/userexams/leaderboard`,
        {
          params: { limit: 5 },
          headers: getAuthHeaders()
        }
      );

      console.log('Leaderboard response:', response.data);

      if (response.data.code === 200) {
        const data = response.data.result || [];
        setLeaderboard(data);
      }
      setLoading(false);
    } catch (error) {
      console.error("Error fetching leaderboard:", error);
      setLoading(false);
    }
  };

  const fetchFullLeaderboard = async () => {
    try {
      setLoadingFull(true);
      const response = await axios.get(
        `${DJANGO_BASE_URL}/userexams/leaderboard`,
        {
          params: { limit: 10000 }, // Lấy tất cả người dùng
          headers: getAuthHeaders()
        }
      );

      console.log('Full leaderboard response:', response.data);

      if (response.data.code === 200) {
        const data = response.data.result || [];
        // Chỉ hiển thị người dùng đã làm ít nhất 1 bài thi
        const filteredData = data.filter(user => user.totalTests > 0);
        setFullLeaderboard(filteredData);
      }
      setLoadingFull(false);
    } catch (error) {
      console.error("Error fetching full leaderboard:", error);
      setLoadingFull(false);
    }
  };

  const handleShowFullLeaderboard = () => {
    setShowFullLeaderboard(true);
    fetchFullLeaderboard();
  };

  const handleCloseFullLeaderboard = () => {
    setShowFullLeaderboard(false);
  };

  const getRankColor = (rank) => {
    if (rank === 1) return "#FFD700";
    if (rank === 2) return "#C0C0C0";
    if (rank === 3) return "#CD7F32";
    return "#6c757d";
  };

  const getScoreBadgeVariant = (score) => {
    if (score >= 8.0) return "success";
    if (score >= 7.0) return "warning";
    if (score >= 6.0) return "info";
    return "secondary";
  };

  if (loading) {
    return (
      <Card className="shadow-sm h-100">
        <Card.Body>
          <h5 className="fw-bold mb-3">Bảng xếp hạng</h5>
          <div className="text-center py-5">
            <Spinner animation="border" size="sm" />
            <p className="mt-2 text-muted small">Đang tải...</p>
          </div>
        </Card.Body>
      </Card>
    );
  }

  if (leaderboard.length === 0) {
    return (
      <Card className="shadow-sm h-100">
        <Card.Body>
          <h5 className="fw-bold mb-3">Bảng xếp hạng</h5>
          <div className="text-center py-5">
            <p className="text-muted">Chưa có dữ liệu xếp hạng</p>
          </div>
        </Card.Body>
      </Card>
    );
  }

  return (
    <Card className="shadow-sm h-100">
      <Card.Body>
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h5 className="fw-bold mb-0">Bảng xếp hạng</h5>
        </div>

        <div className="d-flex flex-column gap-2">
          {leaderboard.map((user, index) => (
              <div
                key={index}
                className={`d-flex align-items-center gap-3 p-2 rounded ${
                  user.currentUser ? 'bg-light' : ''
                }`}
              >
                <div 
                  className="d-flex align-items-center justify-content-center fw-bold"
                  style={{
                    width: '32px',
                    height: '32px',
                    borderRadius: '50%',
                    backgroundColor: user.rank <= 3 ? getRankColor(user.rank) : '#f8f9fa',
                    color: user.rank <= 3 ? 'white' : '#6c757d',
                    fontSize: '0.9rem'
                  }}
                >
                  {user.rank === 1 && '👑'}
                  {user.rank === 2 && '🥈'}
                  {user.rank === 3 && '🥉'}
                  {user.rank > 3 && user.rank}
                </div>

                <div className="flex-grow-1">
                  <div className="d-flex align-items-center gap-2">
                    <span className="fw-semibold">{user.fullName || user.userName}</span>
                    {user.currentUser && (
                      <Badge bg="primary" className="px-2 py-1" style={{ fontSize: '0.6rem' }}>
                        BẠN
                      </Badge>
                    )}
                  </div>
                  {user.totalTests > 0 && (
                    <small className="text-muted">{user.totalTests} bài thi</small>
                  )}
                </div>

                <Badge 
                  bg={getScoreBadgeVariant(user.score)}
                  className="px-3 py-2"
                  style={{ fontSize: '0.85rem', fontWeight: '600' }}
                >
                  {user.score?.toFixed(1) || '0.0'}
                </Badge>
              </div>
            ))}
        </div>

        <div className="text-center mt-3">
          <small 
            className="text-primary" 
            style={{ cursor: 'pointer', textDecoration: 'underline' }}
            onClick={handleShowFullLeaderboard}
          >
            <i className="fe fe-arrow-right me-1"></i>
            Xem toàn bộ bảng xếp hạng
          </small>
        </div>
      </Card.Body>

      {/* Full Leaderboard Modal */}
      <Modal 
        show={showFullLeaderboard} 
        onHide={handleCloseFullLeaderboard}
        size="lg"
        centered
      >
        <Modal.Header closeButton className="border-0 pb-2">
          <Modal.Title className="w-100">
            <div className="text-center w-100">
              <h4 className="fw-bold mb-1">
                <i className="fe fe-award me-2 text-warning"></i>
                Bảng Xếp Hạng Toàn Bộ
              </h4>
              <small className="text-muted">
                Top {fullLeaderboard.length} người dùng xuất sắc nhất
              </small>
            </div>
          </Modal.Title>
        </Modal.Header>
        
        <Modal.Body className="px-0 py-0" style={{ maxHeight: '600px', overflowY: 'auto' }}>
          {loadingFull ? (
            <div className="text-center py-5">
              <Spinner animation="border" role="status" variant="primary">
                <span className="visually-hidden">Loading...</span>
              </Spinner>
              <p className="mt-3 fw-bold" style={{ color: '#495057' }}>Đang tải bảng xếp hạng...</p>
            </div>
          ) : fullLeaderboard.length === 0 ? (
            <div className="text-center py-5">
              <p className="text-muted">Chưa có dữ liệu xếp hạng</p>
            </div>
          ) : (
            <Table hover responsive className="mb-0">
              <thead style={{ backgroundColor: '#f8f9fa', position: 'sticky', top: 0, zIndex: 1 }}>
                <tr>
                  <th className="text-center py-3 fw-bold" style={{ width: '80px' }}>Hạng</th>
                  <th className="py-3 fw-bold">Tên người dùng</th>
                  <th className="text-center py-3 fw-bold" style={{ width: '120px' }}>Số bài thi</th>
                  <th className="text-center py-3 fw-bold" style={{ width: '120px' }}>Điểm TB</th>
                </tr>
              </thead>
              <tbody>
                {fullLeaderboard.map((user, index) => (
                  <tr 
                    key={index}
                    className={user.currentUser ? 'table-primary' : ''}
                    style={{ 
                      backgroundColor: user.currentUser ? '#cfe2ff' : 'white',
                      fontWeight: user.currentUser ? '600' : 'normal'
                    }}
                  >
                    <td className="text-center py-3">
                      <div 
                        className="d-inline-flex align-items-center justify-content-center fw-bold"
                        style={{
                          width: '40px',
                          height: '40px',
                          borderRadius: '50%',
                          backgroundColor: user.rank <= 3 ? getRankColor(user.rank) : '#f8f9fa',
                          color: user.rank <= 3 ? 'white' : '#6c757d',
                          fontSize: '1rem',
                          border: user.rank <= 3 ? '3px solid white' : '2px solid #dee2e6',
                          boxShadow: user.rank <= 3 ? '0 2px 8px rgba(0,0,0,0.15)' : 'none'
                        }}
                      >
                        {user.rank === 1 && '👑'}
                        {user.rank === 2 && '🥈'}
                        {user.rank === 3 && '🥉'}
                        {user.rank > 3 && user.rank}
                      </div>
                    </td>
                    <td className="py-3">
                      <div className="d-flex align-items-center gap-2">
                        <span className={user.currentUser ? 'fw-bold' : ''}>
                          {user.fullName || user.userName}
                        </span>
                        {user.currentUser && (
                          <Badge bg="primary" className="px-2 py-1" style={{ fontSize: '0.7rem' }}>
                            BẠN
                          </Badge>
                        )}
                      </div>
                    </td>
                    <td className="text-center py-3">
                      <Badge bg="info" className="px-3 py-2">
                        {user.totalTests || 0} bài
                      </Badge>
                    </td>
                    <td className="text-center py-3">
                      <Badge 
                        bg={getScoreBadgeVariant(user.score)}
                        className="px-3 py-2"
                        style={{ fontSize: '1rem', fontWeight: '700' }}
                      >
                        {user.score?.toFixed(1) || '0.0'}
                      </Badge>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          )}
        </Modal.Body>
        
        <Modal.Footer className="border-top-3 bg-light py-3">
          <Button 
            variant="outline-dark" 
            onClick={handleCloseFullLeaderboard}
            className="px-4 py-2 fw-bold"
            style={{ borderRadius: '8px', borderWidth: '2px' }}
          >
            <i className="fe fe-x me-2"></i>
            Đóng
          </Button>
        </Modal.Footer>
      </Modal>
    </Card>
  );
};

export default LeaderboardCard;
