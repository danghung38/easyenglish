import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Container, Row, Col, Card, Badge, Button, Spinner, Alert, Stack } from 'react-bootstrap';
import { FiCheckCircle, FiArrowRight, FiHome } from 'react-icons/fi';
import examService, { getMyExams } from '../../../services/examService';

/**
 * Full Test Result Page - Shows consolidated results for all 4 skills
 * Displays after completing Speaking test in Full Test mode
 */
const FullTestResultPage = () => {
  const { examId, userExamId } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [results, setResults] = useState({
    listening: null,
    reading: null,
    writing: null,
    speaking: null
  });
  const [overallBand, setOverallBand] = useState(null);
  const [error, setError] = useState(null);

  console.log('FullTestResultPage mounted - examId:', examId, 'userExamId:', userExamId);

  useEffect(() => {
    console.log('useEffect triggered - calling fetchAllResults');
    fetchAllResults();
  }, [examId, userExamId]);

  const fetchAllResults = async () => {
    try {
      console.log('fetchAllResults started - examId:', examId, 'userExamId:', userExamId);
      setLoading(true);
      
      let latestUserExam;
      
      if (userExamId) {
        console.log('Fetching by userExamId:', userExamId);
        // Fetch specific userExam by ID
        const response = await examService.getUserExamById(userExamId);
        console.log('getUserExamById response:', response);
        
        if (response.code !== 200 || !response.result) {
          setError('Không tìm thấy kết quả bài thi. Vui lòng làm lại bài thi.');
          setLoading(false);
          return;
        }
        
        latestUserExam = response.result;
        console.log('Fetched UserExam by ID:', latestUserExam);
      } else if (examId) {
        // Legacy: find userExam by examId
        const response = await examService.getMyExams();
        
        if (response.code !== 200 || !response.result) {
          setError('Không tìm thấy kết quả bài thi. Vui lòng làm lại bài thi.');
          setLoading(false);
          return;
        }

        // Find the most recent userExam for this examId
        const userExams = response.result
          .filter(userExam => userExam.examId === parseInt(examId))
          .sort((a, b) => new Date(b.startedAt) - new Date(a.startedAt));

        if (userExams.length === 0) {
          setError('Không tìm thấy bài thi này trong lịch sử của bạn.');
          setLoading(false);
          return;
        }

        latestUserExam = userExams[0];
        console.log('Latest UserExam:', latestUserExam);
      } else {
        setError('Thiếu thông tin bài thi.');
        setLoading(false);
        return;
      }

      // Get scores directly from parts array
      const parts = latestUserExam.parts || [];
      const listeningPart = parts.find(p => p.skillType === 'LISTENING' && p.submitted);
      const readingPart = parts.find(p => p.skillType === 'READING' && p.submitted);
      const writingPart = parts.find(p => p.skillType === 'WRITING' && p.submitted);
      const speakingPart = parts.find(p => p.skillType === 'SPEAKING' && p.submitted);

      console.log('Found parts:', {
        listening: listeningPart,
        reading: readingPart,
        writing: writingPart,
        speaking: speakingPart
      });

      // Build results directly from parts
      const fetchedResults = {
        listening: listeningPart ? { 
          band: listeningPart.score,
          userExamPartId: listeningPart.id 
        } : null,
        reading: readingPart ? { 
          band: readingPart.score,
          userExamPartId: readingPart.id 
        } : null,
        writing: writingPart ? { 
          overallBand: writingPart.score,
          userExamPartId: writingPart.id 
        } : null,
        speaking: speakingPart ? { 
          overallBand: speakingPart.score,
          userExamPartId: speakingPart.id 
        } : null
      };

      setResults(fetchedResults);

      // Calculate overall band score (average of completed skills)
      const bands = [];
      if (listeningPart?.score) bands.push(parseFloat(listeningPart.score));
      if (readingPart?.score) bands.push(parseFloat(readingPart.score));
      if (writingPart?.score) bands.push(parseFloat(writingPart.score));
      if (speakingPart?.score) bands.push(parseFloat(speakingPart.score));

      if (bands.length > 0) {
        const avg = bands.reduce((a, b) => a + b, 0) / bands.length;
        setOverallBand(avg.toFixed(1));
      }

    } catch (err) {
      console.error('Error fetching full test results:', err);
      setError('Không thể tải kết quả bài thi. Vui lòng thử lại sau.');
    } finally {
      setLoading(false);
    }
  };

  const getSkillColor = (skill) => {
    const colors = {
      listening: 'primary',
      reading: 'success',
      writing: 'warning',
      speaking: 'danger'
    };
    return colors[skill] || 'secondary';
  };

  const getSkillName = (skill) => {
    const names = {
      listening: 'Listening',
      reading: 'Reading',
      writing: 'Writing',
      speaking: 'Speaking'
    };
    return names[skill];
  };

  const getBandDescription = (band) => {
    const score = parseFloat(band);
    if (score >= 8.0) return 'Excellent - Very Good User';
    if (score >= 7.0) return 'Good User';
    if (score >= 6.0) return 'Competent User';
    if (score >= 5.0) return 'Modest User';
    return 'Limited User';
  };

  if (loading) {
    return (
      <Container className="my-5 text-center">
        <Spinner animation="border" variant="primary" style={{ width: '4rem', height: '4rem' }}>
          <span className="visually-hidden">Loading...</span>
        </Spinner>
        <h4 className="mt-4 fw-bold">Đang tải kết quả Full Test...</h4>
      </Container>
    );
  }

  if (error) {
    return (
      <Container className="my-5">
        <Alert variant="danger">
          <Alert.Heading>Lỗi</Alert.Heading>
          <p>{error}</p>
          <Button variant="outline-danger" onClick={() => navigate('/ieltstest')}>
            Quay về trang chủ
          </Button>
        </Alert>
      </Container>
    );
  }

  return (
    <Container className="my-4">
      {/* Header */}
      <Row className="mb-4">
        <Col>
          <div className="d-flex align-items-center justify-content-between">
            <div>
              <h2 className="fw-bold mb-2">
                <FiCheckCircle className="text-success me-2" />
                Full Test Completed!
              </h2>
              <p className="text-muted">Kết quả chi tiết theo từng kỹ năng</p>
            </div>
            <Button variant="outline-primary" onClick={() => navigate('/ieltstest/listening')}>
              <FiHome className="me-2" />
              Về trang chủ
            </Button>
          </div>
        </Col>
      </Row>

      {/* Overall Band Score */}
      {overallBand && (
        <Row className="mb-4">
          <Col>
            <Card 
              className="border-0 shadow-lg" 
              style={{ 
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                boxShadow: '0 10px 40px rgba(102, 126, 234, 0.4)'
              }}
            >
              <Card.Body className="text-center text-white py-5">
                <h2 className="mb-3 fw-bold text-uppercase" style={{ 
                  fontSize: '1.8rem',
                  letterSpacing: '2px',
                  textShadow: '2px 2px 4px rgba(0,0,0,0.3)'
                }}>
                  Overall Band Score
                </h2>
                <div 
                  className="display-1 fw-bold mb-3" 
                  style={{ 
                    fontSize: '6rem',
                    textShadow: '3px 3px 6px rgba(0,0,0,0.4)',
                    fontWeight: '900'
                  }}
                >
                  {overallBand}
                </div>
                <p className="mt-3 fs-4 fw-bold" style={{ 
                  textShadow: '1px 1px 3px rgba(0,0,0,0.3)',
                  letterSpacing: '1px'
                }}>
                  {getBandDescription(overallBand)}
                </p>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      )}

      {/* Individual Skills Results */}
      <Row>
        {['listening', 'reading', 'writing', 'speaking'].map((skill) => {
          const result = results[skill];
          const skillColor = getSkillColor(skill);
          
          return (
            <Col lg={6} className="mb-4" key={skill}>
              <Card className="h-100 shadow-sm">
                <Card.Header className={`bg-${skillColor} text-white`}>
                  <Stack direction="horizontal">
                    <div>
                      <h4 className="mb-0 fw-bold">{getSkillName(skill)}</h4>
                    </div>
                    <div className="ms-auto">
                      <Badge bg="light" text="dark">
                        <FiCheckCircle className="me-1" />
                        Completed
                      </Badge>
                    </div>
                  </Stack>
                </Card.Header>
                
                <Card.Body>
                  {result ? (
                    <>
                      {/* Band Score Display */}
                      <div className="text-center mb-3">
                        <Badge bg={skillColor} className="p-3">
                          <h2 className="display-4 mb-0 text-white fw-bold">
                            {skill === 'listening' || skill === 'reading' 
                              ? result.band 
                              : result.overallBand || 'N/A'}
                          </h2>
                        </Badge>
                        <p className="mt-2 text-muted">
                          {getBandDescription(
                            skill === 'listening' || skill === 'reading' 
                              ? result.band 
                              : result.overallBand
                          )}
                        </p>
                      </div>

                      {/* Detailed Scores */}
                      {(skill === 'writing' || skill === 'speaking') && (
                        <div className="mt-3">
                          {skill === 'writing' && result.criteria_scores && (
                            <div className="small">
                              <p className="mb-1"><strong>Task Achievement:</strong> {result.criteria_scores.task_achievement || 'N/A'}</p>
                              <p className="mb-1"><strong>Coherence & Cohesion:</strong> {result.criteria_scores.coherence_cohesion || 'N/A'}</p>
                              <p className="mb-1"><strong>Lexical Resource:</strong> {result.criteria_scores.lexical_resource || 'N/A'}</p>
                              <p className="mb-0"><strong>Grammar:</strong> {result.criteria_scores.grammatical_range || 'N/A'}</p>
                            </div>
                          )}
                          {skill === 'speaking' && result.criteria_scores && (
                            <div className="small">
                              <p className="mb-1"><strong>Fluency & Coherence:</strong> {result.criteria_scores.fluency_coherence || 'N/A'}</p>
                              <p className="mb-1"><strong>Lexical Resource:</strong> {result.criteria_scores.lexical_resource || 'N/A'}</p>
                              <p className="mb-1"><strong>Grammar:</strong> {result.criteria_scores.grammatical_range || result.criteria_scores.grammatical_range_accuracy || 'N/A'}</p>
                              <p className="mb-0"><strong>Pronunciation:</strong> {result.criteria_scores.pronunciation || 'N/A'}</p>
                            </div>
                          )}
                        </div>
                      )}
                    </>
                  ) : (
                    <Alert variant="warning">
                      Không tìm thấy kết quả cho phần thi này.
                    </Alert>
                  )}
                </Card.Body>

                <Card.Footer>
                  <Button 
                    variant={`outline-${skillColor}`} 
                    className="w-100"
                    disabled={!result}
                    onClick={() => {
                      if (!result) return;
                      
                      // Navigate to detailed result page with userExamPartId in URL
                      const paths = {
                        listening: `/ieltstest/listening/result/${result.userExamPartId}`,
                        reading: `/ieltstest/reading/result/${result.userExamPartId}`,
                        writing: `/ieltstest/writing/result/${result.userExamPartId}`,
                        speaking: `/ieltstest/speaking/result/${result.userExamPartId}`
                      };
                      
                      // Optional: Pass state for additional data
                      const state = {
                        score: skill === 'listening' || skill === 'reading' 
                          ? result.score 
                          : parseFloat(result.overallBand)
                      };
                      
                      navigate(paths[skill], { state });
                    }}
                  >
                    Xem chi tiết {getSkillName(skill)}
                    <FiArrowRight className="ms-2" />
                  </Button>
                </Card.Footer>
              </Card>
            </Col>
          );
        })}
      </Row>

      {/* Action Buttons */}
      <Row className="mt-4">
        <Col className="text-center">
          <Button 
            variant="primary" 
            size="lg" 
            onClick={() => navigate('/ieltstest/listening')}
            className="me-3"
          >
            <FiHome className="me-2" />
            Về trang danh sách bài thi
          </Button>
          <Button 
            variant="outline-primary" 
            size="lg"
            onClick={() => window.print()}
          >
            In kết quả
          </Button>
        </Col>
      </Row>
    </Container>
  );
};

export default FullTestResultPage;
