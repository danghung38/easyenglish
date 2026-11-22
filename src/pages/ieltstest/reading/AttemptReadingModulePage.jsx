import React, { useState, useEffect, useRef } from "react";
import {
  Stack,
  Navbar,
  Card,
  Container,
  Row,
  Col,
  Modal,
  Accordion,
  Table,
  Badge,
  Button,
} from "react-bootstrap";
import { createUserExam, getUserExamById, getExamParts, getQuestionsByExamPart, submitRLPart, getReadingPart, getMyExams } from "../../../services/examService";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { FiArrowLeft, FiArrowRight, FiCheckCircle } from "react-icons/fi";
import { Alert, Spinner } from "react-bootstrap";
import { MiniNavBar } from "../../../components/ieltstest/MiniNavBar";
import ReadingFooter from "../../../components/ieltstest/reading/ReadingFooter";
import ReadingSection from "../../../components/ieltstest/reading/ReadingSection";
import ReadingPassage from "../../../components/ieltstest/reading/ReadingPassage";
import ToeicReadingSection from "../../../components/ieltstest/reading/ToeicReadingSection";
import CustomSplitPane from "../../../components/layout/CustomSplitPane";
import BookInfo from "../../../components/ieltstest/listening/BookInfo";

const AttemptReadingModulePage = () => {
  const { examId } = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  
  // Debug: Log examId to check if it's being parsed correctly
  console.log('📌 AttemptReadingModulePage - examId from useParams():', examId, typeof examId);
  
  // Full Test mode detection (UI only)
  const isFullTest = searchParams.get('fullTest') === 'true';
  
  // State management
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  
  // Exam data
  const [userExam, setUserExam] = useState(null);
  const [readingPart, setReadingPart] = useState(null);
  const [sections, setSections] = useState([]);
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState({});
  const [duration, setDuration] = useState(60);
  const [timeRemaining, setTimeRemaining] = useState(null); // in seconds
  
  // UI state for old layout
  const [deviceType, setDeviceType] = useState("desktop");
  const [currentSection, setCurrentSection] = useState(null);
  const [currentUserAnswerBySection, setCurrentUserAnswerBySection] = useState(null);
  const [userAnswerBySection, setUserAnswerBySection] = useState({});
  const [isFooterExpanded, setFooterExpanded] = useState(false);
  const [showReviewModal, setShowReviewModal] = useState(false);
  const [showTestInfoModal, setShowTestInfoModal] = useState(false);
  const [questionData, setQuestionData] = useState({ completed_questions: 0 });

  useEffect(() => {
    const abortController = new AbortController();
    let isActive = true;
    
    const init = async () => {
      if (!isActive) return;
      document.title = "TOEIC Reading Test | KeenIELTS";
      await initializeExam();
    };
    
    init();
    
    return () => {
      isActive = false;
      abortController.abort();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [examId]);

  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth < 768) {
        setDeviceType("mobile");
      } else {
        setDeviceType("desktop");
      }
    };
    handleResize();
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  useEffect(() => {
    const completed_questions = Object.keys(answers).length;
    setQuestionData({ completed_questions });
  }, [answers]);

  // Countdown timer - Update when duration changes
  useEffect(() => {
    if (duration) {
      console.log('⏰ Initializing timer with duration:', duration, 'minutes =', duration * 60, 'seconds');
      setTimeRemaining(duration * 60); // Convert minutes to seconds
    }
  }, [duration]); // Remove null check to allow reset when duration changes

  useEffect(() => {
    if (timeRemaining === null || timeRemaining <= 0) return;

    const timer = setInterval(() => {
      setTimeRemaining(prev => {
        if (prev <= 1) {
          clearInterval(timer);
          // Auto submit when time runs out
          handleConfirmSubmit();
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

      let userExamData;

      // In Full Test mode, reuse existing UserExam from sessionStorage
      if (isFullTest) {
        const savedUserExamId = sessionStorage.getItem(`fullTest_userExamId_${examId}`);
        if (savedUserExamId) {
          console.log('Full Test mode: Reusing existing UserExam ID:', savedUserExamId);
          const response = await getUserExamById(Number(savedUserExamId));
          if (response.code === 200) {
            userExamData = response.result;
            console.log('Reused UserExam:', userExamData);
          }
        }
      }

      // If not Full Test or no saved UserExam, create new one
      if (!userExamData) {
        console.log('Creating new user exam for examId:', examId, 'skillType: READING');
        const userExamResponse = await createUserExam(Number(examId), 'READING');
        
        if (userExamResponse.code !== 201) {
          throw new Error('Failed to create user exam');
        }
        
        userExamData = userExamResponse.result;
        console.log('User exam created:', userExamData);
      }

      setUserExam(userExamData);

      const readingPartData = getReadingPart(userExamData);
      if (!readingPartData) {
        throw new Error('Reading part not found in exam');
      }
      setReadingPart(readingPartData);

      const examPartsResponse = await getExamParts(Number(examId));
      if (examPartsResponse.code !== 200) {
        throw new Error('Failed to fetch exam parts');
      }

      const examParts = examPartsResponse.result;
      const readingExamPart = examParts.find(part => part.skillType === 'READING');
      
      if (!readingExamPart) {
        throw new Error('Reading exam part not found');
      }

      console.log('⏱️ ExamPart duration:', readingExamPart.duration);
      
      if (readingExamPart.duration) {
        setDuration(readingExamPart.duration);
        console.log('✅ Duration set to:', readingExamPart.duration, 'minutes');
      } else {
        console.warn('⚠️ No duration found in ExamPart, using default 60 minutes');
      }

      // Fetch questions by examPartId
      const questionsResponse = await getQuestionsByExamPart(readingExamPart.id);
      if (questionsResponse.code !== 200) {
        throw new Error('Failed to fetch questions');
      }

      const questionsData = questionsResponse.result?.items || [];
      
      // Group questions by section/passage
      const groupedSections = groupQuestionsBySection(questionsData);
      setSections(groupedSections);
      setQuestions(questionsData);
      
      // Set current section for old UI
      if (groupedSections.length > 0) {
        setCurrentSection(groupedSections[0]);
      }
      
      console.log(`Loaded ${groupedSections.length} sections with ${questionsData.length} total questions`);

      setLoading(false);
    } catch (err) {
      console.error('Error initializing exam:', err);
      setError(err.message || 'Failed to load exam');
      setLoading(false);
    }
  };

  // Helper function to group questions by section
  const groupQuestionsBySection = (questions) => {
    const grouped = {};
    questions.forEach((question, index) => {
      const sectionKey = question.section || question.imageUrl || `section_${index}`;
      if (!grouped[sectionKey]) {
        grouped[sectionKey] = {
          id: sectionKey,
          section: question.section || `Passage ${Object.keys(grouped).length + 1}`,
          imageUrl: question.imageUrl,
          questions: [],
        };
      }
      grouped[sectionKey].questions.push(question);
    });
    return Object.values(grouped);
  };

  const handleAnswerChange = (questionId, optionId) => {
    setAnswers(prev => ({
      ...prev,
      [questionId]: optionId
    }));
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

  const handlePreviousSectionButton = () => {
    const currentIndex = sections.findIndex(s => s.id === currentSection.id);
    const previousIndex = currentIndex - 1;
    if (previousIndex >= 0) {
      setCurrentSection(sections[previousIndex]);
    }
  };

  const handleNextSectionButton = () => {
    const currentIndex = sections.findIndex(s => s.id === currentSection.id);
    const nextIndex = currentIndex + 1;
    if (nextIndex < sections.length) {
      setCurrentSection(sections[nextIndex]);
    }
  };

  const updateCurrentSection = (id) => {
    const newSection = sections.find(s => s.id === id);
    setCurrentSection(newSection);
  };

  const isFirstSection = currentSection ? currentSection.id === sections[0]?.id : false;
  const isLastSection = currentSection ? currentSection.id === sections[sections.length - 1]?.id : false;

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

      const submitResponse = await submitRLPart(readingPart.id, formattedAnswers);
      
      if (submitResponse.code === 200) {
        console.log('Submit successful:', submitResponse.result);
        
        // Full Test mode: navigate to Writing, else to result page
        if (isFullTest) {
          navigate(`/ieltstest/attempt/writing/${examId}?fullTest=true`);
        } else {
          navigate(`/ieltstest/attempt/reading/result/${readingPart.id}`, {
            state: { 
              userExamPartId: readingPart.id,
              score: submitResponse.result.score 
            }
          });
        }
      } else {
        throw new Error('Failed to submit answers');
      }
    } catch (err) {
      console.error('Error submitting answers:', err);
      setError(err.message || 'Failed to submit answers');
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Container className="my-5 text-center">
        <Spinner animation="border" role="status">
          <span className="visually-hidden">Loading...</span>
        </Spinner>
        <p className="mt-3">Loading exam...</p>
      </Container>
    );
  }

  if (error) {
    return (
      <Container className="my-5">
        <Alert variant="danger">
          <Alert.Heading>Error Loading Exam</Alert.Heading>
          <p>{error}</p>
          <Button variant="outline-danger" onClick={() => navigate('/ieltstest/reading')}>
            Back to Reading Tests
          </Button>
        </Alert>
      </Container>
    );
  }

  if (!currentSection) {
    return (
      <Container className="py-5 text-center">
        <Alert variant="info">
          <h5>Loading sections...</h5>
          <p>Please wait while we prepare your test.</p>
        </Alert>
      </Container>
    );
  }

  return (
    <>
      {/* Main Content - 3 Columns Layout */}
      <Container fluid className="my-4">
        <Row>
          {/* Left & Center: Sections with Passages and Questions */}
          <Col lg={9} md={8} sm={12}>
            {sections.map((section, sectionIndex) => {
              const isToeicFormat = section?.imageUrl || 
                (section?.questions && section.questions.length > 0 && section.questions[0]?.imageUrl);
              
              return (
                <Card key={section.id} className="mb-4 shadow-sm" id={`section-${sectionIndex}`}>
                  <Card.Header className="bg-primary text-white">
                    <div className="d-flex justify-content-between align-items-center">
                      <div>
                        <h4 className="mb-0">{section.section}</h4>
                        <small>Questions {getQuestionRange(sectionIndex)}</small>
                      </div>
                      {isFullTest && sectionIndex === 0 && (
                        <Badge bg="light" text="primary" className="px-3 py-2">
                          <i className="fe fe-layers me-1"></i>
                          FULL TEST - Part 2 of 4
                        </Badge>
                      )}
                    </div>
                  </Card.Header>
                  
                  <Card.Body>
                    <Row>
                      {/* Left: Passage/Image */}
                      <Col lg={6} md={12} className="border-end">
                        <div className="p-3">
                          <ReadingPassage 
                            section={section} 
                            imageUrl={section?.imageUrl}
                          />
                        </div>
                      </Col>
                      
                      {/* Right: Questions */}
                      <Col lg={6} md={12}>
                        <div className="p-3">
                          {isToeicFormat ? (
                            <ToeicReadingSection
                              section={section}
                              questions={section?.questions || []}
                              selectedAnswers={answers}
                              onAnswerChange={handleAnswerChange}
                            />
                          ) : (
                            <div>
                              {section?.questions?.map((question, qIndex) => {
                                const globalIndex = sections.slice(0, sectionIndex).reduce((acc, s) => 
                                  acc + (s.questions?.length || 0), 0) + qIndex;
                                return (
                                  <div 
                                    key={question.id} 
                                    id={`question-${question.id}`}
                                    className="mb-4 p-3 border rounded bg-light"
                                    style={{ scrollMarginTop: '100px' }}
                                  >
                                    <h6 className="fw-bold text-primary">Question {globalIndex + 1}</h6>
                                    <div dangerouslySetInnerHTML={{ __html: question.content }} />
                                    <div className="mt-3">
                                      {question.optionRes?.map((option, optIndex) => (
                                        <div key={option.id} className="form-check mb-2">
                                          <input
                                            className="form-check-input"
                                            type="radio"
                                            name={`question-${question.id}`}
                                            id={`option-${option.id}`}
                                            value={option.id}
                                            checked={answers[question.id] === option.id}
                                            onChange={() => handleAnswerChange(question.id, option.id)}
                                          />
                                          <label className="form-check-label" htmlFor={`option-${option.id}`}>
                                            <strong>({String.fromCharCode(65 + optIndex)})</strong> {option.content}
                                          </label>
                                        </div>
                                      ))}
                                    </div>
                                  </div>
                                );
                              })}
                            </div>
                          )}
                        </div>
                      </Col>
                    </Row>
                  </Card.Body>
                </Card>
              );
            })}
          </Col>

          {/* Right Sidebar: Question Navigator + Timer + Submit */}
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
                    const startQuestionNumber = sections.slice(0, sectionIndex).reduce((acc, s) => 
                      acc + (s.questions?.length || 0), 0);

                    return (
                      <div key={sectionIndex} className="mb-3">
                        <h6 className="text-primary fw-bold mb-2" style={{ fontSize: '12px' }}>
                          {section.section}
                        </h6>
                        <div className="row g-2">
                          {section.questions?.map((question, qIndex) => {
                            const globalQuestionNumber = startQuestionNumber + qIndex + 1;
                            const isAnswered = answers[question.id];
                            
                            return (
                              <div key={question.id} className="col-3">
                                <button
                                  type="button"
                                  className={`btn btn-sm w-100 ${
                                    isAnswered ? 'btn-success' : 'btn-outline-secondary'
                                  }`}
                                  style={{ fontSize: '12px', padding: '6px', cursor: 'pointer' }}
                                  onClick={() => {
                                    const element = document.getElementById(`question-${question.id}`);
                                    if (element) {
                                      const yOffset = -120; // Offset for sticky header
                                      const y = element.getBoundingClientRect().top + window.pageYOffset + yOffset;
                                      window.scrollTo({ top: y, behavior: 'smooth' });
                                    }
                                  }}
                                >
                                  {isAnswered && <span className="me-1">✓</span>}
                                  {globalQuestionNumber}
                                </button>
                              </div>
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

              {/* Submit Button */}
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

      <Modal show={showReviewModal} onHide={() => setShowReviewModal(false)} centered className="p-0">
        <Modal.Header closeButton>
          <Modal.Title>Review Answers</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Table striped bordered size="sm">
            <thead>
              <tr>
                <th scope="col">Question</th>
                <th scope="col">Your Answer</th>
              </tr>
            </thead>
            <tbody>
              {questions.map((question, index) => {
                const userAnswer = answers[question.id];
                const selectedOption = question.optionRes?.find(opt => opt.id === userAnswer);
                const optionLetter = selectedOption ? 
                  String.fromCharCode(65 + question.optionRes.indexOf(selectedOption)) : '-';
                
                return (
                  <tr key={question.id}>
                    <td>Question {index + 1}</td>
                    <td className="fw-bold text-black">
                      {userAnswer ? optionLetter : <span className="text-muted">Not answered</span>}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </Table>
        </Modal.Body>
        <div className="modal-footer py-2">
          <button type="button" className="btn btn-outline-primary" onClick={() => setShowReviewModal(false)}>
            Close
          </button>
        </div>
      </Modal>

      <Modal show={showTestInfoModal} onHide={() => setShowTestInfoModal(false)} centered className="p-0">
        <Modal.Header closeButton>
          <Modal.Title>Test Info</Modal.Title>
        </Modal.Header>
        <Modal.Body className="p-0">
          <BookInfo 
            module={{
              id: examId,
              title: "Reading Test",
              sections: sections,
              total_questions: questions.length
            }} 
            attempt_slug={readingPart?.id}
          />
        </Modal.Body>
        <div className="modal-footer py-2">
          <button type="button" className="btn btn-outline-primary" onClick={() => setShowTestInfoModal(false)}>
            Close
          </button>
        </div>
      </Modal>
    </>
  );
};

export default AttemptReadingModulePage;
