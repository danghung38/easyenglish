import React, { useState, useEffect } from "react";
import { Card, Button, Badge, Spinner, Alert } from "react-bootstrap";
import { Link } from "react-router-dom";
import { FiClock, FiCheckCircle } from "react-icons/fi";
import { getRecentTests } from "../../services/examService";

const RecentTestsCard = ({ tests }) => {
  const [recentTests, setRecentTests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    // If tests prop is provided, use it; otherwise fetch from API
    if (tests && tests.length > 0) {
      setRecentTests(tests);
      setLoading(false);
    } else {
      fetchRecentTests();
    }
  }, [tests]);

  const fetchRecentTests = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getRecentTests();
      setRecentTests(data);
    } catch (err) {
      console.error("Error fetching recent tests:", err);
      setError("Không thể tải lịch sử làm bài");
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Card className="shadow-sm h-100">
        <Card.Body className="d-flex justify-content-center align-items-center" style={{ minHeight: '300px' }}>
          <Spinner animation="border" variant="primary" />
        </Card.Body>
      </Card>
    );
  }

  if (error) {
    return (
      <Card className="shadow-sm h-100">
        <Card.Body>
          <Alert variant="danger">{error}</Alert>
        </Card.Body>
      </Card>
    );
  }

  if (recentTests.length === 0) {
    return (
      <Card className="shadow-sm h-100">
        <Card.Body>
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h5 className="fw-bold mb-0">📝 Lịch sử làm bài</h5>
          </div>
          <div className="text-center py-5">
            <p className="text-muted">Bạn chưa có lịch sử làm bài nào</p>
            <Link to="/ieltstest" className="btn btn-primary btn-sm">
              Bắt đầu làm bài
            </Link>
          </div>
        </Card.Body>
      </Card>
    );
  }

  return (
    <Card className="shadow-sm h-100">
      <Card.Body>
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h5 className="fw-bold mb-0">📝 Lịch sử làm bài</h5>
          <Link to="/history" className="text-primary text-decoration-none small">
            Xem tất cả
          </Link>
        </div>
        
        <div className="d-flex flex-column gap-3">
          {recentTests.map((test, index) => (
            <div
              key={test.id || index}
              className="d-flex justify-content-between align-items-center p-3 border rounded hover-shadow"
              style={{ cursor: 'pointer', transition: 'all 0.2s' }}
            >
              <div className="flex-grow-1">
                <div className="d-flex align-items-center gap-2 mb-1">
                  <Badge 
                    bg={test.variant || "primary"} 
                    className="text-uppercase" 
                    style={{ fontSize: '0.7rem', fontWeight: 'bold' }}
                  >
                    {test.isFullTest ? '🎯 FULL TEST' : test.skill}
                  </Badge>
                  <p className="mb-0 fw-semibold">{test.title}</p>
                </div>
                <div className="d-flex align-items-center gap-3">
                  <small className="text-muted d-flex align-items-center gap-1">
                    <FiClock size={14} />
                    {test.date}
                  </small>
                  {test.status === "completed" && (
                    <small className="text-success d-flex align-items-center gap-1">
                      <FiCheckCircle size={14} />
                      Đã hoàn thành
                    </small>
                  )}
                  {test.isFullTest && (
                    <small className="text-info">
                      (4 kỹ năng)
                    </small>
                  )}
                </div>
              </div>
              <div className="d-flex align-items-center gap-3">
                <div className="text-end">
                  <div className="fw-bold text-primary" style={{ fontSize: '1.2rem' }}>
                    {test.score}
                  </div>
                  <small className="text-muted">
                    {test.isFullTest ? 'Trung bình' : 'Band'}
                  </small>
                </div>
                {test.isFullTest ? (
                  <Link 
                    to={`/ieltstest/fulltest/result/${test.userExamId}`}
                    className="btn btn-sm btn-outline-primary"
                  >
                    Xem chi tiết
                  </Link>
                ) : (
                  <Link 
                    to={`/ieltstest/${test.skill.toLowerCase()}/result/${test.userExamPartId}`}
                    className="btn btn-sm btn-outline-primary"
                  >
                    Xem chi tiết
                  </Link>
                )}
              </div>
            </div>
          ))}
        </div>
      </Card.Body>
    </Card>
  );
};

export default RecentTestsCard;
