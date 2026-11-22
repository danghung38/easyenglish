import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Badge, Button, Spinner } from 'react-bootstrap';
import { FiBook, FiVolume2, FiImage, FiArrowLeft, FiArrowRight, FiRotateCw, FiCheck } from 'react-icons/fi';
import { useParams, useNavigate } from 'react-router-dom';
import { getVocabulariesByTopic, getAllVocabularyTopics } from '../../services/vocabularyService';
import { useAlert } from '../../context/AlertContext';
import '../../styles/VocabularyPage.css';

const VocabularyPage = () => {
  const { topicName } = useParams();
  const navigate = useNavigate();
  const { showError, showSuccess } = useAlert();
  
  const [topics, setTopics] = useState([]);
  const [vocabularies, setVocabularies] = useState([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isFlipped, setIsFlipped] = useState(false);
  const [loading, setLoading] = useState(true);
  const [selectedTopic, setSelectedTopic] = useState(topicName || '');
  const [masteredWords, setMasteredWords] = useState(new Set());

  // Fetch topics on mount
  useEffect(() => {
    fetchTopics();
  }, []);

  // Fetch vocabularies when topic changes
  useEffect(() => {
    if (selectedTopic) {
      fetchVocabularies(selectedTopic);
    }
  }, [selectedTopic]);

  const fetchTopics = async () => {
    try {
      const response = await getAllVocabularyTopics();
      if (response.code === 200) {
        setTopics(response.result);
        if (!selectedTopic && response.result.length > 0) {
          setSelectedTopic(response.result[0].title);
        }
      }
    } catch (err) {
      console.error('Error fetching topics:', err);
      showError('Không thể tải danh sách chủ đề. Vui lòng thử lại!');
    }
  };

  const fetchVocabularies = async (topic) => {
    setLoading(true);
    try {
      const response = await getVocabulariesByTopic(topic);
      if (response.code === 200) {
        setVocabularies(response.result);
        setCurrentIndex(0);
        setIsFlipped(false);
      }
    } catch (err) {
      console.error('Error fetching vocabularies:', err);
      showError('Không thể tải từ vựng. Vui lòng thử lại!');
    } finally {
      setLoading(false);
    }
  };

  const handleTopicChange = (topic) => {
    setSelectedTopic(topic);
    navigate(`/vocabulary/${topic}`);
  };

  const handleFlip = () => {
    setIsFlipped(!isFlipped);
  };

  const handleNext = () => {
    if (currentIndex < vocabularies.length - 1) {
      setCurrentIndex(currentIndex + 1);
      setIsFlipped(false);
    }
  };

  const handlePrevious = () => {
    if (currentIndex > 0) {
      setCurrentIndex(currentIndex - 1);
      setIsFlipped(false);
    }
  };

  const handlePlayAudio = (audioUrl) => {
    if (audioUrl) {
      const audio = new Audio(audioUrl);
      audio.play().catch(err => console.error('Error playing audio:', err));
    }
  };

  const handleMarkAsMastered = () => {
    if (currentVocab) {
      setMasteredWords(prev => new Set([...prev, currentVocab.id]));
      showSuccess(`Đã đánh dấu "${currentVocab.word}" là đã thuộc!`, 2000);
      // Auto advance to next word after marking as mastered
      setTimeout(() => {
        if (currentIndex < vocabularies.length - 1) {
          handleNext();
        }
      }, 500);
    }
  };

  const currentVocab = vocabularies[currentIndex];
  const currentTopicInfo = topics.find(t => t.title === selectedTopic);

  if (loading && vocabularies.length === 0) {
    return (
      <Container className="py-5 text-center">
        <Spinner animation="border" variant="primary" />
        <p className="mt-3">Đang tải từ vựng...</p>
      </Container>
    );
  }

  return (
    <Container className="py-4">
      {/* Header */}
      <div className="mb-4">
        <Button 
          variant="link" 
          className="text-decoration-none p-0 mb-3"
          onClick={() => navigate('/dashboard')}
        >
          <FiArrowLeft size={20} className="me-2" />
          Quay lại Dashboard
        </Button>
        <h2 className="fw-bold mb-1">Học từ vựng theo chủ đề</h2>
        <p className="text-muted">Mở rộng vốn từ vựng của bạn với các flashcard tương tác</p>
      </div>

      {/* Topic Selector */}
      <div className="mb-4">
        <h5 className="fw-bold mb-3"><FiBook className="me-2" />Chọn chủ đề học tập</h5>
        <div className="topic-grid">
          {topics.map((topic) => (
            <div
              key={topic.title}
              className={`topic-card ${selectedTopic === topic.title ? 'active' : ''}`}
              onClick={() => handleTopicChange(topic.title)}
            >
              <div
                className="topic-card-header"
                style={{ background: topic.gradient }}
              >
                <FiBook size={28} style={{ color: topic.iconColor || '#fff' }} />
                <Badge bg="light" text="dark" className="mt-2">
                  {topic.wordCount} từ
                </Badge>
              </div>
              <div className="topic-card-body">
                <h6 className="mb-0">{topic.title}</h6>
              </div>
            </div>
          ))}
        </div>
      </div>

      {vocabularies.length === 0 && !loading && (
        <div className="text-center py-5">
          <FiBook size={64} className="text-muted mb-3" />
          <h5 className="text-muted">Chưa có từ vựng nào trong chủ đề này.</h5>
        </div>
      )}

      {vocabularies.length > 0 && (
        <>
          {/* Progress */}
          <div className="vocabulary-progress mb-4">
            <div className="d-flex justify-content-between align-items-center mb-3">
              <div>
                <h5 className="mb-1 fw-bold">{selectedTopic}</h5>
                <span className="text-muted">Từ {currentIndex + 1} / {vocabularies.length}</span>
              </div>
              <Badge bg="success" className="px-3 py-2" style={{ fontSize: '1rem' }}>
                <FiCheck className="me-1" />
                {masteredWords.size} đã thuộc
              </Badge>
            </div>
            <div className="custom-progress">
              <div
                className="custom-progress-bar"
                style={{
                  width: `${((currentIndex + 1) / vocabularies.length) * 100}%`,
                  background: currentTopicInfo?.gradient || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
                }}
              ></div>
            </div>
          </div>

          {/* Flashcard */}
          <Row className="justify-content-center mb-4">
            <Col md={10} lg={8} xl={6}>
              <div className="flashcard-wrapper">
                <div 
                  className={`flashcard ${isFlipped ? 'is-flipped' : ''} ${masteredWords.has(currentVocab?.id) ? 'mastered' : ''}`}
                  onClick={handleFlip}
                >
                  {/* Front Side */}
                  <div className="flashcard-face flashcard-front">
                    <div className="flashcard-content">
                      <h1 className="flashcard-word">
                        {currentVocab?.word}
                      </h1>
                      {currentVocab?.pronunciation && (
                        <p className="flashcard-pronunciation">
                          {currentVocab.pronunciation}
                        </p>
                      )}
                      {currentVocab?.pronunciationAudioUrl && (
                        <Button
                          variant="primary"
                          className="audio-button"
                          onClick={(e) => {
                            e.stopPropagation();
                            handlePlayAudio(currentVocab.pronunciationAudioUrl);
                          }}
                        >
                          <FiVolume2 size={24} />
                        </Button>
                      )}
                    </div>
                    <div className="flashcard-hint">
                      <Badge bg="primary" className="px-3 py-2">
                        👆 Click để xem nghĩa
                      </Badge>
                    </div>
                  </div>

                  {/* Back Side */}
                  <div className="flashcard-face flashcard-back">
                    <div className="flashcard-content">
                      <div className="flashcard-meaning-section">            
                        <h2 className="flashcard-meaning">
                          {currentVocab?.meaning}
                        </h2>
                      </div>
                      {currentVocab?.example && (
                        <div className="flashcard-example-section">
                          <p className="flashcard-example">
                            "{currentVocab.example}"
                          </p>
                        </div>
                      )}
                    </div>
                    <div className="flashcard-hint">
                      <Badge bg="light" text="dark" className="px-3 py-2">
                        👆 Click để xem từ
                      </Badge>
                    </div>
                  </div>
                </div>
              </div>
            </Col>
          </Row>

          {/* Navigation */}
          <Row className="justify-content-center">
            <Col md={10} lg={8} xl={6}>
              <div className="flashcard-controls">
                <Button
                  variant="outline-secondary"
                  disabled={currentIndex === 0}
                  onClick={handlePrevious}
                  className="control-btn"
                  size="lg"
                >
                  <FiArrowLeft size={20} className="me-2" />
                  Trước
                </Button>

                <div className="d-flex gap-2">
                  <Button
                    variant="outline-primary"
                    onClick={handleFlip}
                    className="control-btn"
                    size="lg"
                  >
                    <FiRotateCw size={20} className="me-2" />
                    Lật thẻ
                  </Button>

                  <Button
                    variant={masteredWords.has(currentVocab?.id) ? "success" : "outline-success"}
                    onClick={handleMarkAsMastered}
                    className="control-btn"
                    size="lg"
                    disabled={masteredWords.has(currentVocab?.id)}
                  >
                    <FiCheck size={20} className="me-2" />
                    {masteredWords.has(currentVocab?.id) ? 'Đã thuộc' : 'Đánh dấu đã thuộc'}
                  </Button>
                </div>

                <Button
                  variant="outline-secondary"
                  disabled={currentIndex === vocabularies.length - 1}
                  onClick={handleNext}
                  className="control-btn"
                  size="lg"
                >
                  Sau
                  <FiArrowRight size={20} className="ms-2" />
                </Button>
              </div>
            </Col>
          </Row>
        </>
      )}
    </Container>
  );
};

export default VocabularyPage;
