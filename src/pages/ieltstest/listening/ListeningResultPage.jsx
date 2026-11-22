/**
 * ListeningResultPage - Rewritten for Spring Boot backend
 * 
 * Displays test results with score and correct/incorrect answers
 */

import React, { useEffect, useState } from "react";
import { useParams, useLocation, useNavigate } from "react-router-dom";
import { Card, Col, Container, Row, Table, Spinner, Alert, Button, Modal } from "react-bootstrap";
import { getUserAnswers } from "../../../services/examService";
import CustomAudioPlayer from "../../../components/elements/audioplayer/CustomAudioPlayer";

const ListeningResultPage = () => {
  const { examId, userExamPartId: urlUserExamPartId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [userAnswers, setUserAnswers] = useState([]);
  const [score, setScore] = useState(null);
  const [userExamPartId, setUserExamPartId] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [selectedQuestion, setSelectedQuestion] = useState(null);

  useEffect(() => {
    document.title = "TOEIC Listening Test Result | KeenIELTS";
    
    // Priority: URL params > location.state
    const partId = urlUserExamPartId || location.state?.userExamPartId;
    
    console.log('📍 URL userExamPartId:', urlUserExamPartId);
    console.log('📍 Location state:', location.state);
    console.log('📍 Final userExamPartId:', partId);
    
    if (partId) {
      setUserExamPartId(partId);
      setScore(location.state?.score || null);
      fetchResults(partId);
    } else {
      setError('No exam data found. Please take the test first.');
      setLoading(false);
    }
  }, [urlUserExamPartId, location.state]);

  const fetchResults = async (userExamPartId) => {
    try {
      setLoading(true);
      console.log('Fetching results for userExamPartId:', userExamPartId);
      
      const answersResponse = await getUserAnswers(userExamPartId);
      
      if (answersResponse.code === 200) {
        const answers = answersResponse.result || [];
        setUserAnswers(answers);
        
        console.log('User answers loaded:', answers);
        console.log('Sample answer structure:', answers[0]);
        console.log('Scores:', answers.map(a => ({ id: a.id, score: a.score, selected: a.selectedOptionId })));
      } else {
        throw new Error(answersResponse.message || 'Failed to fetch results');
      }
      
      setLoading(false);
    } catch (err) {
      console.error('Error fetching results:', err);
      setError(err.message || 'Failed to load results');
      setLoading(false);
    }
  };

  const calculateStats = () => {
    // Sử dụng score từ backend: 1.0 = correct, 0.0 = incorrect
    const correct = userAnswers.filter(answer => answer.score === 1.0).length;
    const total = userAnswers.length;
    const percentage = total > 0 ? ((correct / total) * 100).toFixed(1) : 0;
    
    return { correct, total, percentage };
  };

  // Helper function to check if answer is correct using score from backend
  const isAnswerCorrect = (answer) => {
    return answer.score === 1.0;
  };

  // Helper function to get selected option text
  const getSelectedOptionText = (answer) => {
    if (!answer.selectedOptionId || !answer.question?.optionRes) return 'No answer';
    const selectedOption = answer.question.optionRes.find(opt => opt.id === answer.selectedOptionId);
    return selectedOption ? selectedOption.content : 'No answer';
  };

  // Helper function to get option letter (A, B, C, D)
  const getOptionLetter = (question, optionId) => {
    if (!optionId || !question?.optionRes) return '-';
    const optionIndex = question.optionRes.findIndex(opt => opt.id === optionId);
    return optionIndex >= 0 ? String.fromCharCode(65 + optionIndex) : '-';
  };

  // Helper function to get correct answer letter
  const getCorrectAnswerLetter = (answer) => {
    // Try multiple ways to get correct answer
    let correctOptionId = null;
    
    // Method 1: From correctOption object
    if (answer.question?.correctOption?.id) {
      correctOptionId = answer.question.correctOption.id;
    }
    // Method 2: From correctOptionId field
    else if (answer.question?.correctOptionId) {
      correctOptionId = answer.question.correctOptionId;
    }
    // Method 3: Find option where isCorrect = true
    else if (answer.question?.optionRes) {
      const correctOpt = answer.question.optionRes.find(opt => opt.isCorrect === true);
      if (correctOpt) {
        correctOptionId = correctOpt.id;
      }
    }
    
    return getOptionLetter(answer.question, correctOptionId);
  };

  // Helper function to get user answer letter
  const getUserAnswerLetter = (answer) => {
    return getOptionLetter(answer.question, answer.selectedOptionId);
  };

  // Open modal with question details
  const openQuestionModal = (answer, index) => {
    setSelectedQuestion({ answer, index });
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
    setSelectedQuestion(null);
  };

  if (loading) {
    return (
      <Container className="my-5 text-center">
        <Spinner animation="border" role="status">
          <span className="visually-hidden">Loading results...</span>
        </Spinner>
        <p className="mt-3">Loading results...</p>
      </Container>
    );
  }

  if (error) {
    return (
      <Container className="my-5">
        <Alert variant="danger">
          <Alert.Heading>Error Loading Results</Alert.Heading>
          <p>{error}</p>
          <Button variant="outline-danger" onClick={() => navigate('/ieltstest/listening')}>
            Back to Listening Tests
          </Button>
        </Alert>
      </Container>
    );
  }

  const stats = calculateStats();

  return (
    <div className="mt-4 mb-4">
      <Container>
        <Row className="justify-content-center">
          <Col xl={8} lg={10} md={12}>
            <Card className="mb-3">
              <Card.Header className="bg-primary text-white">
                <h4 className="mb-0">TOEIC Listening Test Result</h4>
              </Card.Header>
              <Card.Body>
                <Row className="text-center">
                  <Col md={4}>
                    <h2 className="text-success">{stats.correct}</h2>
                    <p className="text-muted">Correct Answers</p>
                  </Col>
                  <Col md={4}>
                    <h2 className="text-danger">{stats.total - stats.correct}</h2>
                    <p className="text-muted">Incorrect Answers</p>
                  </Col>
                  <Col md={4}>
                    <h2 className="text-primary">{stats.percentage}%</h2>
                    <p className="text-muted">Accuracy</p>
                  </Col>
                </Row>
                {score !== null && (
                  <Row className="mt-3">
                    <Col className="text-center">
                      <h3>Final Score: <span className="text-success">{score.toFixed(2)}</span></h3>
                      <p className="text-muted">Out of {stats.total} questions</p>
                    </Col>
                  </Row>
                )}
              </Card.Body>
            </Card>

            <Card className="mb-3">
              <Card.Header>
                <h5 className="mb-0">Answer Review</h5>
                <small className="text-muted">Click on any question to view transcript and explanation</small>
              </Card.Header>
              <Card.Body>
                <Table striped bordered hover responsive>
                  <thead>
                    <tr>
                      <th style={{ width: '60px' }}>#</th>
                      <th>Question</th>
                      <th style={{ width: '120px' }}>Your Answer</th>
                      <th style={{ width: '120px' }}>Correct Answer</th>
                      <th style={{ width: '80px' }}>Result</th>
                    </tr>
                  </thead>
                  <tbody>
                    {userAnswers.map((answer, index) => {
                      const isCorrect = isAnswerCorrect(answer);
                      const userAnswerLetter = getUserAnswerLetter(answer);
                      const correctAnswerLetter = getCorrectAnswerLetter(answer);
                      
                      return (
                        <tr key={answer.id || index}>
                          <td className="fw-bold text-center">{index + 1}</td>
                          <td>
                            <div dangerouslySetInnerHTML={{ 
                              __html: answer.question?.content || 'Question' 
                            }} />
                            {(answer.question?.transcript || answer.question?.explain) && (
                              <Button 
                                variant="link" 
                                size="sm" 
                                className="p-0 mt-1"
                                onClick={() => openQuestionModal(answer, index)}
                              >
                                ▶ View details
                              </Button>
                            )}
                          </td>
                          <td className="text-center">
                            <span 
                              className={`badge ${isCorrect ? 'bg-success' : 'bg-danger'} fs-6 px-3 py-2`}
                            >
                              {userAnswerLetter && userAnswerLetter !== '-' ? userAnswerLetter : 'Not answered'}
                            </span>
                          </td>
                          <td className="text-center">
                            <span className="badge bg-primary fs-6 px-3 py-2">
                              {correctAnswerLetter}
                            </span>
                          </td>
                          <td className="text-center">
                            {isCorrect ? (
                              <span className="text-success fw-bold" style={{ fontSize: '24px' }}>✓</span>
                            ) : (
                              <span className="text-danger fw-bold" style={{ fontSize: '24px' }}>✗</span>
                            )}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </Table>
              </Card.Body>
            </Card>

            <Card>
              <Card.Body className="text-center">
                <Button 
                  variant="primary" 
                  onClick={() => navigate('/ieltstest/listening')}
                  className="me-2"
                >
                  Back to Listening Tests
                </Button>
                <Button 
                  variant="outline-primary" 
                  onClick={() => navigate('/dashboard')}
                >
                  Go to Dashboard
                </Button>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>

      {/* Question Details Modal */}
      <Modal show={showModal} onHide={closeModal} size="lg" centered>
        <Modal.Header closeButton>
          <Modal.Title>
            Question {selectedQuestion ? selectedQuestion.index + 1 : ''} - Details
          </Modal.Title>
        </Modal.Header>
        <Modal.Body style={{ maxHeight: '70vh', overflowY: 'auto' }}>
          {selectedQuestion && (
            <div>
              {/* Audio Player */}
              {selectedQuestion.answer.question?.section?.audioUrl && (
                <div className="mb-4">
                  <h5 className="text-primary mb-3">🎧 Section Audio</h5>
                  <CustomAudioPlayer audioUrl={selectedQuestion.answer.question.section.audioUrl} />
                </div>
              )}

              {/* Transcript */}
              {selectedQuestion.answer.question?.transcript && (
                <div className="mb-4">
                  <h5 className="text-info mb-3">📝 Audio Transcript</h5>
                  <div 
                    className="p-3 bg-light rounded border"
                    style={{ 
                      whiteSpace: 'pre-wrap',
                      lineHeight: '1.8',
                      fontSize: '0.95rem'
                    }}
                    dangerouslySetInnerHTML={{ __html: selectedQuestion.answer.question.transcript }}
                  />
                </div>
              )}

              {/* Question */}
              <div className="mb-4">
                <h5 className="text-dark mb-3">❓ Question</h5>
                <div 
                  className="p-3 bg-white rounded border"
                  dangerouslySetInnerHTML={{ 
                    __html: selectedQuestion.answer.question?.content || 'Question' 
                  }}
                />
                
                {/* Options */}
                {selectedQuestion.answer.question?.optionRes && (
                  <div className="mt-3">
                    {selectedQuestion.answer.question.optionRes.map((option, idx) => {
                      const optionLetter = String.fromCharCode(65 + idx);
                      const isUserAnswer = option.id === selectedQuestion.answer.selectedOptionId;
                      const isCorrectAnswer = option.isCorrect || option.id === selectedQuestion.answer.question?.correctOptionId;
                      
                      return (
                        <div 
                          key={option.id} 
                          className="p-3 mb-2 rounded"
                          style={{
                            backgroundColor: isCorrectAnswer 
                              ? 'rgba(25, 135, 84, 0.05)' 
                              : isUserAnswer 
                              ? 'rgba(220, 53, 69, 0.05)' 
                              : '#f8f9fa',
                            border: isCorrectAnswer 
                              ? '2px solid #198754' 
                              : isUserAnswer 
                              ? '2px solid #dc3545' 
                              : '1px solid #dee2e6',
                            position: 'relative'
                          }}
                        >
                          <div className="d-flex align-items-start">
                            <strong 
                              className="me-2"
                              style={{ 
                                minWidth: '30px',
                                color: isCorrectAnswer ? '#198754' : isUserAnswer ? '#dc3545' : '#212529',
                                fontSize: '1.1rem'
                              }}
                            >
                              {optionLetter}.
                            </strong>
                            <span 
                              className="flex-grow-1"
                              style={{ 
                                color: '#212529',
                                lineHeight: '1.6'
                              }}
                              dangerouslySetInnerHTML={{ __html: option.content }} 
                            />
                            <div className="ms-2">
                              {isCorrectAnswer && (
                                <span className="badge bg-success" style={{ fontSize: '0.85rem' }}>
                                  ✓ Correct
                                </span>
                              )}
                              {isUserAnswer && !isCorrectAnswer && (
                                <span className="badge bg-danger" style={{ fontSize: '0.85rem' }}>
                                  ✗ Your answer
                                </span>
                              )}
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>

              {/* Explanation */}
              {selectedQuestion.answer.question?.explain && (
                <div className="mb-3">
                  <h5 className="text-primary mb-3">💡 Explanation</h5>
                  <div 
                    className="p-3 bg-light rounded border"
                    style={{ lineHeight: '1.8' }}
                    dangerouslySetInnerHTML={{ 
                      __html: selectedQuestion.answer.question.explain 
                    }}
                  />
                </div>
              )}
            </div>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={closeModal}>
            Close
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
};

export default ListeningResultPage;
