import React, { useContext, useEffect, useState } from "react";
import { Container, Row, Col, Card, Button } from "react-bootstrap";
import { Link, useLocation, useNavigate } from "react-router-dom";
import AuthContext from "../../context/AuthContext";

const VerifyEmailPage = () => {
  const location = useLocation();
  const [email, setEmail] = useState("");
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

  useEffect(() => {
    // Get email from URL query params
    const queryParams = new URLSearchParams(location.search);
    const emailParam = queryParams.get("email");
    if (emailParam) {
      setEmail(emailParam);
    }
  }, [location]);

  useEffect(() => {
    // Redirect if already logged in
    if (user) {
      navigate("/");
    }
  }, [user, navigate]);

  return (
    <>
      <Container className="mb-5">
        <Row className="align-items-center justify-content-center mt-5">
          <Col lg={6} md={8} className="">
            <Card className="shadow-sm">
              <Card.Body className="p-5 text-center">
                {/* Email Icon */}
                <div className="mb-4">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="80"
                    height="80"
                    fill="#0d6efd"
                    className="bi bi-envelope-check"
                    viewBox="0 0 16 16"
                  >
                    <path d="M2 2a2 2 0 0 0-2 2v8.01A2 2 0 0 0 2 14h5.5a.5.5 0 0 0 0-1H2a1 1 0 0 1-.966-.741l5.64-3.471L8 9.583l7-4.2V8.5a.5.5 0 0 0 1 0V4a2 2 0 0 0-2-2H2Zm3.708 6.208L1 11.105V5.383l4.708 2.825ZM1 4.217V4a1 1 0 0 1 1-1h12a1 1 0 0 1 1 1v.217l-7 4.2-7-4.2Z" />
                    <path d="M16 12.5a3.5 3.5 0 1 1-7 0 3.5 3.5 0 0 1 7 0Zm-1.993-1.679a.5.5 0 0 0-.686.172l-1.17 1.95-.547-.547a.5.5 0 0 0-.708.708l.774.773a.75.75 0 0 0 1.174-.144l1.335-2.226a.5.5 0 0 0-.172-.686Z" />
                  </svg>
                </div>

                <h2 className="fw-bold mb-3">Check Your Email</h2>
                
                <p className="text-muted mb-4">
                  We've sent a verification email to:
                </p>
                
                <p className="fw-bold text-primary mb-4">{email || "your email address"}</p>
                
                <div className="alert alert-info text-start mb-4">
                  <h6 className="fw-bold mb-2">📧 What to do next:</h6>
                  <ol className="mb-0 ps-3">
                    <li>Check your inbox (and spam folder)</li>
                    <li>Click the verification link in the email</li>
                    <li>Your account will be activated immediately</li>
                  </ol>
                </div>

                <div className="alert alert-warning text-start mb-4">
                  <small>
                    <strong>⏰ Note:</strong> The verification link will expire in 30 minutes.
                  </small>
                </div>

                <div className="d-grid gap-2">
                  <Button
                    variant="primary"
                    size="lg"
                    as={Link}
                    to="/login"
                  >
                    Go to Login Page
                  </Button>
                  
                  <Button
                    variant="outline-secondary"
                    as={Link}
                    to="/register"
                  >
                    Back to Register
                  </Button>
                </div>

                <hr className="my-4" />

                <p className="text-muted small mb-0">
                  Didn't receive the email? Check your spam folder or contact support.
                </p>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
    </>
  );
};

export default VerifyEmailPage;
