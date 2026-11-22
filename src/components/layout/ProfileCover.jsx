// import node module libraries
import { Row, Col, Card, Tab, Nav } from "react-bootstrap";
import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import { useAuth } from "../../context/AuthContext";
import { FiBell } from "react-icons/fi";

const ProfileCover = ({ page = "dashboard" }) => {
  const { user } = useAuth();

  if (!user) {
    return null;
  }

  // Don't show the header for account page
  if (page === "account") {
    return null;
  }

  return (
    <Row className="align-items-center mb-4">
      <Col xl={12} lg={12} md={12} sm={12}>
        <Card className="border-0 shadow-sm">
          <Card.Body className="p-0">
            <div className="d-flex justify-content-between align-items-center px-4 py-3 border-bottom">
              <div>
                <h4 className="mb-0 fw-bold">IELTS Master</h4>
              </div>
              <div className="d-flex align-items-center gap-3">
                <Nav className="nav-pills">
                  <Nav.Item>
                    <Nav.Link
                      as={Link}
                      to="/dashboard"
                      active={page === "dashboard"}
                      className="px-3"
                    >
                      Trang chủ
                    </Nav.Link>
                  </Nav.Item>
                  <Nav.Item>
                    <Nav.Link
                      as={Link}
                      to="#"
                      active={page === "courses"}
                      className="px-3"
                    >
                      Khóa học
                    </Nav.Link>
                  </Nav.Item>
                  <Nav.Item>
                    <Nav.Link
                      as={Link}
                      to="#"
                      active={page === "materials"}
                      className="px-3"
                    >
                      Tài liệu
                    </Nav.Link>
                  </Nav.Item>
                  <Nav.Item>
                    <Nav.Link
                      as={Link}
                      to="#"
                      active={page === "community"}
                      className="px-3"
                    >
                      Cộng đồng
                    </Nav.Link>
                  </Nav.Item>
                </Nav>
                <div className="d-flex align-items-center gap-3">
                  <div className="position-relative">
                    <FiBell size={20} className="text-muted cursor-pointer" />
                    <span
                      className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
                      style={{ fontSize: "0.6rem" }}
                    >
                      1
                    </span>
                  </div>
                  <div className="d-flex align-items-center gap-2">
                    <div className="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center" style={{ width: "32px", height: "32px" }}>
                      <span className="fw-bold">
                        {user?.fullName?.charAt(0) || user?.username?.charAt(0) || "U"}
                      </span>
                    </div>
                    <span className="text-muted small">
                      Xin chào, {user?.fullName?.split(" ")[0] || user?.username || "User"}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </Card.Body>
        </Card>
      </Col>
    </Row>
  );
};

export default ProfileCover;
