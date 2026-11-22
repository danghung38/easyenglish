import { Card } from "react-bootstrap";
import { FiCalendar, FiFlag } from "react-icons/fi";
import { useState, useEffect } from "react";
import { getUpcomingEvents } from "../../services/scheduleService";

const GoalCard = ({ bandScore = 7.5, targetDate = "15/05/2024" }) => {
  const [nextTestDate, setNextTestDate] = useState(null);

  useEffect(() => {
    const fetchNextTestDate = async () => {
      try {
        console.log('🔍 Fetching upcoming events...');
        const response = await getUpcomingEvents();
        console.log('📅 Upcoming events response:', response);
        
        if (response.result && response.result.length > 0) {
          // Get the first upcoming event (should be sorted by date)
          const nextEvent = response.result[0];
          console.log('✅ Next event:', nextEvent);
          const date = new Date(nextEvent.scheduledTime);
          const formattedDate = date.toLocaleDateString('vi-VN');
          console.log('📆 Formatted date:', formattedDate);
          setNextTestDate(formattedDate);
        } else {
          console.log('ℹ️ No upcoming events found');
        }
      } catch (error) {
        console.error('❌ Error fetching upcoming test date:', error);
      }
    };

    fetchNextTestDate();
  }, []);

  const displayDate = nextTestDate || targetDate;

  return (
    <Card className="shadow-sm border-0" style={{
      background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
      color: "white",
      borderRadius: "12px"
    }}>
      <Card.Body className="p-4">
        <div className="d-flex justify-content-between align-items-start mb-3">
          <h6 className="fw-semibold mb-0 opacity-75">Mục tiêu Band</h6>
          <FiFlag size={20} className="opacity-75" />
        </div>
        
        <h1 className="fw-bold mb-4" style={{ fontSize: "3rem", lineHeight: "1" }}>{bandScore}</h1>

        <div className="d-flex justify-content-between align-items-center">
          <span className="opacity-75" style={{ fontSize: "0.9rem" }}>Ngày thi dự kiến</span>
          <span className="fw-semibold" style={{ fontSize: "0.9rem" }}>{displayDate}</span>
        </div>
      </Card.Body>
    </Card>
  );
};

export default GoalCard;
