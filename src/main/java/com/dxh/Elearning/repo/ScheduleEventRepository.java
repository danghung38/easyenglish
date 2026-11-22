package com.dxh.Elearning.repo;

import com.dxh.Elearning.entity.ScheduleEvent;
import com.dxh.Elearning.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleEventRepository extends JpaRepository<ScheduleEvent, Long> {

    List<ScheduleEvent> findByUserOrderByScheduledTimeDesc(User user);

    @Query("SELECT s FROM ScheduleEvent s WHERE s.user = :user AND s.scheduledTime >= :startTime ORDER BY s.scheduledTime ASC")
    List<ScheduleEvent> findUpcomingEvents(User user, LocalDateTime startTime);

    @Query("SELECT s FROM ScheduleEvent s WHERE s.user = :user AND s.scheduledTime >= :oneWeekAgo ORDER BY s.scheduledTime DESC")
    List<ScheduleEvent> findRecentEvents(User user, LocalDateTime oneWeekAgo);

    List<ScheduleEvent> findByUserAndCompletedOrderByScheduledTimeDesc(User user, Boolean completed);
}
