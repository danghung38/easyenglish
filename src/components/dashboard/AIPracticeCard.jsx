import React from "react";
import { Card, Badge, Row, Col } from "react-bootstrap";
import { FiEdit, FiMic, FiArrowRight } from "react-icons/fi";
import { useNavigate } from "react-router-dom";

const AIPracticeCard = () => {
  const navigate = useNavigate();

  const practices = [
    {
      icon: <FiEdit size={24} />,
      title: "Writing Task 2",
      description: "Chẵm bài Essay tự động",
      subtitle: "Nếp bài luận của bạn và nhận nhận xét chi tiết về nội dung, từ vựng và ngữ pháp, từ vựng và tính nhất quán.",
      color: "#FFE5E5",
      iconColor: "#DC3545",
      tag: "Writing Task 2",
      action: "Bắt đầu",
      link: "/ieltstest/writing/"
    },
    {
      icon: <FiMic size={24} />,
      title: "Luyện nói với AI Tutor",
      description: "Speaking Part 2",
      subtitle: "Mô phỏng phỏng vấn thật, trả lời câu hỏi và nhận feedback về phát âm, từ vựng và độ trôi chảy.",
      color: "#E5F3FF",
      iconColor: "#0D6EFD",
      tag: "Speaking Part 2",
      action: "Bắt đầu",
      link: "/ieltstest/speaking/"
    }
  ];

  return (
    <Card className="shadow-sm h-100">
      <Card.Body className="d-flex flex-column" style={{ height: '100%' }}>
        <div className="d-flex justify-content-between align-items-start mb-3">
          <div>
            <h5 className="fw-bold mb-1">✨ Thực hành AI</h5>
            <small className="text-muted">Phần hỗi tức thi cho bài Nói và Viết của bạn</small>
          </div>
          <a href="#" className="text-primary text-decoration-none small">Xem lịch sử</a>
        </div>

        <Row className="g-2 flex-grow-1">
          {practices.map((practice, index) => (
            <Col md={6} key={index}>
              <Card 
                className="border-0 cursor-pointer h-100"
                style={{ 
                  backgroundColor: practice.color,
                  cursor: 'pointer',
                  transition: 'transform 0.2s'
                }}
                onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.01)'}
                onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
                onClick={() => navigate(practice.link)}
              >
                <Card.Body className="p-3 d-flex flex-column">
                  <div className="d-flex gap-2 mb-2">
                    <div 
                      className="d-flex align-items-center justify-content-center rounded"
                      style={{
                        width: '40px',
                        height: '40px',
                        backgroundColor: 'white',
                        color: practice.iconColor,
                        flexShrink: 0
                      }}
                    >
                      {practice.icon}
                    </div>
                    <div className="flex-grow-1">
                      <h6 className="fw-bold mb-1" style={{ fontSize: '0.95rem' }}>
                        {practice.title}
                      </h6>
                      <Badge 
                        bg="light" 
                        text="dark"
                        className="px-2 py-1"
                        style={{ fontSize: '0.65rem' }}
                      >
                        {practice.tag}
                      </Badge>
                    </div>
                  </div>
                  <p className="mb-2 small text-muted flex-grow-1" style={{ fontSize: '0.8rem', lineHeight: '1.4' }}>
                    {practice.subtitle}
                  </p>
                  <span 
                    className="text-decoration-none small fw-bold"
                    style={{ color: practice.iconColor, fontSize: '0.85rem' }}
                  >
                    {practice.action} <FiArrowRight size={14} />
                  </span>
                </Card.Body>
              </Card>
            </Col>
          ))}
        </Row>

        {/* Suggestion section */}
        <div 
          className="mt-3 p-2 rounded d-flex gap-2"
          style={{ 
            backgroundColor: '#FFF9E6',
            border: '1px solid #FFE5B4'
          }}
        >
          <span style={{ fontSize: '1.2rem' }}>💡</span>
          <div style={{ fontSize: '0.8rem' }}>
            <strong>Gợi ý cải thiện từ bài Speaking gần nhất:</strong>
            <p className="mb-0 text-muted mt-1">
              Bạn cần chú ý hơn về 'Intonation' (ngữ điệu) khi liệt kê các ý. Hãy thử bài tập nhấn trong âm câu.
            </p>
          </div>
        </div>
      </Card.Body>
    </Card>
  );
};

export default AIPracticeCard;
