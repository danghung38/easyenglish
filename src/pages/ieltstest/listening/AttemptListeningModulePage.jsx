/**
 * AttemptListeningModulePage - TOEIC Style Interface
 * 
 * Features:
 * - Simple multiple choice questions only
 * - 2 sections with separate audio files
 * - Support for images in questions
 * - Clean TOEIC-style layout
 * - Spring Boot backend integration
 */

import React, { useEffect, useState, useRef } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import {
  Modal,
  Stack,
  Row,
  Col,
  Container,
  Card,
  Button,
  Spinner,
  Alert,
  Badge,
} from "react-bootstrap";
import { FiCheckCircle } from "react-icons/fi";
import CountdownTimer from "../../../components/elements/CountdownTimer";
import {
  createUserExam,
  getExamParts,
  getListeningQuestionsByExamId,
  getListeningPart,
  submitRLPart,
  getMyExams,
} from "../../../services/examService";

const AttemptListeningModulePage = () => {
  const { examId } = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  
  // Debug: Log examId to check if it's being parsed correctly
  console.log('📌 AttemptListeningModulePage - examId from useParams():', examId, typeof examId);
  
  // Full Test mode detection (UI only - no logic changes)
  const isFullTest = searchParams.get('fullTest') === 'true';
  
  // State management
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [testStarted, setTestStarted] = useState(false);
  const [showModal, setShowModal] = useState(false);
  
  // Exam data
  const [userExam, setUserExam] = useState(null);
  const [listeningPart, setListeningPart] = useState(null);
  const [listeningExamPart, setListeningExamPart] = useState(null);
  const [sections, setSections] = useState([]);
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState({});
  const [duration, setDuration] = useState(40);
  const [timeRemaining, setTimeRemaining] = useState(null); // in seconds

  useEffect(() => {
    const abortController = new AbortController();
    let isActive = true;
    
    const init = async () => {
      if (!isActive) return;
      document.title = "TOEIC Listening Test | KeenIELTS";
      await initializeExam();
    };
    
    init();
    
    return () => {
      isActive = false;
      abortController.abort();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [examId]);

  // Countdown timer - Update when duration changes
  useEffect(() => {
    if (duration) {
      console.log('⏰ Initializing timer with duration:', duration, 'minutes =', duration * 60, 'seconds');
      setTimeRemaining(duration * 60); // Convert minutes to seconds
    }
  }, [duration]); // Remove timeRemaining from dependencies to allow reset

  useEffect(() => {
    if (timeRemaining === null || timeRemaining <= 0) return;

    const timer = setInterval(() => {
      setTimeRemaining(prev => {
        if (prev <= 1) {
          clearInterval(timer);
          handleTimesUp();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [timeRemaining]);

  // Format time for display
  const formatTime = (seconds) => {
    if (seconds === null) return '00:00';
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };



  /**
   * Initialize exam - Create user exam session and fetch questions
   */
  const initializeExam = async () => {
    try {
      setLoading(true);
      setError(null);

      // Validate examId before proceeding
      if (!examId) {
        throw new Error('No examId provided in URL. Please access this page from the exam list.');
      }

      // Always create a new UserExam for each attempt (retakes should be separate attempts)
      // If fullTest, create all parts. Otherwise, create only LISTENING part
      console.log('Creating new user exam for examId:', examId, 'isFullTest:', isFullTest);
      const userExamResponse = await createUserExam(Number(examId), isFullTest ? null : 'LISTENING');
      
      if (userExamResponse.code !== 201) {
        throw new Error(userExamResponse.message || 'Failed to create exam session');
      }
      
      const userExamData = userExamResponse.result;
      console.log('User exam created:', userExamData);

      // If Full Test mode, save userExamId to sessionStorage for reuse in other parts
      if (isFullTest) {
        sessionStorage.setItem(`fullTest_userExamId_${examId}`, userExamData.id);
        console.log('Saved Full Test userExamId to sessionStorage:', userExamData.id);
      }

      setUserExam(userExamData);

      const listeningPartData = getListeningPart(userExamData);
      if (!listeningPartData) {
        throw new Error('No listening part found in this exam');
      }
      setListeningPart(listeningPartData);

      const examPartsResponse = await getExamParts(Number(examId));
      if (examPartsResponse.code !== 200) {
        throw new Error('Failed to fetch exam parts');
      }

      const examParts = examPartsResponse.result;
      const listeningExamPart = examParts.find(part => part.skillType === 'LISTENING');
      
      console.log('🔍 All exam parts:', examParts);
      console.log('🎧 Found listening exam part:', listeningExamPart);
      
      if (!listeningExamPart) {
        throw new Error('No listening exam part found');
      }

      // Save examPart data for displaying content/passage
      setListeningExamPart(listeningExamPart);

      console.log('⏱️ ExamPart duration:', listeningExamPart.duration);
      
      if (listeningExamPart.duration) {
        setDuration(listeningExamPart.duration);
        console.log('✅ Duration set to:', listeningExamPart.duration, 'minutes');
      } else {
        console.warn('⚠️ No duration found in ExamPart, using default 40 minutes');
      }

      console.log('📝 About to fetch questions for examId:', examId);
      
      // Use new API: /questions/exam/{examId}/skill-type/LISTENING
      const questionsResponse = await getListeningQuestionsByExamId(Number(examId));
      console.log('📦 Questions API Response:', questionsResponse);
      
      if (questionsResponse.code !== 200) {
        throw new Error('Failed to fetch listening questions');
      }

      // API returns flat array of questions
      const allQuestions = questionsResponse.result || [];
      console.log('❓ Total questions fetched:', allQuestions.length);
      console.log('❓ Questions detail:', allQuestions);
      
      setQuestions(allQuestions);
      
      // Group questions by section for display
      const groupedSections = groupQuestionsBySection(allQuestions);
      console.log('📊 Grouped sections:', groupedSections);
      
      setSections(groupedSections);
      
      console.log(`✅ Loaded ${groupedSections.length} sections with ${allQuestions.length} total questions`);

      setLoading(false);
    } catch (err) {
      console.error('Error initializing exam:', err);
      setError(err.message || 'Failed to load exam');
      setLoading(false);
    }
  };



  const handleAnswerChange = (questionId, optionId) => {
    setAnswers(prev => ({
      ...prev,
      [questionId]: optionId
    }));
  };

  // Helper function to group questions by section
  const groupQuestionsBySection = (questions) => {
    const sectionMap = {};
    
    questions.forEach(question => {
      const sectionName = question.section || 'SECTION1';
      
      if (!sectionMap[sectionName]) {
        sectionMap[sectionName] = {
          sectionName: sectionName,
          sectionDisplayName: sectionName.replace('SECTION', 'Part '),
          audioUrl: question.audioUrl || null,
          instructions: question.instructions || null,
          questions: []
        };
      }
      
      sectionMap[sectionName].questions.push(question);
    });
    
    // Convert to array and sort by section name
    return Object.values(sectionMap).sort((a, b) => 
      a.sectionName.localeCompare(b.sectionName)
    );
  };

  // Helper function to get question range for each section
  const getQuestionRange = (sectionIndex) => {
    const previousQuestions = sections.slice(0, sectionIndex).reduce((acc, section) => 
      acc + (section.questions?.length || 0), 0);
    const currentQuestions = sections[sectionIndex]?.questions?.length || 0;
    const start = previousQuestions + 1;
    const end = previousQuestions + currentQuestions;
    return `${start}-${end}`;
  };

  const getCompletedQuestions = () => {
    return Object.keys(answers).length;
  };

  const handleSubmit = (event) => {
    if (event) event.preventDefault();
    setShowModal(true);
  };

  const handleConfirmSubmit = async () => {
    try {
      setShowModal(false);
      setLoading(true);

      const formattedAnswers = Object.entries(answers).map(([questionId, selectedOptionId]) => ({
        questionId: Number(questionId),
        selectedOptionId: Number(selectedOptionId)
      }));

      console.log('Submitting answers:', formattedAnswers);

      const submitResponse = await submitRLPart(listeningPart.id, formattedAnswers);
      
      if (submitResponse.code === 200) {
        console.log('Submission successful:', submitResponse.result);
        
        // Full Test mode: navigate to Reading, else to result page
        if (isFullTest) {
          navigate(`/ieltstest/attempt/reading/${examId}?fullTest=true`);
        } else {
          navigate(`/ieltstest/attempt/listening/result/${listeningPart.id}`, {
            state: { 
              userExamPartId: listeningPart.id,
              score: submitResponse.result.score 
            }
          });
        }
      } else {
        throw new Error(submitResponse.message || 'Failed to submit answers');
      }
    } catch (err) {
      console.error('Error submitting answers:', err);
      setError(err.message || 'Failed to submit answers');
      setLoading(false);
    }
  };

  const handleTimesUp = () => {
    handleConfirmSubmit();
  };

  if (loading && !testStarted) {
    return (
      <Container className="my-5 text-center">
        <Spinner animation="border" role="status">
          <span className="visually-hidden">Loading...</span>
        </Spinner>
        <p className="mt-3">Loading exam...</p>
      </Container>
    );
  }

  if (error && !testStarted) {
    return (
      <Container className="my-5">
        <Alert variant="danger">
          <Alert.Heading>Error Loading Exam</Alert.Heading>
          <p>{error}</p>
          <Button variant="outline-danger" onClick={() => navigate('/ieltstest/listening')}>
            Back to Listening Tests
          </Button>
        </Alert>
      </Container>
    );
  }

  return (
    <>
      <Modal show={showModal} onHide={() => setShowModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Nộp bài thi</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p>Bạn đã hoàn thành <strong>{getCompletedQuestions()}/{questions.length}</strong> câu hỏi.</p>
          <p>Thời gian còn lại: <strong className={timeRemaining < 300 ? "text-danger" : "text-primary"}>{formatTime(timeRemaining)}</strong></p>
          <p>Bạn có chắc chắn muốn nộp bài?</p>
        </Modal.Body>
        <Modal.Footer className="p-2">
          <Button variant="outline-primary" onClick={() => setShowModal(false)}>
            Hủy
          </Button>
          <Button variant="success" onClick={handleConfirmSubmit}>
            Nộp bài
          </Button>
        </Modal.Footer>
      </Modal>

      {!testStarted ? (
        <Container className="my-3">
          <Row className="justify-content-center">
            <Col sm={8}>
              <Card>
                <Card.Header className="bg-primary text-white">
                  <div className="d-flex justify-content-between align-items-center">
                    <h3 className="m-0 fw-bold">TOEIC Listening Test</h3>
                    {isFullTest && (
                      <Badge bg="light" text="primary" className="px-3 py-2">
                        <i className="fe fe-layers me-1"></i>
                        FULL TEST MODE - Part 1 of 4
                      </Badge>
                    )}
                  </div>
                </Card.Header>
                <Card.Body>
                  <p className="fw-bold" style={{ fontSize: "16px" }}>
                    Welcome to the  Listening Test. This test consists of 2 parts with multiple choice questions.
                  </p>
                  <p style={{ fontSize: "16px" }}>
                    <ul>
                      <li>
                        Please ensure your speakers or headphones are properly
                        connected and set to a comfortable volume level.
                      </li>
                      <li>
                        Each part has its own audio file. Listen carefully as audio will play only once.
                      </li>
                      <li>
                        You will have {duration} minutes to complete this test.
                      </li>
                      <li>
                        There are {questions.length} questions in total across 2 parts.
                      </li>
                      <li>
                        Select the best answer (A, B, C, or D) for each question.
                      </li>
                    </ul>
                  </p>
                </Card.Body>
                <Card.Footer>
                  <Button onClick={() => setTestStarted(true)}>
                    Start Test
                  </Button>
                </Card.Footer>
              </Card>
            </Col>
          </Row>
        </Container>
      ) : (
        <form onSubmit={handleSubmit}>
          <Container fluid className="my-4">
            <Row>
              {/* Left: Questions */}
              <Col lg={9} md={8} sm={12}>
                {/* Listening Exam Part Content/Passage - Display before sections */}
                {listeningExamPart?.content && (
                  <Card className="mb-4 shadow-sm">
                    <Card.Header className="bg-info text-white">
                      <h4 className="mb-0">
                        <i className="fe fe-file-text me-2"></i>
                        Listening Test Instructions & Context
                      </h4>
                    </Card.Header>
                    <Card.Body className="p-4">
                      <div 
                        className="listening-content fs-5" 
                        dangerouslySetInnerHTML={{ __html: listeningExamPart.content }}
                        style={{ lineHeight: '1.8' }}
                      />
                    </Card.Body>
                  </Card>
                )}

                {/* Show all listening sections */}
                {sections.map((section, sectionIndex) => (
                  <Card key={sectionIndex} className="mb-4 shadow-sm">
                    <Card.Header className="bg-primary text-white">
                      <Row className="align-items-center">
                        <Col>
                          <h4 className="mb-0">Part {sectionIndex + 1}</h4>
                          <small>Questions {getQuestionRange(sectionIndex)}</small>
                        </Col>
                        {section.audioUrl && (
                          <Col xs="auto">
                            <audio controls className="bg-white rounded">
                              <source src={section.audioUrl} type="audio/mpeg" />
                              Your browser does not support the audio element.
                            </audio>
                          </Col>
                        )}
                      </Row>
                    </Card.Header>
                    
                    <Card.Body className="p-4">
                      {section.instructions && (
                        <Alert variant="light" className="mb-4 border-start border-primary border-4">
                          <div dangerouslySetInnerHTML={{ __html: section.instructions }} />
                        </Alert>
                      )}
                      
                      {/* TOEIC Style - Vertical Layout */}
                      {section.questions?.map((question, questionIndex) => {
                        const globalQuestionNumber = sections.slice(0, sectionIndex).reduce((acc, prevSection) => 
                          acc + (prevSection.questions?.length || 0), 0) + questionIndex + 1;
                        
                        return (
                          <div key={question.id} id={`question-${question.id}`} className="mb-4 p-4 border rounded bg-light">
                            <div className="d-flex align-items-start mb-3">
                              <Badge bg="primary" className="me-3 fs-6 px-3 py-2">
                                {globalQuestionNumber}
                              </Badge>
                              <div className="flex-grow-1">
                                {/* Question Image if exists */}
                                {question.imageUrl && (
                                  <div className="mb-3 text-center">
                                    <img 
                                      src={question.imageUrl} 
                                      alt={`Question ${globalQuestionNumber}`}
                                      className="img-fluid rounded shadow-sm"
                                      style={{ maxHeight: '300px' }}
                                    />
                                  </div>
                                )}
                                
                                {/* Question Content */}
                                <div 
                                  className="mb-3 fw-medium fs-5" 
                                  dangerouslySetInnerHTML={{ __html: question.content }}
                                />
                              </div>
                            </div>
                            
                            {/* Multiple Choice Options - Vertical List */}
                            <div className="options-container ms-5">
                              {question.optionRes?.map((option, optIndex) => (
                                <div key={option.id} className="form-check mb-3 fs-6">
                                  <input
                                    className="form-check-input me-3"
                                    type="radio"
                                    name={`question-${question.id}`}
                                    id={`option-${option.id}`}
                                    value={option.id}
                                    checked={answers[question.id] === option.id}
                                    onChange={() => handleAnswerChange(question.id, option.id)}
                                  />
                                  <label
                                    className="form-check-label fw-normal"
                                    htmlFor={`option-${option.id}`}
                                  >
                                    <strong>({String.fromCharCode(65 + optIndex)})</strong> {option.content}
                                  </label>
                                </div>
                              ))}
                            </div>
                          </div>
                        );
                      })}
                    </Card.Body>
                  </Card>
                ))}
              </Col>

              {/* Right Sidebar */}
              <Col lg={3} md={4} sm={12}>
                <div className="sticky-top" style={{ top: '80px' }}>
                  {/* Timer Card */}
                  <Card className="mb-3 shadow-sm">
                    <Card.Body className="text-center py-3">
                      <h5 className="mb-2">Thời gian còn lại</h5>
                      <h2 
                        className={timeRemaining < 300 ? "text-danger" : "text-primary"}
                        style={{ fontSize: "48px", fontWeight: "bold" }}
                      >
                        {formatTime(timeRemaining)}
                      </h2>
                      {timeRemaining < 300 && timeRemaining > 0 && (
                        <small className="text-danger">Cảnh báo: Sắp hết giờ!</small>
                      )}
                    </Card.Body>
                  </Card>

                  {/* Progress Card */}
                  <Card className="mb-3 shadow-sm">
                    <Card.Body className="py-3">
                      <h6 className="mb-3">Tiến độ</h6>
                      <div className="d-flex justify-content-between mb-2">
                        <span>Đã làm:</span>
                        <strong className="text-success">{getCompletedQuestions()}</strong>
                      </div>
                      <div className="d-flex justify-content-between mb-2">
                        <span>Chưa làm:</span>
                        <strong className="text-warning">{questions.length - getCompletedQuestions()}</strong>
                      </div>
                      <div className="d-flex justify-content-between">
                        <span>Tổng số:</span>
                        <strong className="text-primary">{questions.length}</strong>
                      </div>
                      <div className="progress mt-3" style={{ height: "20px" }}>
                        <div 
                          className="progress-bar bg-success" 
                          role="progressbar" 
                          style={{ width: `${(getCompletedQuestions() / questions.length) * 100}%` }}
                          aria-valuenow={getCompletedQuestions()} 
                          aria-valuemin="0" 
                          aria-valuemax={questions.length}
                        >
                          {Math.round((getCompletedQuestions() / questions.length) * 100)}%
                        </div>
                      </div>
                    </Card.Body>
                  </Card>

                  {/* Question Navigator */}
                  <Card className="shadow-sm">
                    <Card.Header className="bg-secondary text-white">
                      <h6 className="mb-0">Danh sách câu hỏi</h6>
                    </Card.Header>
                    <Card.Body style={{ maxHeight: '400px', overflowY: 'auto' }}>
                      {sections.map((section, sectionIndex) => {
                        const startQuestionNumber = sections.slice(0, sectionIndex).reduce((acc, prevSection) => 
                          acc + (prevSection.questions?.length || 0), 0);

                        return (
                          <div key={sectionIndex} className="mb-3">
                            <h6 className="text-primary fw-bold mb-2 pb-2 border-bottom">
                              Part {sectionIndex + 1}
                            </h6>
                            <div className="d-flex flex-wrap gap-2">
                            {section.questions?.map((question, questionIndex) => {
                              const globalQuestionNumber = startQuestionNumber + questionIndex + 1;
                              const isAnswered = answers[question.id];
                              
                              return (
                                <button
                                  key={question.id}
                                  type="button"
                                  className={`btn ${
                                    isAnswered 
                                      ? 'btn-success' 
                                      : 'btn-outline-secondary'
                                  }`}
                                  style={{
                                    width: '40px',
                                    height: '40px',
                                    padding: '0',
                                    fontSize: '14px',
                                    fontWeight: 'bold'
                                  }}
                                  onClick={() => {
                                    const questionElement = document.getElementById(`question-${question.id}`);
                                    if (questionElement) {
                                      questionElement.scrollIntoView({ 
                                        behavior: 'smooth', 
                                        block: 'center' 
                                      });
                                    }
                                  }}
                                >
                                  {globalQuestionNumber}
                                  {isAnswered && ' ✓'}
                                </button>
                              );
                            })}
                          </div>
                        </div>
                      );
                    })}
                    </Card.Body>
                    <Card.Footer className="bg-light">
                      <div className="d-flex gap-2 align-items-center" style={{ fontSize: '11px' }}>
                        <div className="d-flex align-items-center">
                          <div className="btn btn-success btn-sm me-1" style={{ width: '18px', height: '18px', fontSize: '10px', padding: '0' }}>✓</div>
                          <small>Đã làm</small>
                        </div>
                        <div className="d-flex align-items-center">
                          <div className="btn btn-outline-secondary btn-sm me-1" style={{ width: '18px', height: '18px', padding: '0' }}></div>
                          <small>Chưa làm</small>
                        </div>
                      </div>
                    </Card.Footer>
                  </Card>

                  {/* Submit Button Card */}
                  <Card className="mt-3 shadow-sm">
                    <Card.Body className="text-center py-3">
                      <Button 
                        variant="success" 
                        size="lg"
                        className="w-100"
                        onClick={() => setShowModal(true)}
                      >
                        <FiCheckCircle className="me-2" size={20} />
                        Nộp bài
                      </Button>
                      <small className="text-muted d-block mt-2">
                        {getCompletedQuestions()}/{questions.length} câu đã hoàn thành
                      </small>
                    </Card.Body>
                  </Card>
                </div>
              </Col>
            </Row>
          </Container>
          
          {/* Floating Submit Button removed as requested */}
        </form>
      )}
    </>
  );
};

export default AttemptListeningModulePage;
