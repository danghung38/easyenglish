import React, { useState, useEffect, useRef } from "react";
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
  ProgressBar,
  Modal,
  ButtonGroup
} from "react-bootstrap";
import { useAlert } from '../../../context/AlertContext';
import { 
  createUserExam,
  getUserExamById,
  getSpeakingQuestionsByExamId,
  getMyExams
} from "../../../services/examService";
import { evaluateSpeakingByQuestion, evaluatePartWithMultipleRecordings, submitSpeakingExam as submitSpeakingExamAPI } from '../../../services/speakingService';
import '../../admin/QuillCustom.css';
import './SpeakingExam.css';

const AttemptSpeakingExamPage = () => {
  const navigate = useNavigate();
  const { examId } = useParams();
  const [searchParams] = useSearchParams();
  const { showSuccess, showError, showWarning } = useAlert();
  const mediaRecorderRef = useRef(null);
  const fileInputRef = useRef(null);
  
  // Debug: Log examId to check if it's being parsed correctly
  console.log('📌 AttemptSpeakingExamPage - examId from useParams():', examId, typeof examId);
  
  // Full Test mode detection (UI only)
  const isFullTest = searchParams.get('fullTest') === 'true';
  const audioChunksRef = useRef([]);
  const timerIntervalRef = useRef(null);

  // State management
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [userExamPartId, setUserExamPartId] = useState(null);
  const [userExamId, setUserExamId] = useState(null);
  
  // Part-based navigation
  const [selectedPart, setSelectedPart] = useState('part1');
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [recordings, setRecordings] = useState({});
  
  // Recording state
  const [isRecording, setIsRecording] = useState(false);
  const [recordingTime, setRecordingTime] = useState(0);
  const [uploadMode, setUploadMode] = useState('record'); // 'record' or 'upload'
  
  // UI state
  const [testStarted, setTestStarted] = useState(false);
  const [micAccessError, setMicAccessError] = useState(null);
  
  // Evaluation state
  const [partEvaluations, setPartEvaluations] = useState({
    part1: null,
    part2: null,
    part3: null
  });
  const [evaluating, setEvaluating] = useState(false);
  const [submittedExam, setSubmittedExam] = useState(false);
  const [overallResult, setOverallResult] = useState(null);
  const [showSubmitConfirm, setShowSubmitConfirm] = useState(false);
  const [submitWarningMessage, setSubmitWarningMessage] = useState('');
  
  // Transcript and audio states
  const [questionTranscripts, setQuestionTranscripts] = useState({});
  const [audioURLs, setAudioURLs] = useState({});

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
      if (timerIntervalRef.current) clearInterval(timerIntervalRef.current);
      if (mediaRecorderRef.current?.stream) {
        mediaRecorderRef.current.stream.getTracks().forEach(track => track.stop());
      }
    };
  }, [examId]);

  // Get questions by part
  const getQuestionsByPart = (part) => {
    return questions.filter(q => q.section?.toLowerCase() === part.toLowerCase());
  };

  // Get current part questions
  const currentPartQuestions = getQuestionsByPart(selectedPart);
  const currentQuestion = currentPartQuestions[currentQuestionIndex];

  const initializeExam = async () => {
    try {
      setLoading(true);
      setError(null);

      const questionsResponse = await getSpeakingQuestionsByExamId(Number(examId));
      
      if (questionsResponse.code !== 200) {
        throw new Error("Failed to load speaking questions");
      }

      const questionsList = questionsResponse.result || [];
      
      if (questionsList.length === 0) {
        throw new Error("No speaking questions found for this exam");
      }

      setQuestions(questionsList);
      
      let userExamData;

      // In Full Test mode, reuse existing UserExam from sessionStorage
      if (isFullTest) {
        const savedUserExamId = sessionStorage.getItem(`fullTest_userExamId_${examId}`);
        if (savedUserExamId) {
          console.log('🎤 Full Test mode: Reusing existing UserExam ID:', savedUserExamId);
          const response = await getUserExamById(Number(savedUserExamId));
          if (response.code === 200) {
            userExamData = response.result;
            console.log('✅ Reused UserExam:', userExamData);
          }
        }
      }

      // If not Full Test or no saved UserExam, create new one
      if (!userExamData) {
        console.log('Creating new user exam for SPEAKING...');
        const userExamResponse = await createUserExam(Number(examId), 'SPEAKING');
        
        if (userExamResponse.code !== 201) {
          throw new Error('Failed to create user exam');
        }
        
        userExamData = userExamResponse.result;
        console.log('User exam created:', userExamData);
      }
      
      // Find speaking part
      if (userExamData) {
        setUserExamId(userExamData.id);
        const speakingPart = userExamData.parts?.find(p => p.skillType === 'SPEAKING');
        if (speakingPart) {
          setUserExamPartId(speakingPart.id);
          console.log('Speaking part found:', speakingPart);
          console.log('UserExam ID:', userExamData.id);
        }
      }
      
      setLoading(false);

    } catch (err) {
      console.error("Error initializing exam:", err);
      setError(err.message || "Failed to load speaking test");
      setLoading(false);
    }
  };

  const requestMicrophoneAccess = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const recorder = new MediaRecorder(stream, {
        mimeType: MediaRecorder.isTypeSupported('audio/webm') ? 'audio/webm' : 'audio/mp4'
      });
      
      recorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          audioChunksRef.current.push(event.data);
        }
      };

      mediaRecorderRef.current = recorder;
      setTestStarted(true);
      setMicAccessError(null);
    } catch (err) {
      console.error("Microphone access error:", err);
      setMicAccessError("Failed to access microphone. Please check your browser permissions.");
    }
  };

  const startRecording = () => {
    if (mediaRecorderRef.current && mediaRecorderRef.current.state === "inactive") {
      audioChunksRef.current = [];
      setRecordingTime(0);
      
      mediaRecorderRef.current.start();
      setIsRecording(true);
      
      timerIntervalRef.current = setInterval(() => {
        setRecordingTime(prev => prev + 1);
      }, 1000);
    }
  };

  const stopRecording = () => {
    return new Promise((resolve) => {
      if (mediaRecorderRef.current && mediaRecorderRef.current.state === "recording") {
        mediaRecorderRef.current.onstop = () => {
          const audioBlob = new Blob(audioChunksRef.current, { type: 'audio/webm' });
          const duration = recordingTime;
          const audioURL = URL.createObjectURL(audioBlob);
          
          setRecordings(prev => ({
            ...prev,
            [currentQuestion.id]: {
              blob: audioBlob,
              duration: duration,
              questionId: currentQuestion.id,
              section: currentQuestion.section
            }
          }));
          
          setAudioURLs(prev => ({
            ...prev,
            [currentQuestion.id]: audioURL
          }));

          if (timerIntervalRef.current) {
            clearInterval(timerIntervalRef.current);
            timerIntervalRef.current = null;
          }

          setIsRecording(false);
          setRecordingTime(0);
          audioChunksRef.current = [];
          
          resolve();
        };

        mediaRecorderRef.current.stop();
      } else {
        resolve();
      }
    });
  };

  const deleteRecording = () => {
    if (audioURLs[currentQuestion.id]) {
      URL.revokeObjectURL(audioURLs[currentQuestion.id]);
    }
    
    setRecordings(prev => {
      const newRecordings = { ...prev };
      delete newRecordings[currentQuestion.id];
      return newRecordings;
    });
    
    setAudioURLs(prev => {
      const newURLs = { ...prev };
      delete newURLs[currentQuestion.id];
      return newURLs;
    });
    
    setQuestionTranscripts(prev => {
      const newTranscripts = { ...prev };
      delete newTranscripts[currentQuestion.id];
      return newTranscripts;
    });
  };

  const handleFileUpload = (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // Validate file type
    const validTypes = ['audio/mpeg', 'audio/mp3', 'audio/wav', 'audio/webm', 'audio/ogg', 'audio/mp4', 'audio/m4a'];
    if (!validTypes.includes(file.type) && !file.name.match(/\.(mp3|wav|webm|ogg|m4a|mp4)$/i)) {
      showWarning('Vui lòng chọn file audio hợp lệ (MP3, WAV, WEBM, OGG, M4A)');
      return;
    }

    // Validate file size (max 50MB)
    const maxSize = 50 * 1024 * 1024; // 50MB
    if (file.size > maxSize) {
      showWarning('File quá lớn! Vui lòng chọn file nhỏ hơn 50MB.');
      return;
    }

    // Create blob and audio URL
    const audioBlob = new Blob([file], { type: file.type });
    const audioURL = URL.createObjectURL(audioBlob);

    // Get audio duration
    const audio = new Audio(audioURL);
    audio.addEventListener('loadedmetadata', () => {
      const duration = Math.floor(audio.duration);
      
      setRecordings(prev => ({
        ...prev,
        [currentQuestion.id]: {
          blob: audioBlob,
          duration: duration,
          questionId: currentQuestion.id,
          section: currentQuestion.section,
          fileName: file.name
        }
      }));
      
      setAudioURLs(prev => ({
        ...prev,
        [currentQuestion.id]: audioURL
      }));

      console.log('✅ File uploaded:', file.name, 'Duration:', duration, 's');
    });

    // Reset file input
    event.target.value = '';
  };

  const nextQuestion = () => {
    if (currentQuestionIndex < currentPartQuestions.length - 1) {
      setCurrentQuestionIndex(currentQuestionIndex + 1);
    }
  };

  const previousQuestion = () => {
    if (currentQuestionIndex > 0) {
      setCurrentQuestionIndex(currentQuestionIndex - 1);
    }
  };

  const changePart = (part) => {
    setSelectedPart(part);
    setCurrentQuestionIndex(0);
  };

  const evaluateCurrentPart = async () => {
    try {
      setEvaluating(true);
      const partQuestions = getQuestionsByPart(selectedPart);
      
      // Get recordings for current part
      const partRecordings = partQuestions
        .filter(q => recordings[q.id])
        .map((q, idx) => ({
          questionId: q.id,
          audioBlob: recordings[q.id].blob,
          part: selectedPart,
          questionIndex: idx
        }));

      if (partRecordings.length === 0) {
        alert("⚠️ Vui lòng ghi âm ít nhất 1 câu trả lời cho part này!");
        setEvaluating(false);
        return;
      }

      // Get or create userExamPartId (hardcode tạm = 1 hoặc lấy từ API)
      const currentUserExamPartId = userExamPartId || 1;

      console.log(`📊 Evaluating ${selectedPart} with ${partRecordings.length} recordings...`);
      console.log(`💾 Saving to userExamPartId: ${currentUserExamPartId}`);

      // NEW APPROACH: Call backend API for EACH question to save to database
      const evaluationResults = [];

      for (const recording of partRecordings) {
        try {
          console.log(`🔍 Evaluating question ${recording.questionId}...`);
          console.log(`📝 Question ID: ${recording.questionId}, UserExamPartId: ${currentUserExamPartId}`);
          
          // Use the service function with userExamPartId to save to database
          const response = await evaluateSpeakingByQuestion(
            recording.audioBlob,
            recording.questionId,
            currentUserExamPartId,
            null // audioUrl (optional - can be S3 URL if uploaded)
          );

          if (response?.code === 200 && response?.result) {
            evaluationResults.push(response.result);
            console.log(`✅ Question ${recording.questionId} evaluated and saved to database`);
          } else {
            throw new Error('Invalid response from evaluation service');
          }
        } catch (err) {
          console.error(`❌ Failed to evaluate question ${recording.questionId}:`, err);
          console.error(`❌ Error details:`, {
            message: err.message,
            response: err.response?.data,
            status: err.response?.status
          });
          throw new Error(`Failed to evaluate question ${recording.questionId}: ${err.message}`);
        }
      }

      // Calculate average scores for this part - Round to 0.5
      const avgBand = evaluationResults.reduce((sum, r) => sum + (r.overall_band || 0), 0) / evaluationResults.length;
      const avgCriteria = {
        fluency_coherence: Math.round((evaluationResults.reduce((sum, r) => sum + (r.criteria_scores?.fluency_coherence || 0), 0) / evaluationResults.length) * 2) / 2,
        lexical_resource: Math.round((evaluationResults.reduce((sum, r) => sum + (r.criteria_scores?.lexical_resource || 0), 0) / evaluationResults.length) * 2) / 2,
        grammatical_range_accuracy: Math.round((evaluationResults.reduce((sum, r) => sum + (r.criteria_scores?.grammatical_range_accuracy || 0), 0) / evaluationResults.length) * 2) / 2,
        pronunciation: Math.round((evaluationResults.reduce((sum, r) => sum + (r.criteria_scores?.pronunciation || 0), 0) / evaluationResults.length) * 2) / 2,
      };

      // Prepare part result
      const partResult = {
        part: selectedPart,
        overall_band: Math.round(avgBand * 2) / 2, // Round to nearest 0.5
        criteria_scores: avgCriteria,
        examiner_feedback: evaluationResults[0]?.examiner_feedback || '',
        strengths: evaluationResults[0]?.strengths || [],
        areas_for_improvement: evaluationResults[0]?.areas_for_improvement || [],
        recordedCount: partRecordings.length,
        totalQuestions: partQuestions.length,
        detailed_feedback: evaluationResults[0]?.detailed_feedback || ''
      };

      setPartEvaluations(prev => ({
        ...prev,
        [selectedPart]: partResult
      }));

      setEvaluating(false);
      
      // Show success message
      showSuccess(`Đã chấm và lưu xong ${selectedPart.toUpperCase()}! Điểm trung bình: ${partResult.overall_band} - Số câu đã chấm: ${partRecordings.length}/${partQuestions.length}`);

    } catch (err) {
      console.error("Error evaluating part:", err);
      showError(`Lỗi khi chấm bài: ${err.message || 'Unknown error'}`);
      setEvaluating(false);
    }
  };

  const submitSpeakingExam = async () => {
    // Check how many parts are evaluated
    const evaluatedParts = ['part1', 'part2', 'part3'].filter(
      part => partEvaluations[part] !== null
    );

    if (evaluatedParts.length === 0) {
      showWarning("Vui lòng chấm ít nhất 1 part trước khi nộp bài!");
      return;
    }

    const allPartsCompleted = evaluatedParts.length === 3;
    const warningMessage = !allPartsCompleted 
      ? `Bạn mới chỉ hoàn thành ${evaluatedParts.length}/3 parts. Điểm tổng sẽ chỉ tính dựa trên các parts đã chấm.`
      : '';

    setSubmitWarningMessage(warningMessage);
    setShowSubmitConfirm(true);
  };

  const handleConfirmSubmit = async () => {
    setShowSubmitConfirm(false);
    
    try {
      setEvaluating(true);

      const evaluatedParts = ['part1', 'part2', 'part3'].filter(
        part => partEvaluations[part] !== null
      );

      // Calculate overall score với trọng số: Part1 20%, Part2 40%, Part3 40%
      const part1Score = partEvaluations.part1?.overall_band || 0;
      const part2Score = partEvaluations.part2?.overall_band || 0;
      const part3Score = partEvaluations.part3?.overall_band || 0;
      
      // Tính weighted average
      let overallScore = 0;
      if (evaluatedParts.length === 3) {
        // Full test: áp dụng trọng số
        overallScore = Math.round((part1Score * 0.2 + part2Score * 0.4 + part3Score * 0.4) * 2) / 2;
      } else {
        // Không đủ 3 parts: tính trung bình các parts đã chấm
        const totalScore = part1Score + part2Score + part3Score;
        overallScore = Math.round((totalScore / evaluatedParts.length) * 2) / 2;
      }

      // Calculate average criteria THEO TRỌNG SỐ (Part1: 20%, Part2: 40%, Part3: 40%)
      const avgCriteria = {};
      ['fluency_coherence', 'lexical_resource', 'grammatical_range_accuracy', 'pronunciation'].forEach(criterion => {
        if (evaluatedParts.length === 3) {
          // Full test: Tính theo trọng số
          const weighted = (
            (partEvaluations.part1?.criteria_scores?.[criterion] || 0) * 0.2 +
            (partEvaluations.part2?.criteria_scores?.[criterion] || 0) * 0.4 +
            (partEvaluations.part3?.criteria_scores?.[criterion] || 0) * 0.4
          );
          avgCriteria[criterion] = Math.round(weighted * 2) / 2;
        } else {
          // Không đủ 3 parts: Tính trung bình thông thường
          const sum = (
            (partEvaluations.part1?.criteria_scores?.[criterion] || 0) +
            (partEvaluations.part2?.criteria_scores?.[criterion] || 0) +
            (partEvaluations.part3?.criteria_scores?.[criterion] || 0)
          );
          avgCriteria[criterion] = evaluatedParts.length > 0 
            ? Math.round((sum / evaluatedParts.length) * 2) / 2 
            : 0;
        }
      });

        const result = {
          overall_band: overallScore,
          criteria_scores: avgCriteria,
          part_scores: {
            part1: part1Score || null,
            part2: part2Score || null,
            part3: part3Score || null
          },
          evaluated_parts: evaluatedParts.length,
          examiner_feedback: `Điểm tổng kết IELTS Speaking: ${overallScore}\n` +
                            `(Tính dựa trên ${evaluatedParts.length}/3 parts đã hoàn thành)\n\n` +
                            `📊 Chi tiết theo từng part:\n` +
                            (partEvaluations.part1 ? `• Part 1 (Introduction): ${part1Score} ✅\n` : `• Part 1: Chưa hoàn thành\n`) +
                            (partEvaluations.part2 ? `• Part 2 (Long Turn): ${part2Score} ✅\n` : `• Part 2: Chưa hoàn thành\n`) +
                            (partEvaluations.part3 ? `• Part 3 (Discussion): ${part3Score} ✅\n` : `• Part 3: Chưa hoàn thành\n`) +
                            `\n📈 Điểm theo tiêu chí:\n` +
                            `• Fluency & Coherence: ${avgCriteria.fluency_coherence}\n` +
                            `• Lexical Resource: ${avgCriteria.lexical_resource}\n` +
                            `• Grammatical Range: ${avgCriteria.grammatical_range_accuracy}\n` +
                            `• Pronunciation: ${avgCriteria.pronunciation}`
        };

        // ✅ SUBMIT TO BACKEND to save final score to UserExamPart
        console.log('📤 Submitting final scores to backend...');
        const submissionData = {
          userExamPartId: userExamPartId,
          overallBand: overallScore,
          part1Score: part1Score || null,
          part2Score: part2Score || null,
          part3Score: part3Score || null,
          fluencyCoherence: avgCriteria.fluency_coherence,
          lexicalResource: avgCriteria.lexical_resource,
          grammaticalRange: avgCriteria.grammatical_range_accuracy,
          pronunciation: avgCriteria.pronunciation,
          overallFeedback: result.examiner_feedback
        };

        await submitSpeakingExamAPI(submissionData);
        console.log('✅ Final scores saved to database');

        setOverallResult(result);
        setSubmittedExam(true);
        setEvaluating(false);

        // Navigate: Full Test goes to consolidated result page, regular test to individual result
        if (isFullTest) {
          // Clean up Full Test sessionStorage
          sessionStorage.removeItem(`fullTest_userExamId_${examId}`);
          console.log('✅ Full Test completed - cleaned up sessionStorage');
          console.log('📍 Navigating to full test result with userExamId:', userExamId);
          
          if (!userExamId) {
            console.error('❌ userExamId is missing! Cannot navigate to result page');
            showError('Lỗi: Không tìm thấy ID bài thi. Vui lòng thử lại.');
            return;
          }
          
          navigate(`/ieltstest/fulltest/result/${userExamId}`);
        } else {
          // Navigate to speaking result page with userExamPartId
          navigate(`/ieltstest/speaking/result/${userExamPartId}`, {
            state: {
              userExamPartId: userExamPartId,
              overallBand: overallScore,
              result: result
            }
          });
        }

    } catch (err) {
      console.error("Error submitting exam:", err);
      showError(`Lỗi khi nộp bài: ${err.message || 'Unknown error'}`);
      setEvaluating(false);
    }
  };

  const handleCancelSubmit = () => {
    setShowSubmitConfirm(false);
  };

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const getPartIcon = (part) => {
    const hasRecordings = getQuestionsByPart(part).some(q => recordings[q.id]);
    const isEvaluated = partEvaluations[part] !== null;
    
    if (isEvaluated) return '✅';
    if (hasRecordings) return '🎤';
    return '⭕';
  };

  const getRecordedCount = (part) => {
    const partQuestions = getQuestionsByPart(part);
    const recorded = partQuestions.filter(q => recordings[q.id]).length;
    return `${recorded}/${partQuestions.length}`;
  };

  if (loading) {
    return (
      <Container className="py-5 text-center">
        <Spinner animation="border" variant="primary" style={{ width: '3rem', height: '3rem' }} />
        <p className="mt-3 lead">Loading speaking test...</p>
      </Container>
    );
  }

  if (error) {
    return (
      <Container className="py-5">
        <Alert variant="danger">
          <Alert.Heading>Error Loading Test</Alert.Heading>
          <p>{error}</p>
          <Button variant="outline-danger" onClick={() => navigate('/ieltstest/speaking')}>
            Back to Speaking Tests
          </Button>
        </Alert>
      </Container>
    );
  }

  const hasRecording = recordings[currentQuestion?.id];
  const totalRecorded = Object.keys(recordings).length;

  return (
    <div className="speaking-exam-container bg-light min-vh-100">
      <Container fluid className="py-4">
        {!testStarted ? (
          <Container className="my-3">
            <Row className="justify-content-center">
              <Col sm={8}>
                <Card className="shadow-sm">
                  <Card.Header className="bg-primary text-white">
                    <div className="d-flex justify-content-between align-items-center">
                      <h3 className="m-0 fw-bold">IELTS Speaking Test</h3>
                      {isFullTest && (
                        <Badge bg="light" text="primary" className="px-3 py-2">
                          <i className="fe fe-layers me-1"></i>
                          FULL TEST MODE - Part 4 of 4
                        </Badge>
                      )}
                    </div>
                  </Card.Header>
                  <Card.Body>
                    {micAccessError ? (
                      <Alert variant="danger">
                        <Alert.Heading>Microphone Access Needed</Alert.Heading>
                        <p>{micAccessError}</p>
                      </Alert>
                    ) : (
                      <>
                        <p className="fw-bold" style={{ fontSize: "16px" }}>
                          Welcome to the IELTS Speaking Test. This test consists of 3 parts.
                        </p>
                        <p style={{ fontSize: "16px" }}>
                          <ul>
                            <li>Please ensure your microphone is properly connected and working.</li>
                            <li>You can practice any part individually and get AI evaluation for each part.</li>
                            <li>Part 1: Introduction & Interview (4-5 minutes)</li>
                            <li>Part 2: Long Turn / Cue Card (3-4 minutes)</li>
                            <li>Part 3: Discussion (4-5 minutes)</li>
                            <li>You can record, stop, and re-record your answers as needed.</li>
                          </ul>
                        </p>
                      </>
                    )}
                  </Card.Body>
                  <Card.Footer>
                    <Button onClick={requestMicrophoneAccess}>
                      Start Test
                    </Button>
                  </Card.Footer>
                </Card>
              </Col>
            </Row>
          </Container>
        ) : (
          <>
            <Container>
              {isFullTest && (
                <Alert variant="success" className="mb-3 text-center">
                  <Badge bg="success" className="px-3 py-2 me-2">
                    <i className="fe fe-layers me-1"></i>
                    FULL TEST MODE
                  </Badge>
                  <strong>Part 4 of 4: Speaking</strong>
                </Alert>
              )}
              <Row className="g-4">
                {/* Left Column - Instructions */}
                <Col lg={6}>
                  <Card className="border-0 shadow-sm h-100">
                    <Card.Body className="p-4">
                      <h4 className="mb-1">
                        Speaking Practice - Part {selectedPart.replace('part', '')}:
                      </h4>
                      <p className="text-muted mb-4">
                        {selectedPart === 'part1' && 'Introduction & Interview'}
                        {selectedPart === 'part2' && 'Long Turn (Cue Card)'}
                        {selectedPart === 'part3' && 'Discussion'}
                      </p>

                      <h6 className="fw-bold mb-3">Instructions:</h6>
                      <p className="mb-4" style={{fontSize: '0.95rem'}}>
                        {selectedPart === 'part1' && "Answer the following questions about yourself. You have 4-5 minutes for this part. Press 'Record' to begin, and 'Stop' when finished. You can re-record your answers."}
                        {selectedPart === 'part2' && "You will be given a topic card. You have 1 minute to prepare and make notes. Then speak for 2 minutes on the topic. Press 'Record' to begin your preparation."}
                        {selectedPart === 'part3' && "Discuss the topic in more depth with follow-up questions. You have 4-5 minutes for this part. Press 'Record' to begin, and 'Stop' when finished."}
                      </p>

                      <h6 className="fw-bold mb-3">
                        {selectedPart === 'part2' ? 'Topic Card:' : 'Prompt Questions:'}
                      </h6>
                      
                      {currentPartQuestions.map((q, idx) => (
                        <div 
                          key={q.id} 
                          className={`p-3 mb-2 rounded ${currentQuestionIndex === idx ? 'bg-primary text-white' : 'bg-light'}`}
                          style={{cursor: 'pointer'}}
                          onClick={() => setCurrentQuestionIndex(idx)}
                        >
                          <div className="d-flex justify-content-between align-items-start">
                            <div className="flex-grow-1 question-content-display">
                              <small className="fw-bold">{idx + 1}.</small>{' '}
                              <span dangerouslySetInnerHTML={{ __html: q.content }} />
                            </div>
                            {recordings[q.id] && (
                              <Badge bg={currentQuestionIndex === idx ? 'light' : 'success'} text={currentQuestionIndex === idx ? 'primary' : 'white'} className="ms-2">
                                ✓
                              </Badge>
                            )}
                          </div>
                        </div>
                      ))}
                      
                      {/* Part Switcher */}
                      <div className="mt-4">
                        <ButtonGroup className="w-100">
                          <Button
                            size="sm"
                            variant={selectedPart === 'part1' ? 'primary' : 'outline-secondary'}
                            onClick={() => { setSelectedPart('part1'); setCurrentQuestionIndex(0); }}
                          >
                            Part 1
                          </Button>
                          <Button
                            size="sm"
                            variant={selectedPart === 'part2' ? 'primary' : 'outline-secondary'}
                            onClick={() => { setSelectedPart('part2'); setCurrentQuestionIndex(0); }}
                          >
                            Part 2
                          </Button>
                          <Button
                            size="sm"
                            variant={selectedPart === 'part3' ? 'primary' : 'outline-secondary'}
                            onClick={() => { setSelectedPart('part3'); setCurrentQuestionIndex(0); }}
                          >
                            Part 3
                          </Button>
                        </ButtonGroup>
                      </div>
                    </Card.Body>
                  </Card>
                </Col>

                {/* Right Column - Recording Studio */}
                <Col lg={6}>
                  <Card className="border-0 shadow-sm mb-3">
                    <Card.Header className="bg-white border-0 p-4">
                      <div className="d-flex justify-content-between align-items-center">
                        <h5 className="mb-0">Recording Studio</h5>
                        <ButtonGroup size="sm">
                          <Button 
                            variant={uploadMode === 'record' ? 'primary' : 'outline-secondary'}
                            onClick={() => setUploadMode('record')}
                          >
                            🎤 Ghi âm
                          </Button>
                          <Button 
                            variant={uploadMode === 'upload' ? 'primary' : 'outline-secondary'}
                            onClick={() => setUploadMode('upload')}
                          >
                            📎 Upload
                          </Button>
                        </ButtonGroup>
                      </div>
                    </Card.Header>
                    <Card.Body className="p-4">
                      {/* Timer Display */}
                      <div 
                        className="text-center p-5 rounded mb-4 bg-primary"
                        style={{position: 'relative'}}
                      >
                        <div className="d-flex justify-content-between text-white fw-bold mb-3" style={{fontSize: '1.1rem'}}>
                          <span>{isRecording ? formatTime(recordingTime) : '00:00'}</span>
                          <span>/</span>
                          <span>05:00</span>
                        </div>
                        
                        <div className="bg-white rounded-circle mx-auto d-flex align-items-center justify-content-center" 
                             style={{width: '100px', height: '100px', marginTop: '20px', marginBottom: '20px'}}>
                          {isRecording ? (
                            <div className="bg-danger rounded-circle d-flex align-items-center justify-content-center" style={{width: '80px', height: '80px', animation: 'pulse 1.5s infinite'}}>
                              <svg width="40" height="40" viewBox="0 0 24 24" fill="white">
                                <path d="M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3z"/>
                                <path d="M17 11c0 2.76-2.24 5-5 5s-5-2.24-5-5H5c0 3.53 2.61 6.43 6 6.92V21h2v-3.08c3.39-.49 6-3.39 6-6.92h-2z"/>
                              </svg>
                            </div>
                          ) : hasRecording ? (
                            <svg width="50" height="50" viewBox="0 0 24 24" fill="#28a745">
                              <path d="M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z"/>
                            </svg>
                          ) : (
                            <svg width="50" height="50" viewBox="0 0 24 24" fill="#6c757d">
                              <path d="M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3z"/>
                              <path d="M17 11c0 2.76-2.24 5-5 5s-5-2.24-5-5H5c0 3.53 2.61 6.43 6 6.92V21h2v-3.08c3.39-.49 6-3.39 6-6.92h-2z"/>
                            </svg>
                          )}
                        </div>
                        
                        <div className="text-white">
                          {isRecording ? 'Recording...' : hasRecording ? 'Recorded' : 'Record Answer'}
                        </div>
                      </div>

                      {/* Recording Controls */}
                      <Row className="g-2 mb-4">
                        {uploadMode === 'upload' ? (
                          /* Upload Mode */
                          <>
                            <Col>
                              <input
                                ref={fileInputRef}
                                type="file"
                                accept="audio/*,.mp3,.wav,.webm,.ogg,.m4a"
                                onChange={handleFileUpload}
                                style={{ display: 'none' }}
                              />
                              <Button 
                                variant="primary"
                                className="w-100"
                                onClick={() => fileInputRef.current?.click()}
                                disabled={isRecording}
                              >
                                📎 Chọn file audio
                              </Button>
                            </Col>
                            {hasRecording && (
                              <Col>
                                <Button 
                                  variant="outline-primary" 
                                  className="w-100 d-flex align-items-center justify-content-center gap-2"
                                  onClick={() => {
                                    const audio = new Audio(audioURLs[currentQuestion.id]);
                                    audio.play();
                                  }}
                                >
                                  <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M8 5v14l11-7z"/>
                                  </svg>
                                  Phát lại
                                </Button>
                              </Col>
                            )}
                          </>
                        ) : (
                          /* Recording Mode */
                          isRecording ? (
                            <Col>
                              <Button 
                                variant="danger"
                                className="w-100"
                                onClick={stopRecording}
                              >
                                Stop Recording
                              </Button>
                            </Col>
                          ) : (
                            <>
                              {hasRecording && (
                                <>
                                  <Col>
                                    <Button 
                                      variant="outline-primary" 
                                      className="w-100 d-flex align-items-center justify-content-center gap-2"
                                      onClick={() => {
                                        const audio = new Audio(audioURLs[currentQuestion.id]);
                                        audio.play();
                                      }}
                                    >
                                      <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M8 5v14l11-7z"/>
                                      </svg>
                                      Play Back
                                    </Button>
                                  </Col>
                                  <Col>
                                    <Button 
                                      variant="outline-primary"
                                      className="w-100 d-flex align-items-center justify-content-center gap-2"
                                      onClick={startRecording}
                                    >
                                      <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3z"/>
                                      </svg>
                                      Re-record
                                    </Button>
                                  </Col>
                                </>
                              )}
                              {!hasRecording && (
                                <Col>
                                  <Button 
                                    variant="primary"
                                    className="w-100"
                                    onClick={startRecording}
                                  >
                                    Record Answer
                                  </Button>
                                </Col>
                              )}
                            </>
                          )
                        )}
                      </Row>

                      {/* Audio Player */}
                      {hasRecording && audioURLs[currentQuestion.id] && (
                        <div className="bg-light p-3 rounded mb-3">
                          {recordings[currentQuestion.id]?.fileName && (
                            <div className="mb-2 text-muted small">
                              📁 {recordings[currentQuestion.id].fileName}
                            </div>
                          )}
                          <audio 
                            controls 
                            src={audioURLs[currentQuestion.id]} 
                            className="w-100"
                            style={{height: '40px'}}
                          />
                        </div>
                      )}
                      
                      {/* Delete Recording Button */}
                      {hasRecording && (
                        <Button 
                          variant="outline-danger"
                          size="sm"
                          className="w-100 mb-2"
                          onClick={deleteRecording}
                        >
                          Delete Recording
                        </Button>
                      )}
                    </Card.Body>
                  </Card>

                  {/* Evaluate and Continue Buttons */}
                  <Row className="g-2">
                    <Col>
                      <Button 
                        variant="success" 
                        size="lg" 
                        className="w-100 py-3"
                        onClick={evaluateCurrentPart}
                        disabled={evaluating || !getQuestionsByPart(selectedPart).some(q => recordings[q.id])}
                      >
                        {evaluating ? (
                          <>
                            <Spinner animation="border" size="sm" className="me-2" />
                            Đang chấm bài...
                          </>
                        ) : (
                          <>
                            Chấm {selectedPart.toUpperCase()} 
                            <Badge bg="light" text="success" className="ms-2">
                              {getQuestionsByPart(selectedPart).filter(q => recordings[q.id]).length}/{getQuestionsByPart(selectedPart).length} câu
                            </Badge>
                          </>
                        )}
                      </Button>
                    </Col>
                    {selectedPart !== 'part3' && (
                      <Col>
                        <Button 
                          variant="primary" 
                          size="lg" 
                          className="w-100 py-3"
                          onClick={() => {
                            const nextPart = selectedPart === 'part1' ? 'part2' : 'part3';
                            setSelectedPart(nextPart);
                            setCurrentQuestionIndex(0);
                          }}
                        >
                          Continue to Part {selectedPart === 'part1' ? '2' : '3'}
                        </Button>
                      </Col>
                    )}
                  </Row>

                  {/* Submit Exam Button - Always show when at least 1 part is evaluated */}
                  {(partEvaluations.part1 || partEvaluations.part2 || partEvaluations.part3) && !submittedExam && (
                    <Row className="mt-3">
                      <Col>
                        <Button 
                          variant="danger" 
                          size="lg" 
                          className="w-100 py-3"
                          onClick={submitSpeakingExam}
                          disabled={evaluating}
                        >
                          {evaluating ? (
                            <>
                              <Spinner animation="border" size="sm" className="me-2" />
                              Đang xử lý...
                            </>
                          ) : (
                            <>
                              🎯 NỘP BÀI THI SPEAKING
                              {!(partEvaluations.part1 && partEvaluations.part2 && partEvaluations.part3) && (
                                <Badge bg="warning" text="dark" className="ms-2">
                                  Chưa đủ 3 parts
                                </Badge>
                              )}
                            </>
                          )}
                        </Button>
                      </Col>
                    </Row>
                  )}

                  {/* Overall Result Display */}
                  {submittedExam && overallResult && (
                    <Card className="mt-3 border-success">
                      <Card.Header className="bg-success text-white">
                        <h4>🎉 KẾT QUẢ TỔNG KẾT</h4>
                        {overallResult.evaluated_parts < 3 && (
                          <small className="d-block mt-1">
                            ⚠️ Chỉ tính dựa trên {overallResult.evaluated_parts}/3 parts đã hoàn thành
                          </small>
                        )}
                      </Card.Header>
                      <Card.Body>
                        <div className="text-center mb-3">
                          <h1 className="display-3 text-success">{overallResult.overall_band}</h1>
                          <p className="text-muted">IELTS Speaking Band Score</p>
                        </div>
                        
                        <Row className="mb-3">
                          <Col md={4}>
                            <div className={`text-center p-3 border rounded ${overallResult.part_scores.part1 ? '' : 'bg-light'}`}>
                              <h5>Part 1</h5>
                              {overallResult.part_scores.part1 ? (
                                <h3 className="text-primary">{overallResult.part_scores.part1}</h3>
                              ) : (
                                <p className="text-muted mb-0">Chưa làm</p>
                              )}
                            </div>
                          </Col>
                          <Col md={4}>
                            <div className={`text-center p-3 border rounded ${overallResult.part_scores.part2 ? '' : 'bg-light'}`}>
                              <h5>Part 2</h5>
                              {overallResult.part_scores.part2 ? (
                                <h3 className="text-primary">{overallResult.part_scores.part2}</h3>
                              ) : (
                                <p className="text-muted mb-0">Chưa làm</p>
                              )}
                            </div>
                          </Col>
                          <Col md={4}>
                            <div className={`text-center p-3 border rounded ${overallResult.part_scores.part3 ? '' : 'bg-light'}`}>
                              <h5>Part 3</h5>
                              {overallResult.part_scores.part3 ? (
                                <h3 className="text-primary">{overallResult.part_scores.part3}</h3>
                              ) : (
                                <p className="text-muted mb-0">Chưa làm</p>
                              )}
                            </div>
                          </Col>
                        </Row>

                        <h5 className="mb-3">📊 Điểm theo tiêu chí:</h5>
                        <ul className="list-unstyled">
                          <li className="mb-2">
                            <strong>Fluency & Coherence:</strong> {overallResult.criteria_scores.fluency_coherence}
                          </li>
                          <li className="mb-2">
                            <strong>Lexical Resource:</strong> {overallResult.criteria_scores.lexical_resource}
                          </li>
                          <li className="mb-2">
                            <strong>Grammatical Range:</strong> {overallResult.criteria_scores.grammatical_range_accuracy}
                          </li>
                          <li className="mb-2">
                            <strong>Pronunciation:</strong> {overallResult.criteria_scores.pronunciation}
                          </li>
                        </ul>
                      </Card.Body>
                    </Card>
                  )}
                </Col>
              </Row>

              {/* AI Feedback Section */}
              {partEvaluations[selectedPart] && (
                <Card className="border-0 shadow-sm mt-4">
                  <Card.Header className="bg-white border-0 p-4">
                    <Row className="align-items-center">
                      <Col>
                        <h5 className="mb-0">AI Feedback & Analysis</h5>
                      </Col>
                      <Col xs="auto">
                        <Badge bg="success" className="px-3 py-2 fs-6">
                          Overall Score: {partEvaluations[selectedPart].overall_band}
                        </Badge>
                      </Col>
                    </Row>
                  </Card.Header>
                  <Card.Body className="p-4">
                    {/* Criteria Scores */}
                    <Row className="g-3 mb-4">
                      {Object.entries(partEvaluations[selectedPart].criteria_scores).map(([key, value]) => {
                        const labels = {
                          'pronunciation': 'Pronunciation',
                          'fluency_coherence': 'Fluency & Coherence',
                          'lexical_resource': 'Lexical Resource',
                          'grammatical_range_accuracy': 'Grammar & Accuracy'
                        };
                        
                        const colors = {
                          'pronunciation': 'success',
                          'fluency_coherence': 'warning',
                          'lexical_resource': 'success',
                          'grammatical_range_accuracy': 'warning'
                        };
                        
                        return (
                          <Col md={6} lg={3} key={key}>
                            <div className="bg-light p-3 rounded text-center">
                              <h6 className="mb-2 text-dark">{labels[key]}</h6>
                              <div className="mb-2">
                                <ProgressBar 
                                  now={(value / 9) * 100} 
                                  variant={colors[key]}
                                  style={{height: '8px'}}
                                />
                              </div>
                              <div className="h4 mb-1 fw-bold text-dark">{value}</div>
                              <small className="text-muted">{value >= 7 ? 'Good performance' : 'Needs improvement'}</small>
                            </div>
                          </Col>
                        );
                      })}
                    </Row>

                    {/* Transcript Section */}
                    {questionTranscripts[currentQuestion?.id] && (
                      <div className="mb-4">
                        <h6 className="fw-bold mb-3">Your Transcript</h6>
                        <div className="bg-light p-4 rounded">
                          <p className="mb-0" style={{whiteSpace: 'pre-wrap', lineHeight: '1.8'}}>
                            {questionTranscripts[currentQuestion.id]}
                          </p>
                        </div>
                      </div>
                    )}

                    {/* Detailed Suggestions */}
                    <h6 className="fw-bold mb-3">Detailed Suggestions for Improvement</h6>
                    
                    {partEvaluations[selectedPart].areas_for_improvement.slice(0, 3).map((area, idx) => (
                      <div key={idx} className="mb-3 p-3 bg-light rounded">
                        <div className="d-flex gap-2 align-items-start">
                          <div className={`rounded-circle p-2 ${idx === 0 ? 'bg-warning' : idx === 1 ? 'bg-danger' : 'bg-success'}`} style={{width: '32px', height: '32px', flexShrink: 0}}>
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="white">
                              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
                            </svg>
                          </div>
                          <div className="flex-grow-1">
                            <h6 className="fw-bold mb-2">{idx === 0 ? 'Fluency Improvement' : idx === 1 ? 'Pronunciation Correction' : 'Grammar Tip'}</h6>
                            <p className="mb-0 small">{area}</p>
                          </div>
                        </div>
                      </div>
                    ))}

                    {/* Strengths */}
                    <div className="mt-4">
                      <h6 className="fw-bold mb-3 text-success">✓ Strengths</h6>
                      <ul className="list-unstyled">
                        {partEvaluations[selectedPart].strengths.map((strength, idx) => (
                          <li key={idx} className="mb-2 d-flex gap-2">
                            <span className="text-success">✓</span>
                            <span>{strength}</span>
                          </li>
                        ))}
                      </ul>
                    </div>
                  </Card.Body>
                </Card>
              )}
            </Container>
            
            <style>{`
              @keyframes pulse {
                0%, 100% { transform: scale(1); opacity: 1; }
                50% { transform: scale(1.05); opacity: 0.9; }
              }
            `}</style>
          </>
        )}
      </Container>

      {/* Submit Confirmation Modal */}
      <Modal show={showSubmitConfirm} onHide={() => setShowSubmitConfirm(false)} centered>
        <Modal.Header closeButton className="bg-primary text-white">
          <Modal.Title>🎯 Xác nhận nộp bài</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p className="mb-3">Bạn có chắc chắn muốn nộp bài thi Speaking?</p>
          {submitWarningMessage && (
            <Alert variant="warning" className="mb-0">
              <small>⚠️ {submitWarningMessage}</small>
            </Alert>
          )}
          <p className="mt-3 mb-0 small text-muted">
            Sau khi nộp, bạn sẽ nhận được điểm tổng kết!
          </p>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowSubmitConfirm(false)}>
            Hủy
          </Button>
          <Button variant="primary" onClick={handleConfirmSubmit}>
            Xác nhận nộp bài
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
};

export default AttemptSpeakingExamPage;

