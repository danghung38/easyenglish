import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Badge, Button, Spinner, Alert, Table, Pagination, Form, InputGroup } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { FiArrowLeft, FiCheckCircle, FiClock, FiSearch, FiFilter } from 'react-icons/fi';
import { getMyExams } from '../../services/examService';

const TestHistoryPage = () => {
  const navigate = useNavigate();
  const [userExams, setUserExams] = useState([]);
  const [filteredExams, setFilteredExams] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage] = useState(10);
  
  // Filter states
  const [filters, setFilters] = useState({
    testType: 'all',
    skillType: 'all',
    status: 'all',
    searchText: ''
  });

  useEffect(() => {
    fetchUserExams();
  }, []);

  useEffect(() => {
    applyFilters();
  }, [userExams, filters]);

  const fetchUserExams = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getMyExams();
      
      if (response.code === 200 && response.result) {
        // Sort by most recent first
        const sortedExams = response.result.sort((a, b) => 
          new Date(b.startedAt) - new Date(a.startedAt)
        );
        setUserExams(sortedExams);
        setFilteredExams(sortedExams);
      } else {
        setError('Không thể tải lịch sử làm bài');
      }
    } catch (err) {
      console.error('Error fetching user exams:', err);
      setError('Không thể tải lịch sử làm bài. Vui lòng thử lại sau.');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getSkillBadgeVariant = (skillType) => {
    const variants = {
      LISTENING: 'primary',
      READING: 'success',
      WRITING: 'warning',
      SPEAKING: 'danger'
    };
    return variants[skillType] || 'secondary';
  };

  const calculateOverallBand = (parts) => {
    if (!parts || parts.length === 0) return 'N/A';
    
    const completedParts = parts.filter(p => p.submitted && p.score !== null);
    if (completedParts.length === 0) return 'N/A';
    
    const totalScore = completedParts.reduce((sum, part) => sum + (part.score || 0), 0);
    const avgScore = totalScore / completedParts.length;
    return avgScore.toFixed(1);
  };

  const getTestType = (parts) => {
    if (!parts || parts.length === 0) return 'Unknown';
    
    const skillTypes = [...new Set(parts.map(p => p.skillType))];
    
    if (skillTypes.length >= 4) {
      return '🎯 FULL TEST';
    } else if (skillTypes.length === 1) {
      return skillTypes[0];
    } else {
      return 'Partial Test';
    }
  };

  const applyFilters = () => {
    let filtered = [...userExams];

    if (filters.testType !== 'all') {
      filtered = filtered.filter(exam => {
        const testType = getTestType(exam.parts);
        if (filters.testType === 'full') {
          return testType === '🎯 FULL TEST';
        } else if (filters.testType === 'single') {
          return testType !== '🎯 FULL TEST' && testType !== 'Partial Test';
        }
        return true;
      });
    }

    if (filters.skillType !== 'all') {
      filtered = filtered.filter(exam => {
        const parts = exam.parts || [];
        return parts.some(part => part.skillType === filters.skillType);
      });
    }

    if (filters.status !== 'all') {
      filtered = filtered.filter(exam => {
        const parts = exam.parts || [];
        const allCompleted = parts.length > 0 && parts.every(p => p.submitted);
        return filters.status === 'completed' ? allCompleted : !allCompleted;
      });
    }

    if (filters.searchText.trim()) {
      const searchLower = filters.searchText.toLowerCase();
      filtered = filtered.filter(exam => {
        const testType = getTestType(exam.parts);
        const dateStr = formatDate(exam.startedAt);
        return testType.toLowerCase().includes(searchLower) || 
               dateStr.toLowerCase().includes(searchLower);
      });
    }

    setFilteredExams(filtered);
    setCurrentPage(1);
  };

  const handleFilterChange = (filterName, value) => {
    setFilters(prev => ({
      ...prev,
      [filterName]: value
    }));
  };

  const resetFilters = () => {
    setFilters({
      testType: 'all',
      skillType: 'all',
      status: 'all',
      searchText: ''
    });
  };

  const handleViewResult = (userExam) => {
    const parts = userExam.parts || [];
    const skillTypes = [...new Set(parts.map(p => p.skillType))];
    
    // Full test
    if (skillTypes.length >= 4) {
      navigate(`/ieltstest/fulltest/result/${userExam.id}`);
    } 
    // Single skill
    else if (skillTypes.length === 1 && parts.length > 0) {
      const part = parts[0];
      const skill = part.skillType.toLowerCase();
      navigate(`/ieltstest/${skill}/result/${part.id}`);
    }
    // Multiple skills but not full test - go to first completed part
    else {
      const completedPart = parts.find(p => p.submitted);
      if (completedPart) {
        const skill = completedPart.skillType.toLowerCase();
        navigate(`/ieltstest/${skill}/result/${completedPart.id}`);
      }
    }
  };

  if (loading) {
    return (
      <Container className="py-5">
        <div className="text-center">
          <Spinner animation="border" variant="primary" style={{ width: '4rem', height: '4rem' }} />
          <p className="mt-3">Đang tải lịch sử làm bài...</p>
        </div>
      </Container>
    );
  }

  if (error) {
    return (
      <Container className="py-5">
        <Alert variant="danger">
          <Alert.Heading>Lỗi</Alert.Heading>
          <p>{error}</p>
          <Button variant="outline-danger" onClick={() => navigate('/dashboard')}>
            Quay về Dashboard
          </Button>
        </Alert>
      </Container>
    );
  }

  return (
    <Container className="py-4">
      {/* Header */}
      <Row className="mb-4">
        <Col>
          <Button 
            variant="outline-secondary" 
            onClick={() => navigate('/dashboard')}
            className="mb-3"
          >
            <FiArrowLeft className="me-2" />
            Quay lại Dashboard
          </Button>
          <h2 className="fw-bold mb-0">📝 Lịch sử làm bài</h2>
          <p className="text-muted">Tất cả các bài thi bạn đã làm</p>
        </Col>
      </Row>

      {/* Filters Section */}
      {userExams.length > 0 && (
        <Card className="shadow-sm mb-3">
          <Card.Body>
            <div className="d-flex align-items-center mb-3">
              <FiFilter className="me-2" />
              <h6 className="mb-0 fw-bold">Bộ lọc</h6>
            </div>
            <Row className="g-3">
              <Col md={3}>
                <Form.Group>
                  <Form.Label className="small fw-semibold">Loại bài thi</Form.Label>
                  <Form.Select 
                    size="sm"
                    value={filters.testType}
                    onChange={(e) => handleFilterChange('testType', e.target.value)}
                  >
                    <option value="all">Tất cả</option>
                    <option value="full">Full Test</option>
                    <option value="single">Single Skill</option>
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={3}>
                <Form.Group>
                  <Form.Label className="small fw-semibold">Kỹ năng</Form.Label>
                  <Form.Select 
                    size="sm"
                    value={filters.skillType}
                    onChange={(e) => handleFilterChange('skillType', e.target.value)}
                  >
                    <option value="all">Tất cả</option>
                    <option value="LISTENING">Listening</option>
                    <option value="READING">Reading</option>
                    <option value="WRITING">Writing</option>
                    <option value="SPEAKING">Speaking</option>
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={3}>
                <Form.Group>
                  <Form.Label className="small fw-semibold">Trạng thái</Form.Label>
                  <Form.Select 
                    size="sm"
                    value={filters.status}
                    onChange={(e) => handleFilterChange('status', e.target.value)}
                  >
                    <option value="all">Tất cả</option>
                    <option value="completed">Đã hoàn thành</option>
                    <option value="incomplete">Chưa hoàn thành</option>
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={3}>
                <Form.Group>
                  <Form.Label className="small fw-semibold">Tìm kiếm</Form.Label>
                  <InputGroup size="sm">
                    <InputGroup.Text>
                      <FiSearch />
                    </InputGroup.Text>
                    <Form.Control
                      type="text"
                      placeholder="Tìm kiếm..."
                      value={filters.searchText}
                      onChange={(e) => handleFilterChange('searchText', e.target.value)}
                    />
                  </InputGroup>
                </Form.Group>
              </Col>
            </Row>
            <div className="mt-3">
              <Button 
                variant="outline-secondary" 
                size="sm"
                onClick={resetFilters}
              >
                Đặt lại bộ lọc
              </Button>
              <span className="ms-3 text-muted small">
                Hiển thị <strong>{filteredExams.length}</strong> / {userExams.length} bài thi
              </span>
            </div>
          </Card.Body>
        </Card>
      )}

      {/* Test History List */}
      {userExams.length === 0 ? (
        <Card className="shadow-sm">
          <Card.Body className="text-center py-5">
            <p className="text-muted mb-3">Bạn chưa có lịch sử làm bài nào</p>
            <Button variant="primary" onClick={() => navigate('/ieltstest/listening')}>
              Bắt đầu làm bài
            </Button>
          </Card.Body>
        </Card>
      ) : (
        <>
          {filteredExams.length === 0 ? (
            <Card className="shadow-sm">
              <Card.Body className="text-center py-5">
                <p className="text-muted mb-3">Không tìm thấy bài thi nào phù hợp với bộ lọc</p>
                <Button variant="outline-primary" size="sm" onClick={resetFilters}>
                  Đặt lại bộ lọc
                </Button>
              </Card.Body>
            </Card>
          ) : (
            <>
              <Card className="shadow-sm">
                <Card.Body className="p-0">
                  <div className="table-responsive">
                    <Table hover className="mb-0 align-middle">
                      <thead style={{ backgroundColor: '#f8f9fa', borderBottom: '2px solid #dee2e6' }}>
                        <tr>
                          <th className="py-3 px-4 fw-semibold">Loại bài thi</th>
                          <th className="py-3 px-4 fw-semibold">Ngày làm</th>
                          <th className="py-3 px-4 fw-semibold">Kỹ năng</th>
                          <th className="py-3 px-4 fw-semibold">Trạng thái</th>
                          <th className="py-3 px-4 fw-semibold text-center">Điểm</th>
                          <th className="py-3 px-4 fw-semibold text-center">Thao tác</th>
                        </tr>
                      </thead>
                      <tbody>
                        {(() => {
                          const indexOfLastItem = currentPage * itemsPerPage;
                          const indexOfFirstItem = indexOfLastItem - itemsPerPage;
                          const currentItems = filteredExams.slice(indexOfFirstItem, indexOfLastItem);
                    
                          return currentItems.map((userExam) => {
                            const parts = userExam.parts || [];
                            const testType = getTestType(parts);
                            const isFullTest = testType === '🎯 FULL TEST';
                            const overallBand = calculateOverallBand(parts);
                            const allCompleted = parts.length > 0 && parts.every(p => p.submitted);

                            return (
                              <tr key={userExam.id} style={{ borderBottom: '1px solid #dee2e6' }}>
                                <td className="py-3 px-4">
                                  <Badge 
                                    bg={isFullTest ? 'info' : 'secondary'} 
                                    className="text-uppercase px-3 py-2"
                                    style={{ fontSize: '0.75rem', fontWeight: '600' }}
                                  >
                                    {testType}
                                  </Badge>
                                </td>
                                <td className="py-3 px-4">
                                  <div className="d-flex align-items-center text-muted">
                                    <FiClock size={16} className="me-2" />
                                    <span>{formatDate(userExam.startedAt)}</span>
                                  </div>
                                </td>
                                <td className="py-3 px-4">
                                  <div className="d-flex gap-1 flex-wrap">
                                    {parts.map((part, idx) => (
                                      <Badge 
                                        key={idx}
                                        bg={getSkillBadgeVariant(part.skillType)}
                                        className="px-2 py-1"
                                        style={{ fontSize: '0.7rem', fontWeight: '500' }}
                                      >
                                        {part.skillType}
                                      </Badge>
                                    ))}
                                  </div>
                                </td>
                                <td className="py-3 px-4">
                                  {allCompleted ? (
                                    <span className="d-flex align-items-center text-success fw-semibold">
                                      <FiCheckCircle size={16} className="me-2" />
                                      Đã hoàn thành
                                    </span>
                                  ) : (
                                    <span className="d-flex align-items-center text-warning fw-semibold">
                                      <FiClock size={16} className="me-2" />
                                      Chưa hoàn thành
                                    </span>
                                  )}
                                </td>
                                <td className="py-3 px-4 text-center">
                                  {overallBand !== 'N/A' ? (
                                    <div>
                                      <div className="fs-5 fw-bold text-primary">{overallBand}</div>
                                      <small className="text-muted">Band Score</small>
                                    </div>
                                  ) : (
                                    <span className="text-muted">N/A</span>
                                  )}
                                </td>
                                <td className="py-3 px-4 text-center">
                                  <Button 
                                    variant={allCompleted ? 'primary' : 'outline-secondary'}
                                    size="sm"
                                    onClick={() => handleViewResult(userExam)}
                                    disabled={!allCompleted}
                                    className="px-3"
                                  >
                                    Xem kết quả
                                  </Button>
                                </td>
                              </tr>
                            );
                          });
                        })()}
                      </tbody>
                    </Table>
                  </div>
                </Card.Body>
              </Card>

              {/* Pagination */}
              {filteredExams.length > itemsPerPage && (
                <Row className="mt-4">
                  <Col>
                    <div className="d-flex justify-content-between align-items-center">
                      <div className="text-muted">
                        Hiển thị {Math.min((currentPage - 1) * itemsPerPage + 1, filteredExams.length)} - {Math.min(currentPage * itemsPerPage, filteredExams.length)} trong tổng số {filteredExams.length} bài thi
                      </div>
                      <Pagination className="mb-0">
                        <Pagination.First 
                          onClick={() => setCurrentPage(1)} 
                          disabled={currentPage === 1}
                        />
                        <Pagination.Prev 
                          onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))} 
                          disabled={currentPage === 1}
                        />
                        
                        {(() => {
                          const totalPages = Math.ceil(filteredExams.length / itemsPerPage);
                          const pages = [];
                          const maxPagesToShow = 5;
                          
                          let startPage = Math.max(1, currentPage - Math.floor(maxPagesToShow / 2));
                          let endPage = Math.min(totalPages, startPage + maxPagesToShow - 1);
                          
                          if (endPage - startPage + 1 < maxPagesToShow) {
                            startPage = Math.max(1, endPage - maxPagesToShow + 1);
                          }
                          
                          for (let i = startPage; i <= endPage; i++) {
                            pages.push(
                              <Pagination.Item
                                key={i}
                                active={i === currentPage}
                                onClick={() => setCurrentPage(i)}
                              >
                                {i}
                              </Pagination.Item>
                            );
                          }
                          
                          return pages;
                            })()}
                        
                        <Pagination.Next 
                          onClick={() => setCurrentPage(prev => Math.min(prev + 1, Math.ceil(filteredExams.length / itemsPerPage)))} 
                          disabled={currentPage === Math.ceil(filteredExams.length / itemsPerPage)}
                        />
                        <Pagination.Last 
                          onClick={() => setCurrentPage(Math.ceil(filteredExams.length / itemsPerPage))} 
                          disabled={currentPage === Math.ceil(filteredExams.length / itemsPerPage)}
                        />
                      </Pagination>
                    </div>
                  </Col>
                </Row>
              )}
            </>
          )}
        </>
      )}
    </Container>
  );
};

export default TestHistoryPage;
