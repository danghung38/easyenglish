package com.dxh.Elearning.service.interfac;

import com.dxh.Elearning.dto.request.ScheduleEventRequest;
import com.dxh.Elearning.dto.response.ScheduleEventResponse;

import java.util.List;

public interface ScheduleService {

    ScheduleEventResponse createScheduleEvent(ScheduleEventRequest request);

    List<ScheduleEventResponse> getMyScheduleEvents();

    List<ScheduleEventResponse> getUpcomingEvents();

    ScheduleEventResponse markAsCompleted(Long eventId);

    void deleteScheduleEvent(Long eventId);
}
