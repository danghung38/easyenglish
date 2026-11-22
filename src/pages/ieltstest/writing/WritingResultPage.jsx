import { useEffect, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { Container, Row, Col, Card, Button, Badge, ProgressBar, Spinner, Alert } from "react-bootstrap";
import { FiArrowLeft, FiCheckCircle, FiStar } from "react-icons/fi";
import { getUserAnswers, getQuestionsByExamPart } from "../../../services/examService";

const WritingResultPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { examId, userExamPartId: urlUserExamPartId } = useParams();
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadResults = async () => {
      try {
        // Priority: URL params > location.state
        const partId = urlUserExamPartId || location.state?.userExamPartId;
        
        console.log('📍 URL userExamPartId:', urlUserExamPartId);
        console.log('📍 Location state:', location.state);
        console.log('📍 Final userExamPartId:', partId);
        
        // Case 1: Results passed from submit (has full evaluation data)
        if (location.state?.results) {
          setResults(location.state.results);
          setLoading(false);
          return;
        }

        // Case 2: Coming from history or direct URL (has userExamPartId)
        if (partId) {
          const userExamPartId = partId;
          const score = location.state?.score;

          console.log("Loading Writing results from backend for userExamPartId:", userExamPartId);

          // Fetch user answers (contains essay text and AI feedback)
          const answersResponse = await getUserAnswers(userExamPartId);
          
          if (answersResponse?.code === 200 && answersResponse?.result) {
            const userAnswers = answersResponse.result;
            console.log("User answers:", userAnswers);

            // Parse evaluation results from userAnswers
            const evaluationResults = {};
            const taskResults = [];
            const allCriteriaScores = [];

            for (const answer of userAnswers) {
              try {
                // Parse aiFeedback JSON (contains criteria scores and feedback)
                const feedbackRaw = answer.aiFeedback;
                const feedback = feedbackRaw ? (typeof feedbackRaw === 'string' ? JSON.parse(feedbackRaw) : feedbackRaw) : {};
                
                // Extract criteria scores (try different field names)
                const criteriaScores = feedback.criteria_scores || feedback.criteriaScores || {};
                const taskAchievement = criteriaScores.task_achievement || criteriaScores.taskAchievement || feedback.taskAchievement || 0;
                const coherenceCohesion = criteriaScores.coherence_cohesion || criteriaScores.coherenceCohesion || feedback.coherenceCohesion || 0;
                const lexicalResource = criteriaScores.lexical_resource || criteriaScores.lexicalResource || feedback.lexicalResource || 0;
                const grammaticalRange = criteriaScores.grammatical_range_accuracy || criteriaScores.grammatical_range || criteriaScores.grammaticalRange || feedback.grammaticalRange || 0;
                
                // Calculate task score: Priority aiScore, then average of criteria
                let taskScore = 0;
                if (answer.aiScore) {
                  taskScore = parseFloat(answer.aiScore);
                } else if (taskAchievement > 0 && coherenceCohesion > 0 && lexicalResource > 0 && grammaticalRange > 0) {
                  taskScore = (taskAchievement + coherenceCohesion + lexicalResource + grammaticalRange) / 4;
                  taskScore = Math.round(taskScore * 2) / 2; // Round to 0.5
                }
                
                evaluationResults[answer.questionId] = {
                  overallBand: taskScore,
                  criteriaScores: {
                    taskAchievement: taskAchievement,
                    coherenceCohesion: coherenceCohesion,
                    lexicalResource: lexicalResource,
                    grammaticalRange: grammaticalRange
                  },
                  detailedFeedback: feedback.detailedFeedback || feedback.detailed_feedback || {
                    taskAchievement: "",
                    coherenceCohesion: "",
                    lexicalResource: "",
                    grammaticalRange: ""
                  },
                  examinerFeedback: feedback.examinerFeedback || feedback.examiner_feedback || ""
                };

                // Collect criteria scores for overall calculation
                if (taskAchievement > 0 && coherenceCohesion > 0 && lexicalResource > 0 && grammaticalRange > 0) {
                  allCriteriaScores.push([taskAchievement, coherenceCohesion, lexicalResource, grammaticalRange]);
                }

                taskResults.push({
                  questionId: answer.questionId,
                  essayText: answer.answerText || "",
                  wordCount: answer.answerText ? answer.answerText.trim().split(/\s+/).length : 0,
                  duration: 0 // Not stored in UserAnswer
                });
              } catch (err) {
                console.error("Error parsing answer feedback:", err);
              }
            }

            // Calculate overall band: average of all criteria scores
            let calculatedOverallBand = 0;
            if (allCriteriaScores.length > 0) {
              const totalCriteria = allCriteriaScores.flat();
              calculatedOverallBand = totalCriteria.reduce((a, b) => a + b, 0) / totalCriteria.length;
              calculatedOverallBand = Math.round(calculatedOverallBand * 2) / 2; // Round to 0.5
            } else if (score) {
              calculatedOverallBand = parseFloat(score);
            }

            setResults({
              overallBand: calculatedOverallBand,
              taskResults: taskResults,
              evaluationResults: evaluationResults
            });
          } else {
            throw new Error("Failed to load user answers");
          }

          setLoading(false);
          return;
        }

        // Case 3: No data - show error instead of redirect
        setError('No exam data found. Please take the test first.');
        setLoading(false);
      } catch (err) {
        console.error("Error loading results:", err);
        setError(err.message || "Failed to load results");
        setLoading(false);
      }
    };

    loadResults();
  }, [urlUserExamPartId, location.state, navigate, examId]);

  if (loading) {
    return (
      <Container className="py-5">
        <div className="text-center">
          <Spinner animation="border" variant="primary" />
          <p className="mt-3">Loading results...</p>
        </div>
      </Container>
    );
  }

  if (error) {
    return (
      <Container className="py-5">
        <Alert variant="danger">
          <Alert.Heading>Error Loading Results</Alert.Heading>
          <p>{error}</p>
          <Button variant="outline-danger" onClick={() => navigate('/ieltstest/listening')}>
            Back to Tests
          </Button>
        </Alert>
      </Container>
    );
  }

  if (!results) {
    return (
      <Container className="py-5">
        <Alert variant="warning">
          <Alert.Heading>No Results Found</Alert.Heading>
          <p>No results data available.</p>
          <Button variant="outline-warning" onClick={() => navigate('/ieltstest/listening')}>
            Back to Tests
          </Button>
        </Alert>
      </Container>
    );
  }

  const { overallBand, taskResults, evaluationResults } = results;

  const getBandColor = (band) => {
    if (band >= 7) return "success";
    if (band >= 5.5) return "warning";
    return "danger";
  };

  const getBandLabel = (band) => {
    if (band >= 8) return "Excellent";
    if (band >= 7) return "Good";
    if (band >= 6) return "Competent";
    if (band >= 5) return "Modest";
    return "Needs Improvement";
  };

  // Helper to safely extract text from potentially JSON string
  const extractText = (value) => {
    if (!value) return "";
    
    // If it's already a string and not JSON, return it
    if (typeof value === 'string' && !value.trim().startsWith('{') && !value.trim().startsWith('[')) {
      return value;
    }
    
    // Try to parse as JSON
    try {
      if (typeof value === 'string') {
        const parsed = JSON.parse(value);
        
        // Extract relevant text fields from parsed object
        if (typeof parsed === 'object') {
          return parsed.feedback || parsed.examinerFeedback || parsed.text || JSON.stringify(parsed);
        }
        return String(parsed);
      }
      
      // If it's already an object
      if (typeof value === 'object') {
        return value.feedback || value.examinerFeedback || value.text || JSON.stringify(value);
      }
      
      return String(value);
    } catch (e) {
      // If parsing fails, return as is
      return String(value);
    }
  };

  return (
    <Container className="py-4">
      {/* Header */}
      <Row className="mb-4">
        <Col>
          <Button 
            variant="outline-secondary" 
            onClick={() => navigate('/ieltstest/listening')}
            className="mb-3"
          >
            <FiArrowLeft className="me-2" />
            Back to Tests
          </Button>
          <h2 className="mb-0">
            <FiCheckCircle className="text-success me-2" />
            Writing Test Results
          </h2>
        </Col>
      </Row>

      {/* Overall Score Card */}
      <Card className="mb-4 shadow-sm border-0">
        <Card.Body className="p-4">
          <Row className="align-items-center">
            <Col md={6} className="text-center text-md-start mb-3 mb-md-0">
              <h3 className="text-muted mb-2">Overall Band Score</h3>
              <div className="d-flex align-items-center justify-content-center justify-content-md-start">
                <div 
                  className={`display-1 fw-bold text-${getBandColor(overallBand)} me-3`}
                  style={{ fontSize: '4rem' }}
                >
                  {overallBand.toFixed(1)}
                </div>
                <div>
                  <Badge 
                    bg={getBandColor(overallBand)} 
                    className="fs-5 px-3 py-2"
                  >
                    {getBandLabel(overallBand)}
                  </Badge>
                </div>
              </div>
            </Col>
            <Col md={6}>
              <div className="mb-3">
                <div className="d-flex justify-content-between mb-1">
                  <span>Band Score Progress</span>
                  <span className="fw-bold">{overallBand.toFixed(1)} / 9.0</span>
                </div>
                <ProgressBar 
                  now={(overallBand / 9) * 100} 
                  variant={getBandColor(overallBand)}
                  style={{ height: '20px' }}
                />
              </div>
              <p className="text-muted mb-0">
                <FiStar className="text-warning me-1" />
                Congratulations! Your test has been evaluated and saved.
              </p>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Individual Task Results */}
      {taskResults && taskResults.map((taskResult, index) => {
        const evalResult = evaluationResults?.[taskResult.questionId];
        if (!evalResult || evalResult.error) return null;

        // Get criteria scores - support both naming conventions
        const criteriaScores = evalResult.criteriaScores || evalResult.criteria_scores || {};
        const detailedFeedback = evalResult.detailedFeedback || evalResult.detailed_feedback || {};
        const examinerFeedback = extractText(evalResult.examinerFeedback || evalResult.examiner_feedback || "");

        const criteria = [
          { 
            name: "Task Achievement", 
            score: criteriaScores.taskAchievement || criteriaScores.task_achievement || 0,
            feedback: extractText(detailedFeedback.taskAchievement || detailedFeedback.task_achievement || "")
          },
          { 
            name: "Coherence & Cohesion", 
            score: criteriaScores.coherenceCohesion || criteriaScores.coherence_cohesion || 0,
            feedback: extractText(detailedFeedback.coherenceCohesion || detailedFeedback.coherence_cohesion || "")
          },
          { 
            name: "Lexical Resource", 
            score: criteriaScores.lexicalResource || criteriaScores.lexical_resource || 0,
            feedback: extractText(detailedFeedback.lexicalResource || detailedFeedback.lexical_resource || "")
          },
          { 
            name: "Grammatical Range", 
            score: criteriaScores.grammaticalRange || criteriaScores.grammatical_range_accuracy || criteriaScores.grammatical_range || 0,
            feedback: extractText(detailedFeedback.grammaticalRange || detailedFeedback.grammatical_range_accuracy || detailedFeedback.grammatical_range || "")
          }
        ];

        return (
          <Card key={taskResult.questionId} className="mb-4 shadow-sm border-0">
            <Card.Header className="bg-primary text-white py-3">
              <h4 className="mb-0">
                Task {index + 1} Results
                <Badge bg="light" text="dark" className="ms-3">
                  {taskResult.wordCount} words
                </Badge>
                <Badge bg="light" text="dark" className="ms-2">
                  {taskResult.duration} min
                </Badge>
              </h4>
            </Card.Header>
            <Card.Body className="p-4">
              {/* Criteria Scores */}
              <Row className="mb-4">
                {criteria.map((criterion, idx) => (
                  <Col key={idx} md={6} className="mb-3">
                    <div className="d-flex justify-content-between align-items-center mb-2">
                      <h6 className="mb-0">{criterion.name}</h6>
                      <Badge 
                        bg={getBandColor(criterion.score)} 
                        className="fs-6"
                      >
                        {criterion.score.toFixed(1)}
                      </Badge>
                    </div>
                    <ProgressBar 
                      now={(criterion.score / 9) * 100} 
                      variant={getBandColor(criterion.score)}
                      style={{ height: '8px' }}
                    />
                  </Col>
                ))}
              </Row>

              {/* Examiner Feedback */}
              {examinerFeedback && (
                <div className="mb-4">
                  <h5 className="mb-3">
                    <FiCheckCircle className="text-success me-2" />
                    Examiner's Overall Feedback
                  </h5>
                  <Card className="bg-light border-0">
                    <Card.Body>
                      <p className="mb-0" style={{ whiteSpace: 'pre-wrap' }}>
                        {examinerFeedback}
                      </p>
                    </Card.Body>
                  </Card>
                </div>
              )}

              {/* Detailed Criteria Feedback */}
              <h5 className="mb-3">Detailed Feedback by Criteria</h5>
              {criteria.map((criterion, idx) => (
                criterion.feedback && (
                  <Card key={idx} className="mb-3 border">
                    <Card.Header className="bg-light">
                      <strong>{criterion.name}</strong>
                      <Badge bg={getBandColor(criterion.score)} className="ms-2">
                        {criterion.score.toFixed(1)}
                      </Badge>
                    </Card.Header>
                    <Card.Body>
                      <p className="mb-0" style={{ whiteSpace: 'pre-wrap' }}>
                        {criterion.feedback}
                      </p>
                    </Card.Body>
                  </Card>
                )
              ))}

              {/* Essay Text */}
              <div className="mt-4">
                <h5 className="mb-3">Your Essay</h5>
                <Card className="bg-light border-0">
                  <Card.Body>
                    <p style={{ whiteSpace: 'pre-wrap', lineHeight: '1.8' }}>
                      {taskResult.essayText}
                    </p>
                  </Card.Body>
                </Card>
              </div>
            </Card.Body>
          </Card>
        );
      })}

      {/* Action Buttons */}
      <Row className="mt-4">
        <Col className="text-center">
          <Button 
            variant="primary" 
            size="lg" 
            onClick={() => navigate('/ieltstest/listening')}
            className="me-3"
          >
            Back to Tests
          </Button>
          <Button 
            variant="outline-secondary" 
            size="lg"
            onClick={() => window.print()}
          >
            Print Results
          </Button>
        </Col>
      </Row>
    </Container>
  );
};

export default WritingResultPage;
