import React, { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Container, Spinner } from 'react-bootstrap';

/**
 * Simple redirector to start Full Test mode
 * Redirects to Listening page with fullTest=true flag
 */
const AttemptFullTestPage = () => {
  const { examId } = useParams();
  const navigate = useNavigate();

  useEffect(() => {
    // Simply redirect to Listening with Full Test flag
    navigate(`/ieltstest/attempt/listening/${examId}?fullTest=true`, { replace: true });
  }, [examId, navigate]);

  return (
    <Container className="my-5 text-center">
      <Spinner animation="border" variant="primary" style={{ width: '4rem', height: '4rem' }}>
        <span className="visually-hidden">Loading...</span>
      </Spinner>
      <h4 className="mt-4 fw-bold">Starting Full Test...</h4>
    </Container>
  );
};

export default AttemptFullTestPage;