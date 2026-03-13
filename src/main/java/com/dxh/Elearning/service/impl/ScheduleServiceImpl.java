package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.dto.request.ScheduleEventRequest;
import com.dxh.Elearning.dto.response.ScheduleEventResponse;
import com.dxh.Elearning.entity.Exam;
import com.dxh.Elearning.entity.ScheduleEvent;
import com.dxh.Elearning.entity.User;
import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import com.dxh.Elearning.repo.ExamRepository;
import com.dxh.Elearning.repo.ScheduleEventRepository;
import com.dxh.Elearning.repo.UserRepository;
import com.dxh.Elearning.service.interfac.ScheduleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    ScheduleEventRepository scheduleEventRepository;
    UserRepository userRepository;
    ExamRepository examRepository;

    @Override
    @Transactional
    public ScheduleEventResponse createScheduleEvent(ScheduleEventRequest request) {
        User currentUser = getCurrentUser();

        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND));

        ScheduleEvent scheduleEvent = ScheduleEvent.builder()
                .user(currentUser)
                .exam(exam)
                .testType(request.getTestType())
                .scheduledTime(request.getScheduledTime())
                .completed(false)
                .build();

        scheduleEvent = scheduleEventRepository.save(scheduleEvent);
        log.info("Created schedule event: {} for user: {}", scheduleEvent.getId(), currentUser.getUsername());

        return mapToResponse(scheduleEvent);
    }

    @Override
    public List<ScheduleEventResponse> getMyScheduleEvents() {
        User currentUser = getCurrentUser();
        // Get events from the last 7 days onwards (including past events within 1 week)
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        List<ScheduleEvent> events = scheduleEventRepository.findRecentEvents(currentUser, oneWeekAgo);
        return events.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleEventResponse> getUpcomingEvents() {
        User currentUser = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        List<ScheduleEvent> events = scheduleEventRepository.findUpcomingEvents(currentUser, now);
        return events.stream()
                .limit(5)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ScheduleEventResponse markAsCompleted(Long eventId) {
        User currentUser = getCurrentUser();
        ScheduleEvent scheduleEvent = scheduleEventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!scheduleEvent.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        scheduleEvent.setCompleted(true);
        scheduleEvent = scheduleEventRepository.save(scheduleEvent);

        return mapToResponse(scheduleEvent);
    }

    @Override
    @Transactional
    public void deleteScheduleEvent(Long eventId) {
        User currentUser = getCurrentUser();
        ScheduleEvent scheduleEvent = scheduleEventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!scheduleEvent.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        scheduleEventRepository.delete(scheduleEvent);
        log.info("Deleted schedule event: {} for user: {}", eventId, currentUser.getUsername());
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private ScheduleEventResponse mapToResponse(ScheduleEvent event) {
        return ScheduleEventResponse.builder()
                .id(event.getId())
                .examId(event.getExam().getId())
                .examTitle(event.getExam().getTitle())
                .testType(event.getTestType())
                .scheduledTime(event.getScheduledTime())
                .completed(event.getCompleted())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
