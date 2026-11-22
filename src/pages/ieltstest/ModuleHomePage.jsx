import { useParams, useLocation } from "react-router-dom";
import React, { useEffect, useState, useContext } from "react";
import PageHeadingBriefinfo from "../../components/layout/PageHeadingBriefInfo";
import { useAlert } from '../../context/AlertContext';
import {
  Button,
  ButtonGroup,
  Col,
  Container,
  Form,
  Row,
  Stack,
  Card,
  Modal,
  Table,
  Badge,
  Spinner,
  Alert,
  Nav,
  Tab,
  Accordion,
} from "react-bootstrap";
import BookCard from "../../components/ieltstest/BookCard";
import usePublicAxios from "../../utils/usePublicAxios";
import useAxios from "../../utils/useAxios";
import TestTypeContext from "../../context/TestTypeContext";
import { Helmet } from "react-helmet";
import { getExamList, getMyExams } from "../../services/examService";
import { useNavigate } from "react-router-dom";
import ScheduleEventModal from "../../components/dashboard/ScheduleEventModal";
import { getCommentsByExamId, createComment, updateComment, deleteComment } from "../../services/commentService";
import AuthContext from "../../context/AuthContext";

const ModuleHomePage = () => {
  const { showSuccess, showError, showWarning } = useAlert();
  let api;
  if (localStorage.getItem("authTokens")) {
    api = useAxios();
  } else {
    api = usePublicAxios();
  }
  const { module_slug: paramModuleSlug } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  
  // Extract module from pathname if not in params
  // e.g., /ieltstest/listening -> 'listening'
  const getModuleFromPath = () => {
    if (paramModuleSlug) return paramModuleSlug;
    const pathParts = location.pathname.split('/').filter(Boolean);
    const moduleIndex = pathParts.indexOf('ieltstest') + 1;
    return pathParts[moduleIndex] || 'listening'; // default to listening
  };
  
  const module_slug = getModuleFromPath();
  
  const [books, setBooks] = useState(null);
  const [exams, setExams] = useState(null); // New state for exams
  const [testType, setTestType] = useContext(TestTypeContext);
  const [searchQuery, setSearchQuery] = useState("");
  
  // Modal states
  const [showExamModal, setShowExamModal] = useState(false);
  const [selectedExam, setSelectedExam] = useState(null);
  const [activeTab, setActiveTab] = useState('tests'); // 'tests' or 'attempts'
  const [selectedSkillType, setSelectedSkillType] = useState(null);
  const [allExamHistory, setAllExamHistory] = useState([]);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [historyError, setHistoryError] = useState(null);
  
  // Schedule modal states
  const [showScheduleModal, setShowScheduleModal] = useState(false);
  const [examToSchedule, setExamToSchedule] = useState(null);

  // Comment states
  const [comments, setComments] = useState([]);
  const [loadingComments, setLoadingComments] = useState(false);
  const [commentError, setCommentError] = useState(null);
  const [newComment, setNewComment] = useState('');
  const [editingCommentId, setEditingCommentId] = useState(null);
  const [editingCommentContent, setEditingCommentContent] = useState('');

  useEffect(() => {
    let isMounted = true;
    const abortController = new AbortController();
    
    const fetchExams = async () => {
      if (!isMounted) return;
      
      try {
        console.log('Fetching exams for module:', module_slug);
        const response = await getExamList(1, 100);
        
        if (!isMounted) return; // Check again after async operation
        
        console.log('Exams API response:', response);
        
        if (response.code === 200 && response.result) {
          const examsList = response.result.items || [];
          console.log('All exams loaded:', examsList);
          console.log(`Loaded ${examsList.length} ${module_slug} exams`);
          setExams(examsList);
        } else {
          console.warn('Unexpected response code:', response.code);
          setExams([]);
        }
      } catch (error) {
        if (!isMounted) return;
        setExams([]);
        console.error("Error fetching exams:", error);
      }
    };
    
    fetchExams();
    
    return () => {
      isMounted = false;
      abortController.abort();
    };
  }, [module_slug, testType]);

  const getExams = async () => {
    try {
      console.log('Fetching exams for module:', module_slug);
      const response = await getExamList(1, 100);
      console.log('Exams API response:', response);
      
      if (response.code === 200 && response.result) {
        const examsList = response.result.items || [];
        console.log('All exams loaded:', examsList);
        
        // Show all exams for all modules (filter will be done by backend later)
        console.log(`Loaded ${examsList.length} ${module_slug} exams`);
        setExams(examsList);
      } else {
        console.warn('Unexpected response code:', response.code);
        setExams([]);
      }
    } catch (error) {
      setExams([]);
      console.error("Error fetching exams:", error);
    }
  };

  const getBooks = async () => {
    try {
      console.log('Fetching books for module:', module_slug, 'testType:', testType);
      const response = await api.get(module_data[module_slug].api_url, {
        params: {
          // Use the `params` key to include query parameters
          testType: testType,
        },
      });
      console.log('Books API response:', response);
      if (response.status === 200) {
        const allBooks = response.data;
        console.log('All books received:', allBooks);
        const booksWithTests = allBooks.filter((book) => book["tests"].length > 0);
        console.log('Books with tests:', booksWithTests);
        setBooks(booksWithTests);
      }
    } catch (error) {
      console.error("Error fetching books:", error);
      setBooks([]);
    }
  };

  const handleSearchChange = (e) => {
    setSearchQuery(e.target.value);
  };

  const filteredBooks =
    books &&
    books.filter((book) => {
      if (!searchQuery.trim()) return true; // Show all if no search query
      const bookWords = book.name.toLowerCase().split(/\s+/);
      const searchWords = searchQuery.toLowerCase().split(/\s+/);
      return searchWords.every((searchWord) =>
        bookWords.some((bookWord) => bookWord.includes(searchWord))
      );
    });

  const filteredExams =
    exams &&
    exams.filter((exam) => {
      if (!searchQuery.trim()) return true; // Show all if no search query
      const examWords = exam.title.toLowerCase().split(/\s+/);
      const searchWords = searchQuery.toLowerCase().split(/\s+/);
      return searchWords.every((searchWord) =>
        examWords.some((examWord) => examWord.includes(searchWord))
      );
    });

  // Available skill types with icons and colors
  const skillTypes = [
    { type: 'LISTENING', name: 'Listening', icon: 'fe-headphones', color: 'primary', route: 'listening' },
    { type: 'READING', name: 'Reading', icon: 'fe-book-open', color: 'success', route: 'reading' },
    { type: 'WRITING', name: 'Writing', icon: 'fe-edit', color: 'warning', route: 'writing' },
    { type: 'SPEAKING', name: 'Speaking', icon: 'fe-mic', color: 'danger', route: 'speaking' }
  ];

  // Handle click Start Test button - show exam modal with tabs
  const handleStartTestClick = async (exam) => {
    setSelectedExam(exam);
    setShowExamModal(true);
    setActiveTab('tests');
    setSelectedSkillType(null);
    setAllExamHistory([]);
    setHistoryError(null);
    
    // Load all history for this exam
    await loadExamHistory(exam.id);
    
    // Load comments for this exam
    await loadComments(exam.id);
  };

  const handleCloseExamModal = () => {
    setShowExamModal(false);
    setSelectedExam(null);
    setActiveTab('tests');
    setSelectedSkillType(null);
    setAllExamHistory([]);
    setHistoryError(null);
    setComments([]);
    setNewComment('');
    setEditingCommentId(null);
    setEditingCommentContent('');
  };

  // Schedule handlers
  const handleScheduleClick = (exam) => {
    setExamToSchedule(exam);
    setShowScheduleModal(true);
  };

  const handleCloseScheduleModal = () => {
    setShowScheduleModal(false);
    setExamToSchedule(null);
  };

  const handleScheduleSuccess = () => {
    console.log('Schedule created successfully');
    // Trigger calendar refresh by dispatching custom event
    window.dispatchEvent(new CustomEvent('scheduleCreated'));
  };

  // Load all exam history for all skill types
  const loadExamHistory = async (examId) => {
    setLoadingHistory(true);
    setHistoryError(null);
    
    try {
      const response = await getMyExams();
      
      if (response.code === 200 && response.result) {
        // Filter history for current exam, group by skill type
        const history = response.result
          .filter(userExam => userExam.examId === examId)
          .flatMap(userExam => {
            return userExam.parts
              ?.filter(part => part.submitted)
              .map(part => ({
                id: userExam.id,
                startedAt: userExam.startedAt,
                completedAt: userExam.completedAt,
                score: part.score || 0,
                userExamPartId: part.id,
                skillType: part.skillType
              })) || [];
          })
          .sort((a, b) => new Date(b.startedAt) - new Date(a.startedAt));
        
        setAllExamHistory(history);
      } else {
        setAllExamHistory([]);
      }
    } catch (error) {
      console.error('Error loading exam history:', error);
      setHistoryError('Không thể tải lịch sử làm bài. Vui lòng thử lại.');
      setAllExamHistory([]);
    } finally {
      setLoadingHistory(false);
    }
  };

  // Handle start full test
  const handleStartFullTest = () => {
    handleCloseExamModal();
    navigate(`/ieltstest/attempt/fulltest/${selectedExam.id}`);
  };

  // Handle start individual skill test
  const handleStartSkillTest = (skillType) => {
    const skillInfo = skillTypes.find(s => s.type === skillType);
    if (skillInfo) {
      handleCloseExamModal();
      navigate(`/ieltstest/attempt/${skillInfo.route}/${selectedExam.id}`);
    }
  };

  // Handle view result from history
  const handleViewResult = (userExamPartId, score, skillType) => {
    const skillInfo = skillTypes.find(s => s.type === skillType);
    if (skillInfo) {
      handleCloseExamModal();
      // Navigate directly to result page with userExamPartId in URL (same as dashboard)
      navigate(`/ieltstest/${skillInfo.route}/result/${userExamPartId}`, {
        state: { 
          score: score 
        }
      });
    }
  };

  // Format date
  const formatDate = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  // Load comments for exam
  const loadComments = async (examId) => {
    setLoadingComments(true);
    setCommentError(null);
    
    try {
      const response = await getCommentsByExamId(examId);
      
      if (response.code === 200 && response.result) {
        setComments(response.result);
      } else {
        setComments([]);
      }
    } catch (error) {
      console.error('Error loading comments:', error);
      setCommentError('Không thể tải bình luận. Vui lòng thử lại.');
      setComments([]);
    } finally {
      setLoadingComments(false);
    }
  };

  // Handle submit new comment
  const handleSubmitComment = async (e) => {
    e.preventDefault();
    
    if (!newComment.trim()) {
      showWarning('Vui lòng nhập nội dung bình luận');
      return;
    }

    if (!user) {
      showWarning('Vui lòng đăng nhập để bình luận');
      return;
    }

    try {
      const response = await createComment({
        examId: selectedExam.id,
        content: newComment.trim()
      });

      if (response.code === 201) {
        setNewComment('');
        await loadComments(selectedExam.id);
      }
    } catch (error) {
      console.error('Error creating comment:', error);
      showError('Không thể tạo bình luận. Vui lòng thử lại.');
    }
  };

  // Handle edit comment
  const handleEditComment = (comment) => {
    setEditingCommentId(comment.id);
    setEditingCommentContent(comment.content);
  };

  // Handle update comment
  const handleUpdateComment = async (commentId) => {
    if (!editingCommentContent.trim()) {
      showWarning('Vui lòng nhập nội dung bình luận');
      return;
    }

    try {
      const response = await updateComment(commentId, {
        examId: selectedExam.id,
        content: editingCommentContent.trim()
      });

      if (response.code === 200) {
        setEditingCommentId(null);
        setEditingCommentContent('');
        await loadComments(selectedExam.id);
      }
    } catch (error) {
      console.error('Error updating comment:', error);
      showError('Không thể cập nhật bình luận. Vui lòng thử lại.');
    }
  };

  // Handle delete comment
  const handleDeleteComment = async (commentId) => {
    if (!window.confirm('Bạn có chắc chắn muốn xóa bình luận này?')) {
      return;
    }

    try {
      const response = await deleteComment(commentId);

      if (response.code === 200) {
        await loadComments(selectedExam.id);
      }
    } catch (error) {
      console.error('Error deleting comment:', error);
      showError('Không thể xóa bình luận. Vui lòng thử lại.');
    }
  };

  // Cancel edit
  const handleCancelEdit = () => {
    setEditingCommentId(null);
    setEditingCommentContent('');
  };

  // Show loading state
  if (exams === null) {
    return (
      <Container className="my-5 text-center">
        <Spinner animation="border" role="status">
          <span className="visually-hidden">Loading...</span>
        </Spinner>
        <p className="mt-3">Loading exams...</p>
      </Container>
    );
  }

  // Validate module_slug
  if (!module_data[module_slug]) {
    return (
      <Container className="my-5">
        <Alert variant="danger">
          <h4>Invalid Module</h4>
          <p>The requested module "{module_slug}" does not exist.</p>
          <Button variant="primary" onClick={() => navigate('/ieltstest/listening')}>
            Go to Listening Tests
          </Button>
        </Alert>
      </Container>
    );
  }

  return (
    <div>
      <Helmet>
        <title>{module_data[module_slug].seo_title}</title>
        <meta
          name="description"
          content={module_data[module_slug].seo_description}
        />
      </Helmet>
      <PageHeadingBriefinfo
        pagetitle={module_data[module_slug].page_title}
        briefinfo={module_data[module_slug].seo_description}
        color={`bg-${module_slug}`}
      />
      <div className="border-bottom bg-white">
        <Container>
          <Stack direction="horizontal" gap={2}>
            <div className="w-100">
              <div className="d-lg-block">
                <Form className="d-flex align-items-center">
                  <span className="position-absolute ps-3 search-icon">
                    <i className="fe fe-search text-muted"></i>
                  </span>
                  <Form.Control
                    type="search"
                    placeholder="Search Exams"
                    className="form-control form-control-sm ps-6 py-2"
                    onChange={handleSearchChange}
                  />
                </Form>
              </div>
            </div>
          </Stack>
        </Container>
      </div>
      <Container className="p-3 app">
        <Row>
          {/* Exam list for all modules */}
          {filteredExams && filteredExams.map((exam) => (
            <Col xs={12} md={6} lg={4} key={exam.id} className="mb-3">
              <Card className="h-100 shadow-sm">
                {exam.imageUrl ? (
                  <Card.Img 
                    variant="top" 
                    src={exam.imageUrl} 
                    alt={exam.title}
                    style={{ height: '200px', objectFit: 'cover' }}
                  />
                ) : (
                  <div 
                    style={{ 
                      height: '200px', 
                      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      position: 'relative',
                      overflow: 'hidden'
                    }}
                  >
                    <div style={{ position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, opacity: 0.1 }}>
                      <i className="fe fe-book-open" style={{ fontSize: '120px', color: 'white', position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)' }}></i>
                    </div>
                    <div className="text-center" style={{ zIndex: 1 }}>
                      <i className="fe fe-file-text text-white mb-2" style={{ fontSize: '48px' }}></i>
                      <h5 className="text-white fw-bold mb-0">IELTS Exam</h5>
                    </div>
                  </div>
                )}
                <Card.Body>
                  <Card.Title className="text-dark fw-bold">{exam.title}</Card.Title>
                  <Card.Text className="text-muted">
                    {exam.description || 'Complete IELTS ' + module_slug + ' practice test'}
                  </Card.Text>
                  <div className="mb-2">
                    <small className="text-muted">
                      <i className="fe fe-clock me-1"></i>
                      Duration: {exam.totalDuration} minutes
                    </small>
                  </div>
                </Card.Body>
                <Card.Footer className="bg-white border-top-0">
                  <div className="d-grid gap-2">
                    <Button 
                      variant="primary"
                      onClick={() => handleStartTestClick(exam)}
                    >
                      <i className="fe fe-play me-2"></i>
                      Start Test
                    </Button>
                    <Button 
                      variant="outline-primary"
                      onClick={() => handleScheduleClick(exam)}
                    >
                      <i className="fe fe-calendar me-2"></i>
                      Lên lịch thi
                    </Button>
                  </div>
                </Card.Footer>
              </Card>
            </Col>
          ))}

          {/* No exams found */}
          {exams && exams.length === 0 && (
            <Col xs={12} className="text-center my-5">
              <Alert variant="info">
                <h4>No Exams Available</h4>
                <p>There are currently no {module_slug} exams available. Please check back later.</p>
              </Alert>
            </Col>
          )}
          
          {/* No search results */}
          {exams && exams.length > 0 && filteredExams && filteredExams.length === 0 && (
            <Col xs={12} className="text-center my-5">
              <Alert variant="warning">
                <h4>No Results Found</h4>
                <p>No exams match your search query: <strong>"{searchQuery}"</strong></p>
                <Button variant="primary" onClick={() => setSearchQuery('')}>Clear Search</Button>
              </Alert>
            </Col>
          )}
        </Row>
      </Container>

      {/* Exam Parts Selection Modal with Tabs */}
      <Modal 
        show={showExamModal} 
        onHide={handleCloseExamModal} 
        size="xl"
        centered
      >
        <Modal.Header closeButton className="border-0 pb-2">
          <Modal.Title className="w-100">
            <div className="text-center w-100">
              <h4 className="fw-bold mb-1">{selectedExam?.title || 'English Test'}</h4>
              <small className="text-muted">
                <i className="fe fe-clock me-1"></i>
                Duration: {selectedExam?.totalDuration} minutes
              </small>
            </div>
          </Modal.Title>
        </Modal.Header>
        
        <Modal.Body className="px-0 py-0" style={{ minHeight: '500px' }}>
          <Tab.Container activeKey={activeTab} onSelect={(k) => setActiveTab(k)}>
            {/* Tabs Navigation */}
            <Nav variant="tabs" className="px-4 bg-light border-bottom" style={{ borderBottom: '2px solid #dee2e6' }}>
              <Nav.Item>
                <Nav.Link 
                  eventKey="tests" 
                  className="px-4 py-3 fw-bold"
                  style={{
                    color: activeTab === 'tests' ? '#0d6efd' : '#6c757d',
                    borderBottom: activeTab === 'tests' ? '3px solid #0d6efd' : 'none',
                    backgroundColor: activeTab === 'tests' ? 'white' : 'transparent'
                  }}
                >
                  <i className="fe fe-grid me-2"></i>
                  Tests
                </Nav.Link>
              </Nav.Item>
              <Nav.Item>
                <Nav.Link 
                  eventKey="attempts" 
                  className="px-4 py-3 fw-bold"
                  style={{
                    color: activeTab === 'attempts' ? '#0d6efd' : '#6c757d',
                    borderBottom: activeTab === 'attempts' ? '3px solid #0d6efd' : 'none',
                    backgroundColor: activeTab === 'attempts' ? 'white' : 'transparent'
                  }}
                >
                  <i className="fe fe-clock me-2"></i>
                  Your Attempts
                  {allExamHistory.length > 0 && (
                    <Badge bg="danger" className="ms-2 fw-bold">{allExamHistory.length}</Badge>
                  )}
                </Nav.Link>
              </Nav.Item>
              <Nav.Item>
                <Nav.Link 
                  eventKey="comments" 
                  className="px-4 py-3 fw-bold"
                  style={{
                    color: activeTab === 'comments' ? '#0d6efd' : '#6c757d',
                    borderBottom: activeTab === 'comments' ? '3px solid #0d6efd' : 'none',
                    backgroundColor: activeTab === 'comments' ? 'white' : 'transparent'
                  }}
                >
                  <i className="fe fe-message-square me-2"></i>
                  Comments
                  {comments.length > 0 && (
                    <Badge bg="info" className="ms-2 fw-bold">{comments.length}</Badge>
                  )}
                </Nav.Link>
              </Nav.Item>
            </Nav>

            <Tab.Content className="p-4" style={{ backgroundColor: '#f8f9fa' }}>
              {/* Tab 1: Tests - Full Test or Individual Parts */}
              <Tab.Pane eventKey="tests">
                <div>
                  {/* Full Test Option */}
                  <div className="mb-4">
                    <Card 
                      className="border-3 shadow-lg"
                      style={{ 
                        cursor: 'pointer',
                        transition: 'all 0.3s ease',
                        borderRadius: '16px',
                        borderColor: '#0d6efd',
                        backgroundColor: 'white'
                      }}
                      onClick={handleStartFullTest}
                      onMouseEnter={(e) => {
                        e.currentTarget.style.transform = 'scale(1.02)';
                        e.currentTarget.style.boxShadow = '0 8px 24px rgba(13, 110, 253, 0.3)';
                      }}
                      onMouseLeave={(e) => {
                        e.currentTarget.style.transform = 'scale(1)';
                        e.currentTarget.style.boxShadow = '';
                      }}
                    >
                      <Card.Body className="p-4">
                        <Row className="align-items-center g-3">
                          <Col lg={2} md={3} sm={12} className="text-center">
                            <div 
                              className="d-flex align-items-center justify-content-center mx-auto"
                              style={{
                                width: '90px',
                                height: '90px',
                                borderRadius: '50%',
                                background: 'linear-gradient(135deg, #0d6efd 0%, #0a58ca 100%)',
                                boxShadow: '0 4px 12px rgba(13, 110, 253, 0.3)'
                              }}
                            >
                              <i className="fe fe-layers text-white" style={{ fontSize: '42px' }}></i>
                            </div>
                          </Col>
                          <Col lg={7} md={6} sm={12}>
                            <h3 className="fw-bold mb-2" style={{ color: '#0d6efd' }}>
                              <i className="fe fe-award me-2"></i>
                              Full Test
                            </h3>
                            <p className="mb-3" style={{ color: '#495057', fontSize: '15px' }}>
                              Complete all 4 sections in one session - Listening, Reading, Writing, Speaking
                            </p>
                            <div className="d-flex flex-wrap gap-2">
                              <Badge bg="primary" className="px-3 py-2 fw-bold" style={{ fontSize: '12px' }}>
                                <i className="fe fe-headphones me-1"></i> Listening
                              </Badge>
                              <Badge bg="success" className="px-3 py-2 fw-bold" style={{ fontSize: '12px' }}>
                                <i className="fe fe-book-open me-1"></i> Reading
                              </Badge>
                              <Badge bg="warning" text="dark" className="px-3 py-2 fw-bold" style={{ fontSize: '12px' }}>
                                <i className="fe fe-edit me-1"></i> Writing
                              </Badge>
                              <Badge bg="danger" className="px-3 py-2 fw-bold" style={{ fontSize: '12px' }}>
                                <i className="fe fe-mic me-1"></i> Speaking
                              </Badge>
                            </div>
                          </Col>
                          <Col lg={3} md={3} sm={12} className="text-center">
                            <Button 
                              variant="primary" 
                              size="lg"
                              className="px-4 py-3 fw-bold w-100"
                              style={{
                                fontSize: '16px',
                                borderRadius: '12px',
                                boxShadow: '0 4px 12px rgba(13, 110, 253, 0.2)'
                              }}
                            >
                              <i className="fe fe-play me-2"></i>
                              Start Now
                            </Button>
                          </Col>
                        </Row>
                      </Card.Body>
                    </Card>
                  </div>

                  {/* Divider */}
                  <div className="position-relative text-center my-4">
                    <hr style={{ borderTop: '2px solid #dee2e6' }} />
                    <Badge 
                      bg="dark" 
                      className="px-4 py-2 position-absolute fw-bold" 
                      style={{ 
                        top: '-18px', 
                        left: '50%', 
                        transform: 'translateX(-50%)',
                        fontSize: '13px',
                        letterSpacing: '1px'
                      }}
                    >
                      OR
                    </Badge>
                  </div>

                  {/* Individual Parts */}
                  <div>
                    <div className="text-center mb-4">
                      <h4 className="fw-bold mb-2" style={{ color: '#212529' }}>
                        <i className="fe fe-sliders me-2 text-primary"></i>
                        Practice Individual Sections
                      </h4>
                      <p className="text-muted mb-0" style={{ fontSize: '14px' }}>
                        Choose a specific section to practice separately
                      </p>
                    </div>
                    <Row className="g-3">
                      {skillTypes.map((skill) => (
                        <Col xs={12} sm={6} md={3} key={skill.type}>
                          <Card 
                            className={`h-100 border-3 shadow`}
                            style={{
                              cursor: 'pointer',
                              transition: 'all 0.3s ease',
                              borderRadius: '16px',
                              borderColor: `var(--bs-${skill.color})`,
                              backgroundColor: 'white'
                            }}
                            onClick={() => handleStartSkillTest(skill.type)}
                            onMouseEnter={(e) => {
                              e.currentTarget.style.transform = 'translateY(-8px)';
                              e.currentTarget.style.boxShadow = `0 8px 24px rgba(var(--bs-${skill.color}-rgb), 0.3)`;
                            }}
                            onMouseLeave={(e) => {
                              e.currentTarget.style.transform = 'translateY(0)';
                              e.currentTarget.style.boxShadow = '';
                            }}
                          >
                            <Card.Body className="text-center p-4">
                              <div 
                                className="mb-3 d-flex align-items-center justify-content-center mx-auto"
                                style={{
                                  width: '75px',
                                  height: '75px',
                                  borderRadius: '50%',
                                  background: `linear-gradient(135deg, var(--bs-${skill.color}) 0%, var(--bs-${skill.color}) 100%)`,
                                  boxShadow: `0 4px 12px rgba(var(--bs-${skill.color}-rgb), 0.3)`
                                }}
                              >
                                <i 
                                  className={`fe ${skill.icon} text-white`} 
                                  style={{ fontSize: '36px' }}
                                ></i>
                              </div>
                              <h5 className="fw-bold mb-2" style={{ color: '#212529' }}>{skill.name}</h5>
                              <Badge 
                                bg={skill.color} 
                                className="px-3 py-2 fw-bold" 
                                style={{ fontSize: '11px', letterSpacing: '0.5px' }}
                              >
                                {skill.type}
                              </Badge>
                            </Card.Body>
                            <div 
                              className={`py-3 text-center border-top border-3`}
                              style={{ 
                                backgroundColor: `var(--bs-${skill.color})`,
                                borderTopColor: `var(--bs-${skill.color})!important`
                              }}
                            >
                              <small className="text-white fw-bold" style={{ fontSize: '12px' }}>
                                <i className="fe fe-play me-1"></i>
                                Start Test
                              </small>
                            </div>
                          </Card>
                        </Col>
                      ))}
                    </Row>
                  </div>
                </div>
              </Tab.Pane>

              {/* Tab 2: Your Attempts - History */}
              <Tab.Pane eventKey="attempts">
                <div>
                  {loadingHistory ? (
                    <div className="text-center py-5">
                      <Spinner animation="border" role="status" variant="primary" style={{ width: '3rem', height: '3rem' }}>
                        <span className="visually-hidden">Loading...</span>
                      </Spinner>
                      <p className="mt-3 fw-bold" style={{ color: '#495057' }}>Loading your test history...</p>
                    </div>
                  ) : historyError ? (
                    <Alert variant="danger" className="border-3 shadow-sm">
                      <i className="fe fe-alert-circle me-2"></i>
                      <strong>{historyError}</strong>
                    </Alert>
                  ) : allExamHistory.length === 0 ? (
                    <div className="text-center py-5">
                      <div 
                        className="d-flex align-items-center justify-content-center mb-4 mx-auto"
                        style={{
                          width: '120px',
                          height: '120px',
                          borderRadius: '50%',
                          background: 'linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%)',
                          border: '4px solid #2196f3'
                        }}
                      >
                        <i className="fe fe-inbox text-primary" style={{ fontSize: '56px' }}></i>
                      </div>
                      <h4 className="fw-bold mb-2" style={{ color: '#212529' }}>No Test History</h4>
                      <p className="text-muted mb-4" style={{ fontSize: '15px' }}>
                        You haven't taken this test yet. Start your first attempt now!
                      </p>
                      <Button 
                        variant="primary" 
                        onClick={() => setActiveTab('tests')}
                        className="px-5 py-3 fw-bold"
                        size="lg"
                        style={{ borderRadius: '12px', fontSize: '16px' }}
                      >
                        <i className="fe fe-play me-2"></i>
                        Start Test Now
                      </Button>
                    </div>
                  ) : (
                    <>
                      {/* Statistics Summary */}
                      <Row className="mb-4 g-3">
                        <Col md={4}>
                          <Card className="border-0 shadow-sm h-100" style={{ 
                            borderLeft: '4px solid #28a745',
                            backgroundColor: '#f1f9f3'
                          }}>
                            <Card.Body className="text-center py-4">
                              <i className="fe fe-trending-up text-success mb-2" style={{ fontSize: '40px' }}></i>
                              <h3 className="fw-bold text-success mb-1" style={{ fontSize: '32px' }}>
                                {Math.max(...allExamHistory.map(h => h.score)).toFixed(1)}
                              </h3>
                              <small className="fw-bold text-uppercase" style={{ color: '#495057', letterSpacing: '0.5px' }}>
                                Highest Score
                              </small>
                            </Card.Body>
                          </Card>
                        </Col>
                        <Col md={4}>
                          <Card className="border-0 shadow-sm h-100" style={{ 
                            borderLeft: '4px solid #0d6efd',
                            backgroundColor: '#e7f3ff'
                          }}>
                            <Card.Body className="text-center py-4">
                              <i className="fe fe-bar-chart-2 text-primary mb-2" style={{ fontSize: '40px' }}></i>
                              <h3 className="fw-bold text-primary mb-1" style={{ fontSize: '32px' }}>
                                {(allExamHistory.reduce((sum, h) => sum + h.score, 0) / allExamHistory.length).toFixed(1)}
                              </h3>
                              <small className="fw-bold text-uppercase" style={{ color: '#495057', letterSpacing: '0.5px' }}>
                                Average Score
                              </small>
                            </Card.Body>
                          </Card>
                        </Col>
                        <Col md={4}>
                          <Card className="border-0 shadow-sm h-100" style={{ 
                            borderLeft: '4px solid #17a2b8',
                            backgroundColor: '#e8f8fa'
                          }}>
                            <Card.Body className="text-center py-4">
                              <i className="fe fe-check-circle text-info mb-2" style={{ fontSize: '40px' }}></i>
                              <h3 className="fw-bold text-info mb-1" style={{ fontSize: '32px' }}>
                                {allExamHistory.length}
                              </h3>
                              <small className="fw-bold text-uppercase" style={{ color: '#495057', letterSpacing: '0.5px' }}>
                                Total Attempts
                              </small>
                            </Card.Body>
                          </Card>
                        </Col>
                      </Row>

                      {/* History Accordion by Skill Type */}
                      <Accordion defaultActiveKey="0" className="shadow-sm">
                        {skillTypes.map((skill, index) => {
                          const skillHistory = allExamHistory.filter(h => h.skillType === skill.type);
                          
                          if (skillHistory.length === 0) return null;
                          
                          return (
                            <Accordion.Item 
                              eventKey={index.toString()} 
                              key={skill.type}
                              className="border-3 mb-3"
                              style={{ 
                                borderRadius: '12px',
                                overflow: 'hidden',
                                borderColor: `var(--bs-${skill.color})`,
                                backgroundColor: 'white'
                              }}
                            >
                              <Accordion.Header 
                                style={{ 
                                  backgroundColor: `var(--bs-${skill.color}-bg-subtle)`,
                                  borderBottom: `3px solid var(--bs-${skill.color})`
                                }}
                              >
                                <div className="w-100 d-flex justify-content-between align-items-center pe-3">
                                  <div>
                                    <i className={`fe ${skill.icon} text-${skill.color} me-2`} style={{ fontSize: '20px' }}></i>
                                    <strong style={{ fontSize: '16px', color: '#212529' }}>{skill.name} Test</strong>
                                  </div>
                                  <Badge bg={skill.color} className="px-3 py-2 fw-bold" style={{ fontSize: '13px' }}>
                                    {skillHistory.length} {skillHistory.length === 1 ? 'attempt' : 'attempts'}
                                  </Badge>
                                </div>
                              </Accordion.Header>
                              <Accordion.Body className="p-0">
                                <Table hover responsive className="mb-0" style={{ fontSize: '14px' }}>
                                  <thead style={{ backgroundColor: `var(--bs-${skill.color})`, color: 'white' }}>
                                    <tr>
                                      <th className="text-center py-3 fw-bold" style={{ width: '70px' }}>#</th>
                                      <th className="py-3 fw-bold">Date & Time</th>
                                      <th className="text-center py-3 fw-bold" style={{ width: '130px' }}>Score</th>
                                      <th className="text-center py-3 fw-bold" style={{ width: '160px' }}>Action</th>
                                    </tr>
                                  </thead>
                                  <tbody style={{ backgroundColor: 'white' }}>
                                    {skillHistory.map((history, idx) => (
                                      <tr key={history.id} style={{ borderBottom: '1px solid #dee2e6' }}>
                                        <td className="text-center fw-bold py-3" style={{ color: '#495057' }}>{idx + 1}</td>
                                        <td className="py-3">
                                          <i className="fe fe-calendar me-2" style={{ color: '#6c757d' }}></i>
                                          <strong style={{ color: '#212529' }}>{formatDate(history.startedAt)}</strong>
                                        </td>
                                        <td className="text-center py-3">
                                          <Badge 
                                            bg={history.score >= 7 ? 'success' : history.score >= 5 ? 'warning' : 'danger'}
                                            className="px-4 py-2 fw-bold"
                                            style={{ fontSize: '16px' }}
                                          >
                                            {history.score.toFixed(1)}
                                          </Badge>
                                        </td>
                                        <td className="text-center py-3">
                                          <Button
                                            variant={skill.color}
                                            size="sm"
                                            className="px-3 py-2 fw-bold"
                                            onClick={() => handleViewResult(history.userExamPartId, history.score, history.skillType)}
                                            style={{ borderRadius: '8px' }}
                                          >
                                            <i className="fe fe-eye me-1"></i>
                                            View Result
                                          </Button>
                                        </td>
                                      </tr>
                                    ))}
                                  </tbody>
                                </Table>
                              </Accordion.Body>
                            </Accordion.Item>
                          );
                        })}
                      </Accordion>
                    </>
                  )}
                </div>
              </Tab.Pane>

              {/* Tab 3: Comments */}
              <Tab.Pane eventKey="comments">
                <div>
                  {loadingComments ? (
                    <div className="text-center py-5">
                      <Spinner animation="border" role="status" variant="primary" style={{ width: '3rem', height: '3rem' }}>
                        <span className="visually-hidden">Loading...</span>
                      </Spinner>
                      <p className="mt-3 fw-bold" style={{ color: '#495057' }}>Loading comments...</p>
                    </div>
                  ) : commentError ? (
                    <Alert variant="danger" className="border-3 shadow-sm">
                      <i className="fe fe-alert-circle me-2"></i>
                      <strong>{commentError}</strong>
                    </Alert>
                  ) : (
                    <>
                      {/* New Comment Form */}
                      {user ? (
                        <Card className="mb-4 border-0 shadow-sm">
                          <Card.Body className="p-4">
                            <h5 className="fw-bold mb-3" style={{ color: '#212529' }}>
                              <i className="fe fe-edit me-2 text-primary"></i>
                              Write a Comment
                            </h5>
                            <Form onSubmit={handleSubmitComment}>
                              <Form.Group className="mb-3">
                                <Form.Control
                                  as="textarea"
                                  rows={4}
                                  placeholder="Share your thoughts about this exam..."
                                  value={newComment}
                                  onChange={(e) => setNewComment(e.target.value)}
                                  style={{ 
                                    borderRadius: '12px',
                                    border: '2px solid #e3e6ea',
                                    fontSize: '15px'
                                  }}
                                />
                              </Form.Group>
                              <div className="text-end">
                                <Button 
                                  type="submit" 
                                  variant="primary"
                                  className="px-4 py-2 fw-bold"
                                  style={{ borderRadius: '8px' }}
                                  disabled={!newComment.trim()}
                                >
                                  <i className="fe fe-send me-2"></i>
                                  Post Comment
                                </Button>
                              </div>
                            </Form>
                          </Card.Body>
                        </Card>
                      ) : (
                        <Alert variant="info" className="mb-4 border-0 shadow-sm">
                          <i className="fe fe-info me-2"></i>
                          Please <strong>login</strong> to post a comment.
                        </Alert>
                      )}

                      {/* Comments List */}
                      {comments.length === 0 ? (
                        <div className="text-center py-5">
                          <div 
                            className="d-flex align-items-center justify-content-center mb-4 mx-auto"
                            style={{
                              width: '120px',
                              height: '120px',
                              borderRadius: '50%',
                              background: 'linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%)',
                              border: '4px solid #2196f3'
                            }}
                          >
                            <i className="fe fe-message-square text-primary" style={{ fontSize: '56px' }}></i>
                          </div>
                          <h4 className="fw-bold mb-2" style={{ color: '#212529' }}>No Comments Yet</h4>
                          <p className="text-muted mb-0" style={{ fontSize: '15px' }}>
                            Be the first to share your thoughts about this exam!
                          </p>
                        </div>
                      ) : (
                        <div>
                          <h5 className="fw-bold mb-4" style={{ color: '#212529' }}>
                            <i className="fe fe-message-square me-2 text-primary"></i>
                            All Comments ({comments.length})
                          </h5>
                          {comments.map((comment) => (
                            <Card key={comment.id} className="mb-3 border-0 shadow-sm">
                              <Card.Body className="p-4">
                                <div className="d-flex align-items-start">
                                  {/* User Avatar */}
                                  <div className="me-3">
                                    {comment.userAvatar ? (
                                      <img 
                                        src={comment.userAvatar} 
                                        alt={comment.username}
                                        style={{
                                          width: '50px',
                                          height: '50px',
                                          borderRadius: '50%',
                                          objectFit: 'cover',
                                          border: '3px solid #e3e6ea'
                                        }}
                                      />
                                    ) : (
                                      <div 
                                        className="d-flex align-items-center justify-content-center"
                                        style={{
                                          width: '50px',
                                          height: '50px',
                                          borderRadius: '50%',
                                          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                                          border: '3px solid #e3e6ea'
                                        }}
                                      >
                                        <i className="fe fe-user text-white" style={{ fontSize: '24px' }}></i>
                                      </div>
                                    )}
                                  </div>

                                  {/* Comment Content */}
                                  <div className="flex-grow-1">
                                    <div className="d-flex justify-content-between align-items-start mb-2">
                                      <div>
                                        <h6 className="fw-bold mb-1" style={{ color: '#212529' }}>
                                          {comment.username}
                                        </h6>
                                        <small className="text-muted">
                                          <i className="fe fe-clock me-1"></i>
                                          {formatDate(comment.createdAt)}
                                          {comment.updatedAt && comment.updatedAt !== comment.createdAt && (
                                            <span className="ms-2">
                                              <i className="fe fe-edit-2 me-1"></i>
                                              (edited)
                                            </span>
                                          )}
                                        </small>
                                      </div>
                                      
                                      {/* Edit/Delete buttons for owner */}
                                      {user && user.id === comment.userId && (
                                        <div>
                                          {editingCommentId === comment.id ? (
                                            <>
                                              <Button
                                                variant="success"
                                                size="sm"
                                                className="me-2 px-3"
                                                onClick={() => handleUpdateComment(comment.id)}
                                              >
                                                <i className="fe fe-check me-1"></i>
                                                Save
                                              </Button>
                                              <Button
                                                variant="secondary"
                                                size="sm"
                                                onClick={handleCancelEdit}
                                              >
                                                <i className="fe fe-x me-1"></i>
                                                Cancel
                                              </Button>
                                            </>
                                          ) : (
                                            <>
                                              <Button
                                                variant="outline-primary"
                                                size="sm"
                                                className="me-2"
                                                onClick={() => handleEditComment(comment)}
                                              >
                                                <i className="fe fe-edit me-1"></i>
                                                Edit
                                              </Button>
                                              <Button
                                                variant="outline-danger"
                                                size="sm"
                                                onClick={() => handleDeleteComment(comment.id)}
                                              >
                                                <i className="fe fe-trash-2 me-1"></i>
                                                Delete
                                              </Button>
                                            </>
                                          )}
                                        </div>
                                      )}
                                    </div>

                                    {/* Comment Text */}
                                    {editingCommentId === comment.id ? (
                                      <Form.Control
                                        as="textarea"
                                        rows={3}
                                        value={editingCommentContent}
                                        onChange={(e) => setEditingCommentContent(e.target.value)}
                                        className="mt-2"
                                        style={{ 
                                          borderRadius: '8px',
                                          border: '2px solid #0d6efd'
                                        }}
                                      />
                                    ) : (
                                      <p className="mb-0" style={{ 
                                        fontSize: '15px', 
                                        color: '#495057',
                                        lineHeight: '1.6',
                                        whiteSpace: 'pre-wrap'
                                      }}>
                                        {comment.content}
                                      </p>
                                    )}
                                  </div>
                                </div>
                              </Card.Body>
                            </Card>
                          ))}
                        </div>
                      )}
                    </>
                  )}
                </div>
              </Tab.Pane>
            </Tab.Content>
          </Tab.Container>
        </Modal.Body>
        
        <Modal.Footer className="border-top-3 bg-light py-3" style={{ borderTopColor: '#dee2e6!important' }}>
          <Button 
            variant="outline-dark" 
            onClick={handleCloseExamModal}
            className="px-4 py-2 fw-bold"
            style={{ borderRadius: '8px', borderWidth: '2px' }}
          >
            <i className="fe fe-x me-2"></i>
            Close
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Schedule Modal */}
      {examToSchedule && (
        <ScheduleEventModal
          show={showScheduleModal}
          handleClose={handleCloseScheduleModal}
          exam={examToSchedule}
          onSuccess={handleScheduleSuccess}
        />
      )}
    </div>
  );
};
const module_data = {
  listening: {
    title: "Listening Test",
    page_title: "Listening Books",
    page_description:
      "Improve your English listening skills by practicing with mock tests that closely resemble the actual IELTS listening tests.",
    api_url: "/ieltstest/listening/",
    seo_title: "Free Real IELTS Listening Practice Tests | KeenIELTS",
    seo_description:
      "Boost your IELTS Listening skills with EngTech! Access a variety of authentic IELTS Listening tests for free. Experience real test conditions, get instant results, and improve your score. Start your effective IELTS preparation journey today and achieve the score you need!",
  },
  reading: {
    title: "Reading Test",
    page_title: "Reading Books",
    page_description:
      "Improve your English reading skills by practicing with mock tests that closely resemble the actual IELTS reading tests.",
    api_url: "/ieltstest/reading/",
    seo_title: "Free Real IELTS Reading Practice Tests | KeenIELTS",
    seo_description:
      "Boost your IELTS Reading skills with EngTech! Access a variety of authentic IELTS Reading tests for free. Experience real test conditions, get instant results, and improve your score. Start your effective IELTS preparation journey today and achieve the score you need!",
  },
  writing: {
    title: "Writing Test",
    page_title: "Writing Books",
    page_description:
      "Improve your English writing skills by practicing with mock tests that closely resemble the actual IELTS writing tests.",
    api_url: "/ieltstest/writing/",
    seo_title: "Free Real IELTS Writing Practice Tests | KeenIELTS",
    seo_description:
      "Boost your IELTS Writing skills with EngTech! Access a variety of authentic IELTS Writing tests for free. Experience real test conditions, get instant results, and improve your score. Start your effective IELTS preparation journey today and achieve the score you need!",
  },
  speaking: {
    title: "Speaking Test",
    page_title: "Speaking Books",
    page_description:
      "Improve your English speaking skills by practicing with mock tests that closely resemble the actual IELTS speaking tests.",
    api_url: "/ieltstest/speaking/",
    seo_title: "Free Real IELTS Speaking Practice Tests | KeenIELTS",
    seo_description:
      "Boost your IELTS Speaking skills with EngTech! Access a variety of authentic IELTS Speaking tests for free. Experience real test conditions, get instant results, and improve your score. Start your effective IELTS preparation journey today and achieve the score you need!",
  },
  fulltest: {
    title: "Full Test",
    page_title: "Full Test Books",
    page_description: "Practice full IELTS test with answers and explanations.",
    api_url: "/ieltstest/fulltest/",
    seo_title: "Free Real IELTS Complete Practice Tests | KeenIELTS",
    seo_description:
      "Boost your IELTS exam skills with KeenIELTS! Access a variety of authentic IELTS practice tests for free. Experience real test conditions, get instant results, and improve your score. Start your effective IELTS preparation journey today and achieve the score you need!",
  },
};

export default ModuleHomePage;
