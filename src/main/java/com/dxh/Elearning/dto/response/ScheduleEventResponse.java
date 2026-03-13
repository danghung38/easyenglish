package com.dxh.Elearning.dto.response;

import com.dxh.Elearning.enums.TestType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleEventResponse {

    Long id;
    Long examId;
    String examTitle;
    TestType testType;
    LocalDateTime scheduledTime;
    Boolean completed;
    LocalDateTime createdAt;
}
