import { useEffect, useRef, useState, useCallback } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import {
  Container,
  Row,
  Col,
  Card,
  Button,
  Spinner,
  Alert,
  Badge,
  Modal,
  ButtonGroup,
} from "react-bootstrap";
import { FiArrowLeft, FiArrowRight, FiCheckCircle, FiClock } from "react-icons/fi";
import { 
  createUserExam,
  getUserExamById,
  getWritingQuestionsByExamId,
  evaluateWriting,
  submitWritingExam,
  getMyExams,
  getExamParts
} from "../../../services/examService";
import "./WritingExam.css";

const AttemptWritingModulePage = () => {
  const navigate = useNavigate();
  const { examId } = useParams();
  const [searchParams] = useSearchParams();
  
  // Debug: Log examId to check if it's being parsed correctly
  console.log('📌 AttemptWritingModulePage - examId from useParams():', examId, typeof examId);
  
  // Full Test mode detection (UI only)
  const isFullTest = searchParams.get('fullTest') === 'true';
  
  // State management
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [userExam, setUserExam] = useState(null);
  
  // Task navigation
  const [currentTaskIndex, setCurrentTaskIndex] = useState(0);
  const [answers, setAnswers] = useState({});
  
  // Timer state
  const [duration, setDuration] = useState(60); // Default 60 minutes
  const [timeRemaining, setTimeRemaining] = useState(null);
  const timerRef = useRef(null);
  
  // Evaluation state
  const [evaluating, setEvaluating] = useState(false);
  const [testCompleted, setTestCompleted] = useState(false);
  const [showResults, setShowResults] = useState(false);
  const [evaluationResults, setEvaluationResults] = useState({});
  const [overallBandScore, setOverallBandScore] = useState(0);
  const [writingPartId, setWritingPartId] = useState(null);
  
  // Modals
  const [showSubmitModal, setShowSubmitModal] = useState(false);

  useEffect(() => {
    const abortController = new AbortController();
    let isActive = true;
    
    const init = async () => {
      if (!isActive) return;
      await initializeExam();
    };
    
    init();
    
    return () => {
      isActive = false;
      abortController.abort();
      if (timerRef.current) clearInterval(timerRef.current);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [examId]);

  const initializeExam = async () => {
    try {
      setLoading(true);
      setError(null);

      console.log("🔍 Initializing Writing Exam with examId:", examId);

      let userExamData;

      // In Full Test mode, reuse existing UserExam from sessionStorage
      if (isFullTest) {
        const savedUserExamId = sessionStorage.getItem(`fullTest_userExamId_${examId}`);
        if (savedUserExamId) {
          console.log('📚 Full Test mode: Reusing existing UserExam ID:', savedUserExamId);
          const response = await getUserExamById(Number(savedUserExamId));
          if (response.code === 200) {
            userExamData = response.result;
            console.log('✅ Reused UserExam:', userExamData);
          }
        }
      }

      // If not Full Test or no saved UserExam, create new one
      if (!userExamData) {
        console.log("📝 Creating new user exam for WRITING...");
        const userExamResponse = await createUserExam(examId, 'WRITING');
        
        if (userExamResponse.code !== 201) {
          throw new Error('Failed to create user exam');
        }
        
        userExamData = userExamResponse.result;
        console.log("✅ User exam created:", userExamData);
      }

      // Store userExam in state for later use
      setUserExam(userExamData);
      console.log("📦 UserExam stored in state:", userExamData);

      // Fetch writing questions
      console.log("📚 Fetching writing questions...");
      const questionsResponse = await getWritingQuestionsByExamId(examId);
      console.log("📊 Questions response:", questionsResponse);
      
      // Backend returns: { code: 200, message: "...", result: [...] }
      if (questionsResponse?.code === 200 && questionsResponse?.result) {
        const fetchedQuestions = questionsResponse.result;
        console.log("✅ Questions fetched:", fetchedQuestions);
        console.log("📝 Question count:", fetchedQuestions.length);
        
        // Filter to ensure only WRITING questions
        const writingQuestions = fetchedQuestions.filter(q => q.skillType === 'WRITING');
        console.log("✍️ Writing questions only:", writingQuestions.length);
        
        if (writingQuestions.length === 0) {
          throw new Error("No writing questions found for this exam");
        }
        
        // Sort questions: task1 first, then task2
        const sortedQuestions = writingQuestions.sort((a, b) => {
          const taskA = a.section === 'task1' ? 1 : a.section === 'task2' ? 2 : 99;
          const taskB = b.section === 'task1' ? 1 : b.section === 'task2' ? 2 : 99;
          return taskA - taskB;
        });
        console.log("✅ Questions sorted (task1 first):", sortedQuestions.map(q => q.section));
        
        setQuestions(sortedQuestions);
        
        // Initialize answers object
        const initialAnswers = {};
        writingQuestions.forEach(q => {
          initialAnswers[q.id] = "";
        });
        setAnswers(initialAnswers);
        console.log("✅ Answers initialized");
      } else {
        console.error("❌ Invalid response format:", questionsResponse);
        throw new Error(questionsResponse?.message || "No writing questions found");
      }

      // Fetch ExamParts to get duration
      console.log("📝 Fetching exam parts for duration...");
      const examPartsResponse = await getExamParts(Number(examId));
      if (examPartsResponse.code === 200) {
        const writingPart = examPartsResponse.result.find(part => part.skillType === 'WRITING');
        console.log('⏱️ Writing ExamPart:', writingPart);
        
        if (writingPart && writingPart.duration) {
          setDuration(writingPart.duration);
          console.log('✅ Duration set to:', writingPart.duration, 'minutes');
        } else {
          console.warn('⚠️ No duration found in ExamPart, using default 60 minutes');
        }
      }

      setLoading(false);
    } catch (err) {
      console.error("❌ Error initializing writing exam:", err);
      console.error("Error details:", {
        message: err.message,
        response: err.response?.data,
        status: err.response?.status
      });
      setError(err.message || "Failed to load writing exam");
      setLoading(false);
    }
  };

  const handleSubmitTest = async () => {
    try {
      setEvaluating(true);
      setTestCompleted(true);
      if (timerRef.current) clearInterval(timerRef.current);

      console.log("📝 Starting Writing exam submission...");
      console.log("UserExam data:", userExam);

      const evaluationResults = {};
      // Calculate actual time spent (in minutes)
      const timeSpentInSeconds = (duration * 60) - (timeRemaining || 0);
      const durationInMinutes = Math.floor(timeSpentInSeconds / 60);

      // Evaluate each task with AI
      console.log("🤖 Evaluating essays with AI...");
      for (const question of questions) {
        const essayText = answers[question.id] || "";
        const wordCount = getWordCount(essayText);
        const taskSection = getTaskType(question);
        
        console.log(`📝 Evaluating Question ${question.id}:`, {
          section: question.section,
          taskType: taskSection,
          wordCount: wordCount,
          title: question.title
        });
        
        if (essayText.trim()) {
          try {
            const result = await evaluateWriting(
              essayText,
              question.id,
              wordCount,
              durationInMinutes
            );
            
            // Java backend returns: { code: 200, result: {...evaluation data...} }
            if (result?.code === 200 && result?.result) {
              evaluationResults[question.id] = result.result;
              console.log(`✅ Question ${question.id} (${taskSection}) evaluated:`, result.result);
              console.log(`📊 Overall band for question ${question.id}:`, result.result.overallBand);
            } else {
              console.error(`Invalid response format for question ${question.id}:`, result);
              console.error(`Response code: ${result?.code}, has result: ${!!result?.result}`);
              evaluationResults[question.id] = {
                error: "Invalid response format"
              };
            }
          } catch (err) {
            console.error(`Error evaluating question ${question.id}:`, err);
            evaluationResults[question.id] = {
              error: "Evaluation failed"
            };
          }
        }
      }

      // Find Writing UserExamPart
      const writingPart = userExam?.parts?.find(p => p.skillType === 'WRITING');
      if (!writingPart) {
        throw new Error("Writing exam part not found");
      }

      console.log("📊 Writing part found:", writingPart);
      
      // Save writingPartId for navigation
      setWritingPartId(writingPart.id);

      // Calculate overall band score (average of all tasks)
      console.log("🔍 All evaluation results:", evaluationResults);
      const validResults = Object.values(evaluationResults).filter(r => {
        if (!r || r.error) return false;
        
        // Check both camelCase and snake_case for compatibility
        const hasOverallBand = r.overallBand !== undefined || r.overall_band !== undefined;
        console.log(`Checking result:`, r, `hasOverallBand: ${hasOverallBand}`);
        return hasOverallBand;
      });
      
      console.log("✅ Valid results count:", validResults.length);
      console.log("📝 Valid results:", validResults);
      
      if (validResults.length === 0) {
        console.error("❌ No valid evaluation results found!");
        console.error("All results:", Object.values(evaluationResults));
        throw new Error("No valid evaluation results");
      }

      // Calculate average, supporting both naming conventions
      const overallBand = validResults.reduce((sum, r) => {
        const band = r.overallBand !== undefined ? r.overallBand : r.overall_band;
        return sum + band;
      }, 0) / validResults.length;
      console.log("🎯 Overall band score:", overallBand);

      // Format taskResults for backend
      const taskResults = questions.map(question => {
        const evalResult = evaluationResults[question.id];
        const essayText = answers[question.id] || "";
        
        if (!evalResult || evalResult.error) {
          return null;
        }

        // Extract scores - support both camelCase and snake_case
        const criteriaScores = evalResult.criteriaScores || evalResult.criteria_scores || {};
        const detailedFeedback = evalResult.detailedFeedback || evalResult.detailed_feedback || {};
        
        console.log(`📊 Question ${question.id} data structure:`, {
          evalResult,
          criteriaScores,
          detailedFeedback
        });
        
        // Format detailed feedback as JSON string if it's an object
        let detailedFeedbackStr = "";
        if (typeof detailedFeedback === 'object' && !Array.isArray(detailedFeedback)) {
          detailedFeedbackStr = JSON.stringify(detailedFeedback);
        }

        // Get examiner feedback
        const examinerFeedback = evalResult.examinerFeedback || evalResult.examiner_feedback || "";

        const taskResult = {
          questionId: question.id,
          essayText: essayText,
          wordCount: getWordCount(essayText),
          duration: durationInMinutes,
          taskAchievement: criteriaScores.taskAchievement || criteriaScores.task_achievement || 0,
          coherenceCohesion: criteriaScores.coherenceCohesion || criteriaScores.coherence_cohesion || 0,
          lexicalResource: criteriaScores.lexicalResource || criteriaScores.lexical_resource || 0,
          grammaticalRange: criteriaScores.grammaticalRange || criteriaScores.grammatical_range_accuracy || criteriaScores.grammatical_range || 0,
          detailedFeedback: detailedFeedbackStr,
          examinerFeedback: examinerFeedback
        };

        console.log(`📤 Question ${question.id} task result:`, taskResult);
        return taskResult;
      }).filter(r => r !== null);

      console.log("📤 Submitting to backend:", {
        userExamPartId: writingPart.id,
        overallBand: overallBand,
        taskResults: taskResults
      });

      // Submit to backend to save to database
      const submitResponse = await submitWritingExam(
        writingPart.id,
        overallBand,
        taskResults
      );

      console.log("✅ Submit response:", submitResponse);

      setEvaluating(false);
      setShowSubmitModal(false);
      
      // Set results to display in UI
      setEvaluationResults(evaluationResults);
      setOverallBandScore(overallBand);
      setShowResults(true);
      
      // Scroll to results section
      setTimeout(() => {
        const resultsElement = document.getElementById('results-section');
        if (resultsElement) {
          resultsElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
      }, 100);
      
    } catch (err) {
      console.error("Error submitting test:", err);
      setEvaluating(false);
      alert("Failed to submit test: " + (err.message || "Unknown error"));
    }
  };

  const handleTimeUp = useCallback(() => {
    if (!testCompleted) {
      handleSubmitTest();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [testCompleted]);

  // Helper to safely extract text from potentially JSON string
  const extractFeedbackText = (value) => {
    if (!value) return "";
    
    // If it's already a plain string, return it
    if (typeof value === 'string' && !value.trim().startsWith('{') && !value.trim().startsWith('[')) {
      return value;
    }
    
    // Try to parse as JSON
    try {
      if (typeof value === 'string') {
        const parsed = JSON.parse(value);
        if (typeof parsed === 'object') {
          return parsed.feedback || parsed.examinerFeedback || parsed.text || '';
        }
        return String(parsed);
      }
      
      if (typeof value === 'object') {
        return value.feedback || value.examinerFeedback || value.text || '';
      }
      
      return String(value);
    } catch (e) {
      return String(value);
    }
  };

  // Initialize timer when duration is loaded
  useEffect(() => {
    if (duration) {
      console.log('⏰ Initializing timer with duration:', duration, 'minutes =', duration * 60, 'seconds');
      setTimeRemaining(duration * 60); // Convert minutes to seconds
    }
  }, [duration]);

  useEffect(() => {
    if (timeRemaining > 0 && !testCompleted) {
      timerRef.current = setInterval(() => {
        setTimeRemaining(prev => {
          if (prev <= 1) {
            handleTimeUp();
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    }
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [timeRemaining, testCompleted, handleTimeUp]);

  const handleAnswerChange = (questionId, text) => {
    setAnswers(prev => ({
      ...prev,
      [questionId]: text
    }));
  };

  const getWordCount = (text) => {
    if (!text) return 0;
    return text.trim().split(/\s+/).filter(w => w.length > 0).length;
  };

  const getCurrentQuestion = () => {
    return questions[currentTaskIndex];
  };

  const handlePreviousTask = () => {
    if (currentTaskIndex > 0) {
      setCurrentTaskIndex(currentTaskIndex - 1);
    }
  };

  const handleNextTask = () => {
    if (currentTaskIndex < questions.length - 1) {
      setCurrentTaskIndex(currentTaskIndex + 1);
    } else {
      setShowSubmitModal(true);
    }
  };

  const showResultsModal = (results) => {
    // Calculate average score from evaluation results
    // Python service returns: overall_band, criteria_scores, detailed_feedback, etc.
    const scores = Object.values(results)
      .filter(r => r && !r.error && r.overallBand !== undefined)
      .map(r => r.overallBand);
    
    const avgScore = scores.length > 0 
      ? (scores.reduce((a, b) => a + b, 0) / scores.length).toFixed(1)
      : 0;
    
    alert(`Test completed!\n\nAverage Band Score: ${avgScore}\n\nCheck console for detailed feedback.`);
    console.log("Evaluation Results:", results);
    
    // Navigate to home or results page
    navigate('/ieltstest');
  };

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const getTaskIcon = (question) => {
    // Dựa vào section từ database, không phải index
    const section = question?.section?.toLowerCase();
    return section === 'task1' ? '📊' : '✍️';
  };

  const getTaskTitle = (question) => {
    // Hiển thị rõ ràng task type từ database
    const section = question?.section?.toLowerCase();
    if (section === 'task1') {
      return 'Writing Task 1: Graph/Chart Description';
    }
    return 'Writing Task 2: Essay/Opinion';
  };

  const getMinWordCount = (question) => {
    // Word count dựa vào section từ database
    const section = question?.section?.toLowerCase();
    return section === 'task1' ? 150 : 250;
  };

  const getTaskType = (question) => {
    // Helper để lấy task type string
    return question?.section?.toLowerCase() || 'task2';
  };

  if (loading) {
    return (
      <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: "100vh" }}>
        <div className="text-center">
          <Spinner animation="border" variant="warning" />
          <p className="mt-3">Loading writing exam...</p>
        </div>
      </Container>
    );
  }

  if (error) {
    return (
      <Container className="mt-5">
        <Alert variant="danger">
          <Alert.Heading>Error Loading Exam</Alert.Heading>
          <p>{error}</p>
          <Button variant="outline-danger" onClick={() => navigate('/ieltstest')}>
            Back to Home
          </Button>
        </Alert>
      </Container>
    );
  }

  if (questions.length === 0) {
    return (
      <Container className="mt-5">
        <Alert variant="warning">
          <Alert.Heading>No Questions Available</Alert.Heading>
          <p>This writing exam has no questions.</p>
          <Button variant="outline-warning" onClick={() => navigate('/ieltstest')}>
            Back to Home
          </Button>
        </Alert>
      </Container>
    );
  }

  const currentQuestion = getCurrentQuestion();
  const currentAnswer = answers[currentQuestion?.id] || "";
  const currentWordCount = getWordCount(currentAnswer);
  const isLastTask = currentTaskIndex === questions.length - 1;

  return (
    <div className="writing-exam-container bg-white min-vh-100">
      <Container fluid className="py-4 bg-white">
        <Container>
          {isFullTest && (
            <Alert variant="success" className="mb-3 text-center">
              <Badge bg="success" className="px-3 py-2 me-2">
                <i className="fe fe-layers me-1"></i>
                FULL TEST MODE
              </Badge>
              <strong>Part 3 of 4: Writing</strong>
            </Alert>
          )}
          <Row className="g-4">
            {/* Left Column - Task Instructions */}
            <Col lg={6}>
              <Card className="border-0 shadow-sm h-100">
                <Card.Body className="p-4">
                  <div className="d-flex align-items-center mb-4">
                    <span className="fs-3 me-2">{getTaskIcon(currentQuestion)}</span>
                    <div className="flex-grow-1">
                      <h4 className="mb-1">{getTaskTitle(currentQuestion)}</h4>
                      <div className="d-flex gap-3 align-items-center">
                        <Badge bg="primary" className="px-3 py-1">
                          <i className="fe fe-clock me-1"></i>
                          {duration} minutes
                        </Badge>
                        <Badge bg="info" className="px-3 py-1">
                          <i className="fe fe-file-text me-1"></i>
                          Min {getMinWordCount(currentQuestion)} words
                        </Badge>
                      </div>
                    </div>
                  </div>

                  {/* Display Image if available (for Task 1 charts/graphs) */}
                  {currentQuestion?.imageUrl && (
                    <div className="mb-4">
                      <Card className="border-0 shadow-sm">
                        <Card.Body className="p-2">
                          <img 
                            src={currentQuestion.imageUrl} 
                            alt="Task 1 Visual" 
                            className="img-fluid rounded"
                            style={{ maxHeight: '400px', width: '100%', objectFit: 'contain' }}
                          />
                        </Card.Body>
                      </Card>
                    </div>
                  )}

                  {/* Task Instructions */}
                  <div className="task-content p-4 rounded" style={{
                    fontSize: '1rem', 
                    lineHeight: '1.8',
                    background: '#f8f9fa',
                    border: '2px solid #e9ecef'
                  }}>
                    {currentQuestion?.content ? (
                      <div dangerouslySetInnerHTML={{ __html: currentQuestion.content }} />
                    ) : (
                      <p className="text-muted mb-0">No task content available</p>
                    )}
                  </div>

                  {/* Task Switcher */}
                  {questions.length > 1 && (
                    <div className="mt-4">
                      <ButtonGroup className="w-100">
                        {questions.map((question, idx) => {
                          // Get actual task number from question.section
                          const taskNum = question.section === 'task1' ? 1 : 
                                         question.section === 'task2' ? 2 : idx + 1;
                          return (
                            <Button
                              key={question.id}
                              size="sm"
                              variant={currentTaskIndex === idx ? 'warning' : 'outline-secondary'}
                              onClick={() => setCurrentTaskIndex(idx)}
                            >
                              Task {taskNum}
                            </Button>
                          );
                        })}
                      </ButtonGroup>
                    </div>
                  )}
                </Card.Body>
              </Card>
            </Col>

            {/* Right Column - Writing Area */}
            <Col lg={6}>
              <Card className="border-0 shadow-sm mb-3">
                <Card.Header className="bg-white border-0 p-4">
                  <div className="d-flex justify-content-between align-items-center">
                    <h5 className="mb-0">Your Answer</h5>
                    <Badge 
                      bg={currentWordCount >= getMinWordCount(currentQuestion) ? 'success' : 'warning'} 
                      className="px-3 py-2 fs-6"
                    >
                      {currentWordCount} words
                    </Badge>
                  </div>
                </Card.Header>
                <Card.Body className="p-4">
                  <textarea
                    className="form-control"
                    rows={15}
                    placeholder={`Write your answer here (minimum ${getMinWordCount(currentQuestion)} words)...`}
                    value={currentAnswer}
                    onChange={(e) => handleAnswerChange(currentQuestion.id, e.target.value)}
                    disabled={testCompleted}
                    style={{
                      fontSize: '1rem',
                      lineHeight: '1.6',
                      resize: 'none'
                    }}
                  />
                </Card.Body>
              </Card>

              {/* Timer and Submit Section */}
              <Card className="border-0 shadow-sm">
                <Card.Body className="p-4">
                  {/* Timer Display */}
                  <div 
                    className="text-center p-4 rounded mb-4"
                    style={{
                      background: timeRemaining < 300 ? '#dc3545' : '#0d6efd'
                    }}
                  >
                    <h6 className="text-white mb-2">
                      <FiClock className="me-2" />
                      Time Remaining
                    </h6>
                    <div className="text-white fw-bold" style={{fontSize: '2rem'}}>
                      {formatTime(timeRemaining)}
                    </div>
                  </div>

                  {/* Navigation Buttons */}
                  <Row className="g-2">
                    <Col>
                      <Button 
                        variant="outline-secondary"
                        className="w-100"
                        onClick={handlePreviousTask}
                        disabled={currentTaskIndex === 0 || testCompleted}
                      >
                        <FiArrowLeft className="me-2" />
                        Previous Task
                      </Button>
                    </Col>
                    <Col>
                      {isLastTask ? (
                        <Button 
                          variant="success"
                          size="lg"
                          className="w-100 py-2"
                          onClick={() => setShowSubmitModal(true)}
                          disabled={testCompleted || evaluating}
                        >
                          {evaluating ? (
                            <>
                              <Spinner animation="border" size="sm" className="me-2" />
                              Evaluating...
                            </>
                          ) : (
                            <>
                              <FiCheckCircle className="me-2" />
                              Submit Test
                            </>
                          )}
                        </Button>
                      ) : (
                        <Button 
                          variant="warning"
                          className="w-100"
                          onClick={handleNextTask}
                          disabled={testCompleted}
                        >
                          Next Task
                          <FiArrowRight className="ms-2" />
                        </Button>
                      )}
                    </Col>
                  </Row>
                </Card.Body>
              </Card>
            </Col>
          </Row>

          {/* Results Section - Show after evaluation */}
          {showResults && (
            <Row className="mt-4" id="results-section">
              <Col>
                <Card className="border-0 shadow-lg">
                  <Card.Header className="bg-success text-white py-3">
                    <h3 className="mb-0">
                      <FiCheckCircle className="me-2" />
                      Test Completed - Your Results
                    </h3>
                  </Card.Header>
                  <Card.Body className="p-4">
                    {/* Overall Score */}
                    <Row className="mb-4">
                      <Col className="text-center">
                        <h4 className="text-muted mb-3">Overall Band Score</h4>
                        <div 
                          className="display-1 fw-bold mb-3"
                          style={{ 
                            fontSize: '5rem',
                            color: overallBandScore >= 7 ? '#28a745' : overallBandScore >= 5.5 ? '#ffc107' : '#dc3545'
                          }}
                        >
                          {overallBandScore.toFixed(1)}
                        </div>
                        <Badge 
                          bg={overallBandScore >= 7 ? 'success' : overallBandScore >= 5.5 ? 'warning' : 'danger'}
                          className="fs-4 px-4 py-2"
                        >
                          {overallBandScore >= 8 ? 'Excellent' : 
                           overallBandScore >= 7 ? 'Good' :
                           overallBandScore >= 6 ? 'Competent' :
                           overallBandScore >= 5 ? 'Modest' : 'Needs Improvement'}
                        </Badge>
                      </Col>
                    </Row>

                    {/* Individual Task Results */}
                    {questions.map((question, index) => {
                      const evalResult = evaluationResults[question.id];
                      if (!evalResult || evalResult.error) return null;

                      const criteriaScores = evalResult.criteriaScores || evalResult.criteria_scores || {};
                      const detailedFeedback = evalResult.detailedFeedback || evalResult.detailed_feedback || {};
                      const examinerFeedback = extractFeedbackText(evalResult.examinerFeedback || evalResult.examiner_feedback || "");

                      const criteria = [
                        { 
                          name: "Task Achievement", 
                          score: criteriaScores.taskAchievement || criteriaScores.task_achievement || 0,
                          icon: "📝",
                          feedback: extractFeedbackText(detailedFeedback.taskAchievement || detailedFeedback.task_achievement || "")
                        },
                        { 
                          name: "Coherence & Cohesion", 
                          score: criteriaScores.coherenceCohesion || criteriaScores.coherence_cohesion || 0,
                          icon: "🔗",
                          feedback: extractFeedbackText(detailedFeedback.coherenceCohesion || detailedFeedback.coherence_cohesion || "")
                        },
                        { 
                          name: "Lexical Resource", 
                          score: criteriaScores.lexicalResource || criteriaScores.lexical_resource || 0,
                          icon: "📚",
                          feedback: extractFeedbackText(detailedFeedback.lexicalResource || detailedFeedback.lexical_resource || "")
                        },
                        { 
                          name: "Grammatical Range", 
                          score: criteriaScores.grammaticalRange || criteriaScores.grammatical_range_accuracy || criteriaScores.grammatical_range || 0,
                          icon: "✍️",
                          feedback: extractFeedbackText(detailedFeedback.grammaticalRange || detailedFeedback.grammatical_range_accuracy || detailedFeedback.grammatical_range || "")
                        }
                      ];

                      return (
                        <Card key={question.id} className="mb-4 border">
                          <Card.Header className="bg-primary text-white">
                            <h5 className="mb-0">
                              {getTaskIcon(question)} {getTaskTitle(question)} - Detailed Scores
                            </h5>
                          </Card.Header>
                          <Card.Body>
                            {/* Criteria Scores Grid */}
                            <Row className="mb-4">
                              {criteria.map((criterion, idx) => (
                                <Col key={idx} md={6} className="mb-3">
                                  <div className="p-3 rounded" style={{ background: '#f8f9fa' }}>
                                    <div className="d-flex justify-content-between align-items-center mb-2">
                                      <span className="fw-bold">
                                        <span className="me-2">{criterion.icon}</span>
                                        {criterion.name}
                                      </span>
                                      <Badge 
                                        bg={criterion.score >= 7 ? 'success' : criterion.score >= 5.5 ? 'warning' : 'danger'}
                                        className="fs-5"
                                      >
                                        {criterion.score.toFixed(1)}
                                      </Badge>
                                    </div>
                                    <div className="progress" style={{ height: '10px' }}>
                                      <div 
                                        className={`progress-bar bg-${criterion.score >= 7 ? 'success' : criterion.score >= 5.5 ? 'warning' : 'danger'}`}
                                        style={{ width: `${(criterion.score / 9) * 100}%` }}
                                      />
                                    </div>
                                  </div>
                                </Col>
                              ))}
                            </Row>

                            {/* Examiner Feedback */}
                            {examinerFeedback && (
                              <Alert variant="info" className="mb-3">
                                <Alert.Heading className="h6">
                                  <FiCheckCircle className="me-2" />
                                  Examiner's Overall Feedback
                                </Alert.Heading>
                                <p className="mb-0" style={{ whiteSpace: 'pre-wrap', fontSize: '0.95rem' }}>
                                  {examinerFeedback}
                                </p>
                              </Alert>
                            )}

                            {/* Detailed Criteria Feedback */}
                            <h6 className="mb-3 mt-4">Detailed Feedback by Criteria</h6>
                            {criteria.map((criterion, idx) => (
                              criterion.feedback && (
                                <Card key={idx} className="mb-3 border">
                                  <Card.Header className="bg-light">
                                    <span className="me-2">{criterion.icon}</span>
                                    <strong>{criterion.name}</strong>
                                    <Badge 
                                      bg={criterion.score >= 7 ? 'success' : criterion.score >= 5.5 ? 'warning' : 'danger'} 
                                      className="ms-2"
                                    >
                                      {criterion.score.toFixed(1)}
                                    </Badge>
                                  </Card.Header>
                                  <Card.Body>
                                    <p className="mb-0" style={{ whiteSpace: 'pre-wrap', fontSize: '0.95rem' }}>
                                      {criterion.feedback}
                                    </p>
                                  </Card.Body>
                                </Card>
                              )
                            ))}
                          </Card.Body>
                        </Card>
                      );
                    })}

                    {/* Action Buttons */}
                    <Row className="mt-4">
                      <Col className="text-center">
                        {isFullTest ? (
                          <Button 
                            variant="success" 
                            size="lg" 
                            onClick={() => navigate(`/ieltstest/attempt/speaking/${examId}?fullTest=true`)}
                            className="px-5"
                          >
                            Continue to Speaking
                            <FiArrowRight className="ms-2" />
                          </Button>
                        ) : (
                          <>
                            <Button 
                              variant="primary" 
                              size="lg" 
                              onClick={() => navigate('/ieltstest')}
                              className="me-3"
                            >
                              <FiArrowLeft className="me-2" />
                              Back to Tests
                            </Button>
                            <Button 
                              variant="outline-success" 
                              size="lg"
                              onClick={() => {
                                navigate(`/ieltstest/writing/result/${writingPartId}`, {
                                  state: {
                                    userExamPartId: writingPartId,
                                    results: {
                                      overallBand: overallBandScore,
                                      taskResults: questions.map(q => ({
                                        questionId: q.id,
                                        essayText: answers[q.id] || "",
                                        wordCount: getWordCount(answers[q.id] || "")
                                      })),
                                      evaluationResults: evaluationResults
                                    }
                                  }
                                });
                              }}
                            >
                              View Detailed Results
                              <FiArrowRight className="ms-2" />
                            </Button>
                          </>
                        )}
                      </Col>
                    </Row>
                  </Card.Body>
                </Card>
              </Col>
            </Row>
          )}
        </Container>
      </Container>

      {/* Submit Confirmation Modal */}
      <Modal show={showSubmitModal} onHide={() => setShowSubmitModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Nộp bài thi Writing</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p>Bạn đã hoàn thành:</p>
          <ul>
            {questions.map((question, idx) => {
              const wordCount = getWordCount(answers[question.id] || "");
              const minWords = getMinWordCount(question);
              const taskSection = getTaskType(question);
              return (
                <li key={question.id}>
                  {getTaskTitle(question)}: <strong>{wordCount} words</strong>
                  <Badge bg="info" className="ms-2">
                    {taskSection}
                  </Badge>
                  {wordCount < minWords && (
                    <Badge bg="warning" className="ms-2">
                      Need {minWords - wordCount} more
                    </Badge>
                  )}
                </li>
              );
            })}
          </ul>
          <p className="mb-0">Bài làm sẽ được chấm bởi AI. Bạn có chắc chắn muốn nộp bài?</p>
        </Modal.Body>
        <Modal.Footer className="p-2">
          <Button variant="outline-primary" onClick={() => setShowSubmitModal(false)} disabled={evaluating}>
            Hủy
          </Button>
          <Button variant="success" onClick={handleSubmitTest} disabled={evaluating}>
            {evaluating ? (
              <>
                <Spinner animation="border" size="sm" className="me-2" />
                Đang chấm bài...
              </>
            ) : (
              'Nộp bài'
            )}
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
};

export default AttemptWritingModulePage;
