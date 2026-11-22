import React, { useState, useEffect } from 'react';
import { Card, Badge, Button, Spinner, Modal, Row, Col } from 'react-bootstrap';
import { getUpcomingEvents, markAsCompleted, deleteScheduleEvent } from '../../services/scheduleService';

const CalendarCard = ({ refreshTrigger }) => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [weekDays, setWeekDays] = useState([]);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [currentMonth, setCurrentMonth] = useState(new Date());

  useEffect(() => {
    fetchUpcomingEvents();
    generateWeekDays();
    
    // Listen for schedule created event
    const handleScheduleCreated = () => {
      fetchUpcomingEvents();
    };
    
    window.addEventListener('scheduleCreated', handleScheduleCreated);
    
    return () => {
      window.removeEventListener('scheduleCreated', handleScheduleCreated);
    };
  }, [refreshTrigger]);

  const generateWeekDays = () => {
    const days = [];
    const today = new Date();
    
    for (let i = 0; i < 7; i++) {
      const date = new Date(today);
      date.setDate(today.getDate() + i);
      days.push({
        date: date,
        dayName: date.toLocaleDateString('vi-VN', { weekday: 'short' }),
        dayNumber: date.getDate(),
        month: date.getMonth(),
        year: date.getFullYear()
      });
    }
    setWeekDays(days);
  };

  const fetchUpcomingEvents = async () => {
    try {
      setLoading(true);
      console.log('Fetching upcoming events...');
      const response = await getUpcomingEvents();
      console.log('Response:', response);
      if (response && response.result) {
        console.log('Events received:', response.result);
        setEvents(response.result || []);
      }
    } catch (error) {
      console.error('Error fetching upcoming events:', error);
    } finally {
      setLoading(false);
    }
  };

  const getEventsForDay = (day) => {
    return events.filter(event => {
      const eventDate = new Date(event.scheduledTime);
      return eventDate.getDate() === day.date.getDate() &&
             eventDate.getMonth() === day.date.getMonth() &&
             eventDate.getFullYear() === day.date.getFullYear();
    });
  };

  const handleShowDetail = (event) => {
    setSelectedEvent(event);
    setShowDetailModal(true);
  };

  // Generate calendar days for current month
  const generateMonthDays = () => {
    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startDayOfWeek = firstDay.getDay();
    
    const days = [];
    
    // Add empty cells for days before month starts
    for (let i = 0; i < startDayOfWeek; i++) {
      days.push(null);
    }
    
    // Add all days in month
    for (let day = 1; day <= daysInMonth; day++) {
      days.push(new Date(year, month, day));
    }
    
    return days;
  };

  const getEventsForDate = (date) => {
    if (!date) return [];
    return events.filter(event => {
      const eventDate = new Date(event.scheduledTime);
      return eventDate.getDate() === date.getDate() &&
             eventDate.getMonth() === date.getMonth() &&
             eventDate.getFullYear() === date.getFullYear();
    });
  };

  const changeMonth = (direction) => {
    const newMonth = new Date(currentMonth);
    newMonth.setMonth(currentMonth.getMonth() + direction);
    setCurrentMonth(newMonth);
  };

  const handleComplete = async (eventId) => {
    try {
      await markAsCompleted(eventId);
      fetchUpcomingEvents();
    } catch (error) {
      console.error('Error marking as completed:', error);
    }
  };

  const handleDelete = async (eventId) => {
    if (window.confirm('Bạn có chắc muốn xóa lịch thi này?')) {
      try {
        await deleteScheduleEvent(eventId);
        fetchUpcomingEvents();
      } catch (error) {
        console.error('Error deleting event:', error);
      }
    }
  };

  const getTestTypeLabel = (testType) => {
    const labels = {
      FULL_TEST: 'Full Test',
      LISTENING: 'Listening',
      READING: 'Reading',
      WRITING: 'Writing',
      SPEAKING: 'Speaking'
    };
    return labels[testType] || testType;
  };

  const getTestTypeColor = (testType) => {
    const colors = {
      FULL_TEST: 'primary',
      LISTENING: 'primary',
      READING: 'success',
      WRITING: 'warning',
      SPEAKING: 'danger'
    };
    return colors[testType] || 'secondary';
  };

  const formatDateTime = (dateTimeString) => {
    const date = new Date(dateTimeString);
    return date.toLocaleString('vi-VN', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading) {
    return (
      <Card>
        <Card.Header className="d-flex justify-content-between align-items-center">
          <h5 className="mb-0">
            <i className="fe fe-calendar me-2"></i>
            Lịch ôn thi (7 ngày tới)
          </h5>
        </Card.Header>
        <Card.Body className="text-center py-5">
          <Spinner animation="border" size="sm" />
        </Card.Body>
      </Card>
    );
  }

  const upcomingEvent = events.length > 0 ? events[0] : null;

  return (
    <>
      <Card>
        <Card.Header className="d-flex justify-content-between align-items-center">
          <h5 className="mb-0">Lịch ôn thi</h5>
          <Button 
            variant="link" 
            size="sm" 
            className="text-primary p-0"
            onClick={() => setShowDetailModal(true)}
          >
            Chi tiết
          </Button>
        </Card.Header>
        <Card.Body>
          {/* Week view - Compact - Always show */}
          <div className="d-flex gap-1 mb-3">
                {weekDays.slice(0, 5).map((day, index) => {
                  const dayEvents = getEventsForDay(day);
                  const isToday = new Date().toDateString() === day.date.toDateString();
                  
                  return (
                    <div 
                      key={index}
                      className={`border rounded text-center flex-fill ${isToday ? 'border-primary' : ''}`}
                      style={{ 
                        padding: '6px 4px',
                        cursor: 'pointer',
                        position: 'relative',
                        minWidth: '45px'
                      }}
                      onClick={() => dayEvents.length > 0 && setShowDetailModal(true)}
                    >
                      <div style={{ fontSize: '10px', color: '#6c757d' }}>{day.dayName}</div>
                      <div 
                        className={`${isToday ? 'bg-primary text-white' : ''}`}
                        style={{ 
                          fontSize: '16px',
                          fontWeight: isToday ? 'bold' : 'normal',
                          borderRadius: '50%',
                          width: '28px',
                          height: '28px',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          margin: '2px auto'
                        }}
                      >
                        {day.dayNumber}
                      </div>
                      {dayEvents.length > 0 && (
                        <div style={{ 
                          position: 'absolute',
                          bottom: '3px',
                          left: '50%',
                          transform: 'translateX(-50%)',
                          width: '4px',
                          height: '4px',
                          borderRadius: '50%',
                          backgroundColor: isToday ? '#0d6efd' : '#6c757d'
                        }} />
                      )}
                    </div>
                  );
                })}
              </div>

          {/* Show only the most recent event or empty message */}
          {upcomingEvent ? (
            <div 
              className="border-start border-4 border-primary ps-3"
              style={{ cursor: 'pointer' }}
              onClick={() => setShowDetailModal(true)}
            >
              <h6 className="mb-1 fw-bold">{upcomingEvent.examTitle}</h6>
              <div className="d-flex align-items-center text-muted small">
                <i className="fe fe-clock me-1"></i>
                <span>{formatDateTime(upcomingEvent.scheduledTime)} • Hôm nay</span>
              </div>
            </div>
          ) : (
            <div className="text-center text-muted py-3">
              <i className="fe fe-calendar" style={{ fontSize: '36px', opacity: 0.3 }}></i>
              <p className="mt-2 mb-0 small">Chưa có lịch thi nào</p>
              <small style={{ fontSize: '0.8rem' }}>Hãy tạo lịch thi từ trang bài tập</small>
            </div>
          )}
        </Card.Body>
      </Card>

      {/* Detail Modal - Full Calendar View */}
      <Modal show={showDetailModal} onHide={() => setShowDetailModal(false)} centered size="lg">
        <Modal.Header closeButton>
          <Modal.Title>
            <div className="d-flex align-items-center gap-3">
              <Button variant="outline-secondary" size="sm" onClick={() => changeMonth(-1)}>
                <i className="fe fe-chevron-left"></i>
              </Button>
              <span>
                Tháng {currentMonth.getMonth() + 1}, {currentMonth.getFullYear()}
              </span>
              <Button variant="outline-secondary" size="sm" onClick={() => changeMonth(1)}>
                <i className="fe fe-chevron-right"></i>
              </Button>
            </div>
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Row>
            {/* Calendar View */}
            <Col md={7} className="border-end">
              <div className="mb-3">
                {/* Weekday headers */}
                <div className="d-flex mb-2">
                  {['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'].map((day, idx) => (
                    <div key={idx} className="flex-fill text-center fw-bold small text-muted" style={{ padding: '4px' }}>
                      {day}
                    </div>
                  ))}
                </div>
                
                {/* Calendar grid */}
                <div className="d-flex flex-wrap">
                  {generateMonthDays().map((date, idx) => {
                    const isToday = date && new Date().toDateString() === date.toDateString();
                    const isSelected = date && selectedDate && date.toDateString() === selectedDate.toDateString();
                    const dayEvents = date ? getEventsForDate(date) : [];
                    
                    return (
                      <div 
                        key={idx}
                        className={`text-center ${date ? 'hover-bg-light' : ''}`}
                        style={{ 
                          width: 'calc(100% / 7)',
                          aspectRatio: '1',
                          padding: '4px',
                          cursor: date ? 'pointer' : 'default',
                          position: 'relative'
                        }}
                        onClick={() => date && setSelectedDate(date)}
                      >
                        {date && (
                          <>
                            <div 
                              className={`${isToday ? 'bg-primary text-white' : ''} ${isSelected ? 'border border-primary' : ''}`}
                              style={{ 
                                borderRadius: '50%',
                                width: '32px',
                                height: '32px',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                margin: '0 auto',
                                fontSize: '13px',
                                fontWeight: isToday ? 'bold' : 'normal'
                              }}
                            >
                              {date.getDate()}
                            </div>
                            {dayEvents.length > 0 && (
                              <div style={{ 
                                position: 'absolute',
                                bottom: '8px',
                                left: '50%',
                                transform: 'translateX(-50%)',
                                fontSize: '10px',
                                color: '#0d6efd',
                                fontWeight: 'bold'
                              }}>
                                •
                              </div>
                            )}
                          </>
                        )}
                      </div>
                    );
                  })}
                </div>
              </div>
            </Col>
            
            {/* Events list for selected date */}
            <Col md={5}>
              <h6 className="mb-3">
                {selectedDate.toLocaleDateString('vi-VN', { 
                  weekday: 'long',
                  day: 'numeric', 
                  month: 'long'
                })}
              </h6>
              <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
                {getEventsForDate(selectedDate).length === 0 ? (
                  <div className="text-center text-muted py-4">
                    <i className="fe fe-calendar" style={{ fontSize: '36px', opacity: 0.3 }}></i>
                    <p className="small mt-2">Không có lịch thi</p>
                  </div>
                ) : (
                  <div className="d-flex flex-column gap-2">
                    {getEventsForDate(selectedDate).map((event) => (
                      <div 
                        key={event.id}
                        className="border rounded p-2"
                        style={{ backgroundColor: event.completed ? '#f8f9fa' : '#fff' }}
                      >
                        <div className="d-flex justify-content-between align-items-start mb-1">
                          <h6 className="mb-1 small fw-bold">{event.examTitle}</h6>
                          {event.completed && (
                            <Badge bg="success" style={{ fontSize: '9px' }}>
                              <i className="fe fe-check"></i>
                            </Badge>
                          )}
                        </div>
                        
                        <Badge bg={getTestTypeColor(event.testType)} className="mb-2" style={{ fontSize: '10px' }}>
                          {getTestTypeLabel(event.testType)}
                        </Badge>
                        
                        <div className="text-muted" style={{ fontSize: '11px' }}>
                          <i className="fe fe-clock me-1"></i>
                          {new Date(event.scheduledTime).toLocaleTimeString('vi-VN', { 
                            hour: '2-digit', 
                            minute: '2-digit' 
                          })}
                        </div>

                        {!event.completed && (
                          <div className="d-flex gap-1 mt-2">
                            <Button 
                              variant="success" 
                              size="sm"
                              className="flex-grow-1"
                              style={{ fontSize: '11px', padding: '4px 8px' }}
                              onClick={() => handleComplete(event.id)}
                            >
                              <i className="fe fe-check me-1"></i>
                              Hoàn thành
                            </Button>
                            <Button 
                              variant="outline-danger" 
                              size="sm"
                              style={{ fontSize: '11px', padding: '4px 8px' }}
                              onClick={() => handleDelete(event.id)}
                            >
                              <i className="fe fe-trash-2"></i>
                            </Button>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </Col>
          </Row>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowDetailModal(false)}>
            Đóng
          </Button>
        </Modal.Footer>
      </Modal>
    </>
  );
};

export default CalendarCard;
