package com.dxh.Elearning.dto.request;

import com.dxh.Elearning.enums.TestType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleEventRequest {

    @NotNull(message = "Exam ID is required")
    Long examId;

    @NotNull(message = "Test type is required")
    TestType testType;

    @NotNull(message = "Scheduled time is required")
    LocalDateTime scheduledTime;
}
