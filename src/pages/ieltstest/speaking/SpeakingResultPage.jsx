/**
 * SpeakingResultPage - Display Speaking test results
 * 
 * Shows overall band score, part scores, criteria scores, and AI feedback
 */

import React, { useEffect, useState } from "react";
import { useParams, useLocation, useNavigate } from "react-router-dom";
import { 
  Card, 
  Col, 
  Container, 
  Row, 
  Spinner, 
  Alert, 
  Button, 
  Badge,
  ProgressBar,
  Accordion
} from "react-bootstrap";
import { getUserAnswers } from "../../../services/examService";
import '../../admin/QuillCustom.css';

const SpeakingResultPage = () => {
  const { examId, userExamPartId: urlUserExamPartId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [userAnswers, setUserAnswers] = useState([]);
  const [overallScore, setOverallScore] = useState(null);
  const [userExamPartId, setUserExamPartId] = useState(null);
  const [resultData, setResultData] = useState(null);

  useEffect(() => {
    document.title = "IELTS Speaking Test Result | KeenIELTS";
    
    // Priority: URL params > location.state
    const partId = urlUserExamPartId || location.state?.userExamPartId;
    
    console.log('📍 URL userExamPartId:', urlUserExamPartId);
    console.log('📍 Location state:', location.state);
    console.log('📍 Final userExamPartId:', partId);
    
    if (partId) {
      setUserExamPartId(partId);
      setOverallScore(location.state?.overallBand || location.state?.score || null);
      setResultData(location.state?.result || null);
      fetchResults(partId);
    } else {
      setError('No exam data found. Please take the test first.');
      setLoading(false);
    }
  }, [urlUserExamPartId, location.state]);

  const fetchResults = async (userExamPartId) => {
    try {
      setLoading(true);
      console.log('📊 Fetching Speaking results for userExamPartId:', userExamPartId);
      
      const answersResponse = await getUserAnswers(userExamPartId);
      
      if (answersResponse.code === 200) {
        const answers = answersResponse.result || [];
        console.log('✅ User answers loaded:', answers.length, 'answers');
        console.log('📋 Sample answer structure:', answers[0]);
        
        // Group answers by part (section)
        const groupedAnswers = groupAnswersByPart(answers);
        setUserAnswers(groupedAnswers);
      } else {
        throw new Error(answersResponse.message || 'Failed to fetch results');
      }
      
      setLoading(false);
    } catch (err) {
      console.error('❌ Error fetching results:', err);
      setError(err.message || 'Failed to load results');
      setLoading(false);
    }
  };

  const groupAnswersByPart = (answers) => {
    const grouped = {
      part1: [],
      part2: [],
      part3: []
    };

    answers.forEach(answer => {
      const section = answer.question?.section;
      if (!section) {
        console.warn('⚠️ Answer without section:', answer);
        return;
      }
      
      // Normalize section name - remove spaces and lowercase
      const sectionNormalized = section.toLowerCase().replace(/\s+/g, '');
      console.log('📍 Section:', section, '→', sectionNormalized);
      
      // Match part1, part2, part3 (case insensitive, with or without spaces)
      if (sectionNormalized.includes('part1') || sectionNormalized === 'part1') {
        grouped.part1.push(answer);
      } else if (sectionNormalized.includes('part2') || sectionNormalized === 'part2') {
        grouped.part2.push(answer);
      } else if (sectionNormalized.includes('part3') || sectionNormalized === 'part3') {
        grouped.part3.push(answer);
      } else {
        console.warn('⚠️ Unknown section:', section, 'for answer:', answer.id);
      }
    });

    console.log('📊 Grouped answers:', {
      part1: grouped.part1.length,
      part2: grouped.part2.length,
      part3: grouped.part3.length
    });

    return grouped;
  };

  const calculatePartStats = (partAnswers) => {
    if (!partAnswers || partAnswers.length === 0) {
      console.log('⚠️ No answers to calculate');
      return null;
    }
    
    console.log('🔍 Calculating stats for', partAnswers.length, 'answers');
    
    // Get average score from all answers in this part
    const scores = partAnswers
      .map((answer, idx) => {
        // Try different score sources
        let score = null;
        
        if (answer.score !== null && answer.score !== undefined) {
          score = answer.score;
          console.log(`  Answer ${idx + 1}: score = ${score}`);
        } else if (answer.aiScore !== null && answer.aiScore !== undefined) {
          score = answer.aiScore;
          console.log(`  Answer ${idx + 1}: aiScore = ${score}`);
        } else {
          // Try to get from aiFeedback
          const feedback = parseFeedback(answer);
          if (feedback?.overall_band !== null && feedback?.overall_band !== undefined) {
            score = feedback.overall_band;
            console.log(`  Answer ${idx + 1}: overall_band from feedback = ${score}`);
          } else if (feedback?.criteria_scores) {
            // Calculate average from criteria
            const criteriaValues = Object.values(feedback.criteria_scores)
              .filter(v => v !== null && v !== undefined);
            if (criteriaValues.length > 0) {
              score = criteriaValues.reduce((sum, s) => sum + s, 0) / criteriaValues.length;
              console.log(`  Answer ${idx + 1}: calculated from criteria = ${score}`);
            }
          }
        }
        
        if (score === null) {
          console.warn(`  Answer ${idx + 1}: No score found`, answer);
        }
        
        return score;
      })
      .filter(s => s !== null && s !== undefined);
    
    console.log('✅ Valid scores:', scores);
    
    if (scores.length === 0) return null;
    
    const avgScore = scores.reduce((sum, s) => sum + s, 0) / scores.length;
    const rounded = Math.round(avgScore * 2) / 2;
    console.log('📊 Average score:', avgScore, '→ Rounded:', rounded);
    return rounded;
  };

  const parseFeedback = (answer) => {
    try {
      if (!answer.aiFeedback) return null;
      
      // If it's already an object, return it
      if (typeof answer.aiFeedback === 'object') {
        return answer.aiFeedback;
      }
      
      // Try to parse JSON string
      return JSON.parse(answer.aiFeedback);
    } catch (e) {
      console.warn('Failed to parse feedback:', e);
      return null;
    }
  };

  const getCriteriaScores = () => {
    if (resultData?.criteria_scores) {
      return resultData.criteria_scores;
    }

    // Calculate from user answers THEO TRỌNG SỐ: Part1 20%, Part2 40%, Part3 40%
    const part1Feedbacks = (userAnswers.part1 || []).map(a => parseFeedback(a)).filter(f => f?.criteria_scores);
    const part2Feedbacks = (userAnswers.part2 || []).map(a => parseFeedback(a)).filter(f => f?.criteria_scores);
    const part3Feedbacks = (userAnswers.part3 || []).map(a => parseFeedback(a)).filter(f => f?.criteria_scores);

    const criteria = ['fluency_coherence', 'lexical_resource', 'grammatical_range_accuracy', 'pronunciation'];
    const avgCriteria = {};

    criteria.forEach(criterion => {
      // Tính trung bình cho từng part
      const getPartAvg = (feedbacks) => {
        const scores = feedbacks.map(f => f.criteria_scores?.[criterion]).filter(s => s !== null && s !== undefined);
        return scores.length > 0 ? scores.reduce((sum, s) => sum + s, 0) / scores.length : 0;
      };

      const part1Avg = getPartAvg(part1Feedbacks);
      const part2Avg = getPartAvg(part2Feedbacks);
      const part3Avg = getPartAvg(part3Feedbacks);

      // Kiểm tra có đủ 3 parts không
      const hasPart1 = part1Feedbacks.length > 0;
      const hasPart2 = part2Feedbacks.length > 0;
      const hasPart3 = part3Feedbacks.length > 0;
      const totalParts = [hasPart1, hasPart2, hasPart3].filter(Boolean).length;

      if (totalParts === 0) return;

      let weighted;
      if (totalParts === 3) {
        // Full test: Áp dụng trọng số
        weighted = part1Avg * 0.2 + part2Avg * 0.4 + part3Avg * 0.4;
      } else {
        // Không đủ 3 parts: Tính trung bình thông thường
        const sum = (hasPart1 ? part1Avg : 0) + (hasPart2 ? part2Avg : 0) + (hasPart3 ? part3Avg : 0);
        weighted = sum / totalParts;
      }

      avgCriteria[criterion] = Math.round(weighted * 2) / 2;
    });

    return Object.keys(avgCriteria).length > 0 ? avgCriteria : null;
  };

  const getPartScores = () => {
    if (resultData?.part_scores) {
      return resultData.part_scores;
    }

    return {
      part1: calculatePartStats(userAnswers.part1),
      part2: calculatePartStats(userAnswers.part2),
      part3: calculatePartStats(userAnswers.part3)
    };
  };

  const criteriaLabels = {
    fluency_coherence: 'Fluency & Coherence',
    lexical_resource: 'Lexical Resource',
    grammatical_range_accuracy: 'Grammatical Range & Accuracy',
    pronunciation: 'Pronunciation'
  };

  const criteriaColors = {
    fluency_coherence: 'primary',
    lexical_resource: 'success',
    grammatical_range_accuracy: 'warning',
    pronunciation: 'info'
  };

  if (loading) {
    return (
      <Container className="my-5 text-center">
        <Spinner animation="border" role="status">
          <span className="visually-hidden">Loading results...</span>
        </Spinner>
        <p className="mt-3">Loading Speaking results...</p>
      </Container>
    );
  }

  if (error) {
    return (
      <Container className="my-5">
        <Alert variant="danger">
          <Alert.Heading>Error Loading Results</Alert.Heading>
          <p>{error}</p>
          <Button variant="outline-danger" onClick={() => navigate('/ieltstest/speaking')}>
            Back to Speaking Tests
          </Button>
        </Alert>
      </Container>
    );
  }

  const criteriaScores = getCriteriaScores();
  const partScores = getPartScores();
  const totalAnswers = Object.values(userAnswers).flat().length;

  // Tính overall band: Ưu tiên từ 4 criteria scores
  const calculateOverallBand = () => {
    // Nếu có đủ 4 criteria scores, tính trung bình
    if (criteriaScores && Object.keys(criteriaScores).length === 4) {
      const sum = Object.values(criteriaScores).reduce((total, score) => total + score, 0);
      const avg = sum / 4;
      return Math.round(avg * 2) / 2; // Làm tròn về 0.5
    }
    
    // Fallback: Dùng overallScore từ state hoặc tính từ userAnswers
    if (overallScore !== null && overallScore !== undefined) {
      return overallScore;
    }
    
    const calculated = calculatePartStats(Object.values(userAnswers).flat());
    return calculated !== null ? calculated : 'N/A';
  };

  const displayOverallBand = calculateOverallBand();

  return (
    <div className="mt-4 mb-4">
      <Container>
        <Row className="justify-content-center">
          <Col xl={10} lg={12} md={12}>
            {/* Overall Score Card */}
            <Card className="mb-4 border-0 shadow">
              <Card.Header className="bg-success text-white py-3">
                <h3 className="mb-0">🎉 IELTS Speaking Test Result</h3>
              </Card.Header>
              <Card.Body className="p-4">
                <Row className="align-items-center">
                  <Col md={4} className="text-center border-end">
                    <h1 className="display-1 text-success mb-0">
                      {displayOverallBand}
                    </h1>
                    <h5 className="text-muted">Overall Band Score</h5>
                    <p className="text-muted small mb-0">
                      Based on {totalAnswers} responses
                      {criteriaScores && Object.keys(criteriaScores).length === 4 && (
                        <><br /><small className="text-primary">(Averaged from 4 criteria)</small></>
                      )}
                    </p>
                  </Col>
                  <Col md={8}>
                    <h5 className="mb-3">Part Scores</h5>
                    <Row className="g-3">
                      <Col md={4}>
                        <div className="text-center p-3 border rounded">
                          <h6 className="text-muted mb-2">Part 1</h6>
                          <h3 className={`mb-0 ${partScores.part1 !== null && partScores.part1 !== undefined ? 'text-primary' : 'text-muted'}`}>
                            {partScores.part1 !== null && partScores.part1 !== undefined ? partScores.part1 : 'N/A'}
                          </h3>
                          <small className="text-muted">
                            {userAnswers.part1?.length || 0} questions
                          </small>
                        </div>
                      </Col>
                      <Col md={4}>
                        <div className="text-center p-3 border rounded">
                          <h6 className="text-muted mb-2">Part 2</h6>
                          <h3 className={`mb-0 ${partScores.part2 !== null && partScores.part2 !== undefined ? 'text-primary' : 'text-muted'}`}>
                            {partScores.part2 !== null && partScores.part2 !== undefined ? partScores.part2 : 'N/A'}
                          </h3>
                          <small className="text-muted">
                            {userAnswers.part2?.length || 0} questions
                          </small>
                        </div>
                      </Col>
                      <Col md={4}>
                        <div className="text-center p-3 border rounded">
                          <h6 className="text-muted mb-2">Part 3</h6>
                          <h3 className={`mb-0 ${partScores.part3 !== null && partScores.part3 !== undefined ? 'text-primary' : 'text-muted'}`}>
                            {partScores.part3 !== null && partScores.part3 !== undefined ? partScores.part3 : 'N/A'}
                          </h3>
                          <small className="text-muted">
                            {userAnswers.part3?.length || 0} questions
                          </small>
                        </div>
                      </Col>
                    </Row>
                  </Col>
                </Row>
              </Card.Body>
            </Card>

            {/* Criteria Scores */}
            {criteriaScores && (
              <Card className="mb-4 border-0 shadow">
                <Card.Header className="bg-white border-0 py-3">
                  <h5 className="mb-0">📊 Assessment Criteria</h5>
                </Card.Header>
                <Card.Body className="p-4">
                  <Row className="g-4">
                    {Object.entries(criteriaScores).map(([key, value]) => (
                      <Col md={6} key={key}>
                        <div className="mb-2">
                          <div className="d-flex justify-content-between align-items-center mb-2">
                            <h6 className="mb-0">{criteriaLabels[key] || key}</h6>
                            <Badge bg={criteriaColors[key]} className="px-3 py-2">
                              {value}
                            </Badge>
                          </div>
                          <ProgressBar 
                            now={(value / 9) * 100} 
                            variant={criteriaColors[key]}
                            style={{ height: '8px' }}
                          />
                          <small className="text-muted">
                            {value >= 7 ? '✓ Strong performance' : value >= 5 ? '○ Good effort' : '△ Needs improvement'}
                          </small>
                        </div>
                      </Col>
                    ))}
                  </Row>
                </Card.Body>
              </Card>
            )}

            {/* Detailed Answers by Part */}
            <Card className="mb-4 border-0 shadow">
              <Card.Header className="bg-white border-0 py-3">
                <h5 className="mb-0">📝 Answer Details</h5>
              </Card.Header>
              <Card.Body className="p-4">
                <Accordion defaultActiveKey="0">
                  {['part1', 'part2', 'part3'].map((part, partIndex) => {
                    const partAnswers = userAnswers[part] || [];
                    if (partAnswers.length === 0) return null;

                    return (
                      <Accordion.Item eventKey={partIndex.toString()} key={part}>
                        <Accordion.Header>
                          <div className="d-fl!== null && partScores[part] !== undefined ex justify-content-between align-items-center w-100 me-3">
                            <span className="fw-bold">
                              Part {partIndex + 1} - {partAnswers.length} questions
                            </span>
                            {partScores[part] && (
                              <Badge bg="primary" className="ms-2">
                                Score: {partScores[part]}
                              </Badge>
                            )}
                          </div>
                        </Accordion.Header>
                        <Accordion.Body>
                          {partAnswers.map((answer, idx) => {
                            const feedback = parseFeedback(answer);
                            
                            return (
                              <Card key={answer.id} className="mb-3 border">
                                <Card.Body>
                                  <div className="d-flex justify-content-between align-items-start mb-3">
                                    <h6 className="mb-0 question-content-display">
                                      <Badge bg="secondary" className="me-2">Q{idx + 1}</Badge>
                                      <span dangerouslySetInnerHTML={{ __html: answer.question?.content }} />
                                    </h6>
                                    {answer.score && (
                                      <Badge bg="success" className="px-3 py-2">
                                        {answer.score}
                                      </Badge>
                                    )}
                                  </div>

                                  {/* Transcript */}
                                  {answer.answerText && (
                                    <div className="mb-3">
                                      <small className="text-muted fw-bold">Your Response:</small>
                                      <div className="bg-light p-3 rounded mt-2">
                                        <p className="mb-0" style={{ whiteSpace: 'pre-wrap' }}>
                                          {answer.answerText}
                                        </p>
                                      </div>
                                    </div>
                                  )}

                                  {/* Audio Player */}
                                  {answer.audioUrl && (
                                    <div className="mb-3">
                                      <small className="text-muted fw-bold">Recording:</small>
                                      <audio 
                                        controls 
                                        src={answer.audioUrl} 
                                        className="w-100 mt-2"
                                        style={{ height: '40px' }}
                                      />
                                    </div>
                                  )}

                                  {/* AI Feedback */}
                                  {feedback && (
                                    <div>
                                      <small className="text-muted fw-bold">AI Feedback:</small>
                                      
                                      {/* Criteria Scores */}
                                      {feedback.criteria_scores && (
                                        <Row className="g-2 mt-2 mb-3">
                                          {Object.entries(feedback.criteria_scores).map(([key, value]) => (
                                            <Col xs={6} md={3} key={key}>
                                              <div className="text-center p-2 border rounded">
                                                <small className="d-block text-muted">
                                                  {criteriaLabels[key]?.split(' ')[0] || key}
                                                </small>
                                                <strong className="text-primary">{value}</strong>
                                              </div>
                                            </Col>
                                          ))}
                                        </Row>
                                      )}

                                      {/* Examiner Feedback */}
                                      {feedback.examiner_feedback && (
                                        <div className="bg-light p-3 rounded mb-2">
                                          <small className="text-muted fw-bold d-block mb-2">
                                            💬 Examiner Comments:
                                          </small>
                                          <p className="mb-0 small">
                                            {feedback.examiner_feedback}
                                          </p>
                                        </div>
                                      )}

                                      {/* Strengths */}
                                      {feedback.strengths && feedback.strengths.length > 0 && (
                                        <div className="mb-2">
                                          <small className="text-success fw-bold">✓ Strengths:</small>
                                          <ul className="small mb-0 mt-1">
                                            {feedback.strengths.map((strength, i) => (
                                              <li key={i}>{strength}</li>
                                            ))}
                                          </ul>
                                        </div>
                                      )}

                                      {/* Areas for Improvement */}
                                      {feedback.areas_for_improvement && feedback.areas_for_improvement.length > 0 && (
                                        <div>
                                          <small className="text-warning fw-bold">△ Areas for Improvement:</small>
                                          <ul className="small mb-0 mt-1">
                                            {feedback.areas_for_improvement.map((area, i) => (
                                              <li key={i}>{area}</li>
                                            ))}
                                          </ul>
                                        </div>
                                      )}
                                    </div>
                                  )}
                                </Card.Body>
                              </Card>
                            );
                          })}
                        </Accordion.Body>
                      </Accordion.Item>
                    );
                  })}
                </Accordion>
              </Card.Body>
            </Card>

            {/* Action Buttons */}
            <Card className="border-0 shadow">
              <Card.Body className="text-center p-4">
                <Button 
                  variant="primary" 
                  size="lg"
                  onClick={() => navigate('/ieltstest/speaking')}
                  className="me-3"
                >
                  Back to Speaking Tests
                </Button>
                <Button 
                  variant="outline-primary" 
                  size="lg"
                  onClick={() => navigate('/dashboard')}
                >
                  Go to Dashboard
                </Button>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
    </div>
  );
};

export default SpeakingResultPage;
