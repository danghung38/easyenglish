package com.dxh.Elearning.entity;

import com.dxh.Elearning.enums.TestType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedule_events")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleEvent extends AbstractEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    Exam exam;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TestType testType;

    @Column(nullable = false)
    LocalDateTime scheduledTime;

    @Column(nullable = false)
    Boolean completed = false;
}
