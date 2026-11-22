import React, { useEffect, useState } from "react";
import { Col, Container, Row, Spinner, Alert } from "react-bootstrap";
import { useAuth } from "../../context/AuthContext";
import OverallProgressCard from "../../components/dashboard/OverallProgressCard";
import GoalCard from "../../components/dashboard/GoalCard";
import CalendarCard from "../../components/dashboard/CalendarCard";
import AIPracticeCard from "../../components/dashboard/AIPracticeCard";
import LeaderboardCard from "../../components/dashboard/LeaderboardCard";
import RecommendationCard from "../../components/dashboard/RecommendationCard";
import SkillModulesCard from "../../components/dashboard/SkillModulesCard";
import { getUserStats, getUserInfo } from "../../services/examService";
import "../../assets/css/dashboard.css";

const DashboardPage = () => {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [userInfo, setUserInfo] = useState(null);
  const [dashboardData, setDashboardData] = useState({
    overallScore: 0,
    skills: [],
    recentTests: [],
    totalTests: 0
  });

  useEffect(() => {
    document.title = "Dashboard | IELTS Master";
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Fetch user info and statistics from API in parallel
      const [stats, myInfo] = await Promise.all([
        getUserStats(),
        getUserInfo()
      ]);
      
      setUserInfo(myInfo);
      
      // Transform skills data for display
      const skillsArray = [
        { name: "Nghe (Listening)", score: stats.skills.LISTENING || 0, variant: "primary" },
        { name: "Nói (Speaking)", score: stats.skills.SPEAKING || 0, variant: "danger" },
        { name: "Đọc (Reading)", score: stats.skills.READING || 0, variant: "warning" },
        { name: "Viết (Writing)", score: stats.skills.WRITING || 0, variant: "success" },
      ];

      setDashboardData({
        overallScore: stats.overallScore || 0,
        skills: skillsArray,
        recentTests: stats.recentTests || [],
        totalTests: stats.totalTests || 0
      });
      
      setLoading(false);
    } catch (err) {
      console.error("Error fetching dashboard data:", err);
      setError("Không thể tải dữ liệu dashboard. Vui lòng thử lại sau.");
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Container fluid className="px-4 pb-5 pt-4">
        <div className="text-center py-5">
          <Spinner animation="border" variant="primary" style={{ width: '3rem', height: '3rem' }} />
          <p className="mt-3 text-muted">Đang tải dữ liệu...</p>
        </div>
      </Container>
    );
  }

  if (error) {
    return (
      <Container fluid className="px-4 pb-5 pt-4">
        <Alert variant="danger" className="mt-4">
          <Alert.Heading>Lỗi</Alert.Heading>
          <p>{error}</p>
          <button className="btn btn-primary" onClick={fetchDashboardData}>
            Thử lại
          </button>
        </Alert>
      </Container>
    );
  }

  return (
    <>
      <Container fluid className="px-4 pb-5 pt-4">
        {/* Top Section: Overall Progress + Goal & Calendar */}
        <Row className="g-4 mb-4">
          <Col lg={8}>
            <OverallProgressCard 
              overallScore={dashboardData.overallScore} 
              skills={dashboardData.skills}
              targetBand={userInfo?.bandsTarget || 7.5}
            />
          </Col>
          <Col lg={4}>
            <div className="mb-3">
              <GoalCard 
                bandScore={userInfo?.bandsTarget || 7.5} 
                targetDate={userInfo?.dob || "--/--/----"} 
              />
            </div>
            <CalendarCard />
          </Col>
        </Row>

        {/* Skill Modules Section */}
        <div className="mb-4">
          <h5 className="fw-bold mb-3">Luyện tập theo kỹ năng</h5>
          <SkillModulesCard />
        </div>

        {/* Middle Section: AI Practice + Leaderboard */}
        <Row className="g-4 mb-4">
          <Col lg={8}>
            <AIPracticeCard />
          </Col>
          <Col lg={4}>
            <LeaderboardCard />
          </Col>
        </Row>

        {/* Recommendations Section */}
        <RecommendationCard />
      </Container>
    </>
  );
};

export default DashboardPage;
