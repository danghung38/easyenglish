import React from "react";
import { Card, Row, Col } from "react-bootstrap";
import {
  FiHeadphones,
  FiMic,
  FiBook,
  FiEdit3,
} from "react-icons/fi";
import { Link } from "react-router-dom";

const SkillModulesCard = () => {
  const modules = [
    {
      title: "Nghe (Listening)",
      subtitle: "Luyện tập & Đề thi",
      icon: FiHeadphones,
      color: "#0d6efd",
      bgColor: "#e7f1ff",
      link: "/ieltstest/listening",
    },
    {
      title: "Nói (Speaking)",
      subtitle: "Thực hành & Phản hồi AI",
      icon: FiMic,
      color: "#198754",
      bgColor: "#d1f2e6",
      link: "/ieltstest/speaking",
    },
    {
      title: "Đọc (Reading)",
      subtitle: "Bài tập & Tự vựng",
      icon: FiBook,
      color: "#ffc107",
      bgColor: "#fff3cd",
      link: "/ieltstest/reading",
    },
    {
      title: "Viết (Writing)",
      subtitle: "Sửa bài & Mẫu câu",
      icon: FiEdit3,
      color: "#dc3545",
      bgColor: "#f8d7da",
      link: "/ieltstest/writing",
    },
  ];

  return (
    <Row className="g-3">
      {modules.map((module, index) => (
        <Col key={index} md={6} lg={3}>
          <Link to={module.link} className="text-decoration-none">
            <Card
              className="h-100 border-0 shadow-sm hover-lift"
              style={{ cursor: "pointer", transition: "all 0.3s" }}
            >
              <Card.Body className="p-4">
                <div
                  className="d-flex align-items-center justify-content-center rounded-circle mb-3"
                  style={{
                    width: "60px",
                    height: "60px",
                    backgroundColor: module.bgColor,
                  }}
                >
                  <module.icon size={28} color={module.color} />
                </div>
                <h5 className="fw-bold mb-2" style={{ color: "#000" }}>
                  {module.title}
                </h5>
                <p className="text-muted mb-0 small">{module.subtitle}</p>
              </Card.Body>
            </Card>
          </Link>
        </Col>
      ))}
    </Row>
  );
};

export default SkillModulesCard;
