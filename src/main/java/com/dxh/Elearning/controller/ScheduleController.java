package com.dxh.Elearning.controller;

import com.dxh.Elearning.dto.request.ScheduleEventRequest;
import com.dxh.Elearning.dto.response.ApiResponse;
import com.dxh.Elearning.dto.response.ScheduleEventResponse;
import com.dxh.Elearning.service.interfac.ScheduleService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost"}, allowCredentials = "true")
public class ScheduleController {

    ScheduleService scheduleService;

    @PostMapping
    public ApiResponse<ScheduleEventResponse> createScheduleEvent(@RequestBody @Valid ScheduleEventRequest request) {
        return ApiResponse.<ScheduleEventResponse>builder()
                .result(scheduleService.createScheduleEvent(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<ScheduleEventResponse>> getMyScheduleEvents() {
        return ApiResponse.<List<ScheduleEventResponse>>builder()
                .result(scheduleService.getMyScheduleEvents())
                .build();
    }

    @GetMapping("/upcoming")
    public ApiResponse<List<ScheduleEventResponse>> getUpcomingEvents() {
        return ApiResponse.<List<ScheduleEventResponse>>builder()
                .result(scheduleService.getUpcomingEvents())
                .build();
    }

    @PatchMapping("/{id}/complete")
    public ApiResponse<ScheduleEventResponse> markAsCompleted(@PathVariable Long id) {
        return ApiResponse.<ScheduleEventResponse>builder()
                .result(scheduleService.markAsCompleted(id))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteScheduleEvent(@PathVariable Long id) {
        scheduleService.deleteScheduleEvent(id);
        return ApiResponse.<Void>builder().build();
    }
}
