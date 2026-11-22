import React, { useState, useEffect } from "react";
import { Card, Badge, Button } from "react-bootstrap";
import { FiBook, FiFileText, FiArrowRight, FiUpload, FiCheckCircle } from "react-icons/fi";
import { useNavigate } from "react-router-dom";
import { getAllVocabularyTopics } from "../../services/vocabularyService";
import RecentTestsCard from "./RecentTestsCard";

const RecommendationCard = () => {
  const navigate = useNavigate();
  const [vocabTopics, setVocabTopics] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchVocabularyTopics();
  }, []);

  const fetchVocabularyTopics = async () => {
    try {
      const response = await getAllVocabularyTopics();
      if (response.code === 200) {
        // Lấy 4 chủ đề đầu tiên
        const topics = response.result.slice(0, 4).map(topic => ({
          icon: <FiBook size={36} />,
          badge: "VOCABULARY",
          title: topic.title,
          subtitle: `Học ${topic.wordCount} từ vựng chủ đề ${getTopicVietnamese(topic.title)}`,
          wordCount: topic.wordCount,
          gradient: topic.gradient,
          iconColor: topic.iconColor || "#fff",
          action: "Học ngay"
        }));
        setVocabTopics(topics);
      }
    } catch (error) {
      console.error('Error fetching vocabulary topics:', error);
      // Fallback to default topics if API fails
      setVocabTopics([
        {
          icon: <FiBook size={36} />,
          badge: "VOCABULARY",
          title: "Environment",
          subtitle: "Học từ vựng chủ đề Môi trường",
          wordCount: 0,
          gradient: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
          iconColor: "#fff",
          action: "Học ngay"
        },
        {
          icon: <FiBook size={36} />,
          badge: "VOCABULARY",
          title: "Technology",
          subtitle: "Học từ vựng chủ đề Công nghệ",
          wordCount: 0,
          gradient: "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)",
          iconColor: "#fff",
          action: "Học ngay"
        },
        {
          icon: <FiBook size={36} />,
          badge: "VOCABULARY",
          title: "Education",
          subtitle: "Học từ vựng chủ đề Giáo dục",
          wordCount: 0,
          gradient: "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)",
          iconColor: "#fff",
          action: "Học ngay"
        },
        {
          icon: <FiBook size={36} />,
          badge: "VOCABULARY",
          title: "Health",
          subtitle: "Học từ vựng chủ đề Sức khỏe",
          wordCount: 0,
          gradient: "linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)",
          iconColor: "#fff",
          action: "Học ngay"
        }
      ]);
    } finally {
      setLoading(false);
    }
  };

  const getTopicVietnamese = (topic) => {
    const mapping = {
      'Environment': 'Môi trường',
      'Technology': 'Công nghệ',
      'Education': 'Giáo dục',
      'Health': 'Sức khỏe',
      'Business': 'Kinh doanh'
    };
    return mapping[topic] || topic;
  };

  const handleLearnVocabulary = (topicTitle) => {
    navigate(`/vocabulary/${topicTitle}`);
  };

  return (
    <div className="mb-5">
      {/* Recent Tests Section */}
      <div className="mb-5">
        <RecentTestsCard />
      </div>

      {/* Vocabulary Topics Section - 4 cards in horizontal layout */}
      <div>
        <div className="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h4 className="fw-bold mb-1" style={{ color: '#2c3e50' }}>Thẻ từ vựng theo chủ đề</h4>
            <p className="text-muted small mb-0">Mở rộng vốn từ vựng của bạn</p>
          </div>
          <Button 
            variant="link" 
            className="text-decoration-none p-0 fw-semibold" 
            style={{ fontSize: '0.9rem', color: '#667eea' }}
          >
            Xem tất cả <FiArrowRight size={16} className="ms-1" />
          </Button>
        </div>

        <div className="row g-4">
          {vocabTopics.map((topic, index) => (
            <div key={index} className="col-md-6 col-lg-3">
              <Card 
                className="h-100 border-0 overflow-hidden"
                style={{ 
                  cursor: 'pointer',
                  transition: 'all 0.3s ease',
                  boxShadow: '0 2px 8px rgba(0,0,0,0.08)'
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.transform = 'translateY(-8px) scale(1.02)';
                  e.currentTarget.style.boxShadow = '0 12px 24px rgba(0,0,0,0.15)';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.transform = 'translateY(0) scale(1)';
                  e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.08)';
                }}
              >
                <div 
                  className="p-4 d-flex flex-column align-items-center justify-content-center position-relative"
                  style={{ 
                    background: topic.gradient,
                    minHeight: '140px'
                  }}
                >
                  <div 
                    className="position-absolute"
                    style={{
                      top: '10px',
                      right: '10px',
                      width: '60px',
                      height: '60px',
                      background: 'rgba(255, 255, 255, 0.1)',
                      borderRadius: '50%'
                    }}
                  />
                  <div style={{ color: topic.iconColor, zIndex: 1 }}>
                    {topic.icon}
                  </div>
                  <Badge 
                    bg="light" 
                    className="mt-3 px-2 py-1"
                    style={{ 
                      fontSize: '0.65rem',
                      fontWeight: '600',
                      color: '#2c3e50',
                      letterSpacing: '0.5px'
                    }}
                  >
                    {topic.wordCount} TỪ
                  </Badge>
                </div>
                <Card.Body className="p-3">
                  <h6 className="fw-bold mb-2" style={{ color: '#2c3e50', fontSize: '1rem' }}>
                    {topic.title}
                  </h6>
                  <p className="small text-muted mb-3" style={{ fontSize: '0.85rem', lineHeight: '1.5' }}>
                    {topic.subtitle}
                  </p>
                  
                  <Button 
                    variant="link" 
                    className="p-0 text-decoration-none fw-bold d-flex align-items-center"
                    onClick={() => handleLearnVocabulary(topic.title)}
                    style={{ fontSize: '0.85rem', color: '#667eea' }}
                  >
                    {topic.action} <FiArrowRight size={16} className="ms-2" />
                  </Button>
                </Card.Body>
              </Card>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default RecommendationCard;
