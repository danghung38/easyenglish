package com.dxh.Elearning.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "user_exams")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserExam extends AbstractEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id")
    Exam exam;

    Boolean submitted;

    Double totalScore;

    LocalDateTime startedAt;

    LocalDateTime submittedAt;

    @OneToMany(mappedBy = "userExam", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<UserExamPart> parts;
}

