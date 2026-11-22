import React from "react";
import { Card, Col, Row, ProgressBar } from "react-bootstrap";

const OverallProgressCard = ({ overallScore = 6.5, skills, targetBand = 7.5 }) => {
  const defaultSkills = [
    { name: "Nghe (Listening)", score: 7.0, variant: "primary", icon: "🎧", color: "#3b82f6" },
    { name: "Nói (Speaking)", score: 6.0, variant: "success", icon: "🎤", color: "#22c55e" },
    { name: "Đọc (Reading)", score: 7.5, variant: "warning", icon: "📖", color: "#f97316" },
    { name: "Viết (Writing)", score: 5.5, variant: "danger", icon: "✍️", color: "#ef4444" },
  ];

  // Color mapping for skills
  const colorMap = {
    "Nghe (Listening)": "#3b82f6",
    "Listening": "#3b82f6",
    "Nói (Speaking)": "#22c55e",
    "Speaking": "#22c55e",
    "Đọc (Reading)": "#f97316",
    "Reading": "#f97316",
    "Viết (Writing)": "#ef4444",
    "Writing": "#ef4444",
  };

  // Merge skills data with colors
  const skillsData = skills 
    ? skills.map(skill => ({
        ...skill,
        color: skill.color || colorMap[skill.name] || "#6c757d"
      }))
    : defaultSkills;
  
  // Round scores to nearest 0.5
  const roundedOverallScore = Math.round(overallScore * 2) / 2;
  const roundedSkills = skillsData.map(skill => ({
    ...skill,
    score: Math.round(skill.score * 2) / 2
  }));
  
  // Calculate percentage for circular progress based on target band
  const progressPercentage = targetBand > 0 ? (roundedOverallScore / targetBand) * 100 : 0;

  return (
    <Card className="mb-4 shadow-sm h-100">
      <Card.Body className="py-4">
        <div className="d-flex align-items-start mb-3">
          <h5 className="mb-0 fw-bold">Tổng quan tiến độ</h5>
          
        </div>
        <p className="text-muted mb-4" style={{ fontSize: "0.9rem" }}>
          Bạn đã hoàn thành {progressPercentage.toFixed(0)}% lộ trình mục tiêu {targetBand}
        </p>

        <Row className="align-items-center">
          {/* Tổng quan - Circular Progress */}
          <Col md={4} className="text-center mb-4 mb-md-0">
            <div className="position-relative d-inline-block">
              <svg width="240" height="240">
                <circle
                  cx="120"
                  cy="120"
                  r="95"
                  fill="none"
                  stroke="#e9ecef"
                  strokeWidth="18"
                />
                <circle
                  cx="120"
                  cy="120"
                  r="95"
                  fill="none"
                  stroke="#0d6efd"
                  strokeWidth="18"
                  strokeDasharray={`${(progressPercentage / 100) * 597} 597`}
                  strokeLinecap="round"
                  transform="rotate(-90 120 120)"
                />
              </svg>
              <div
                className="position-absolute top-50 start-50 translate-middle text-center"
              >
                <h1 className="mb-0 fw-bold" style={{ fontSize: "3.5rem" }}>
                  {roundedOverallScore}
                </h1>
                <small className="text-muted fw-semibold">Band hiện tại</small>
              </div>
            </div>
          </Col>

          {/* Skills Progress Bars - Vertical Layout */}
          <Col md={8}>
            <div className="d-flex flex-column gap-3">
              {roundedSkills.map((skill, index) => (
                <div key={index}>
                  <div className="d-flex align-items-center mb-2">
                    <span className="me-2" style={{ fontSize: "1.1rem" }}>
                      {skill.icon}
                    </span>
                    <span className="fw-semibold flex-grow-1">{skill.name}</span>
                    <span className="fw-bold">{skill.score}</span>
                  </div>
                  <ProgressBar
                    now={(skill.score / 9.0) * 100}
                    style={{ 
                      height: "10px", 
                      borderRadius: "5px",
                      backgroundColor: '#e9ecef'
                    }}
                  >
                    <ProgressBar
                      now={(skill.score / 9.0) * 100}
                      style={{ backgroundColor: skill.color }}
                    />
                  </ProgressBar>
                </div>
              ))}
            </div>
          </Col>
        </Row>
      </Card.Body>
    </Card>
  );
};

export default OverallProgressCard;
