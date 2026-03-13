package com.dxh.Elearning.entity;

import com.dxh.Elearning.enums.SkillType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "user_exam_parts")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserExamPart extends AbstractEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_exam_id")
    UserExam userExam;

    @Enumerated(EnumType.STRING)
    SkillType skillType;

    Double score;

    Boolean submitted;

    @OneToMany(mappedBy = "userExamPart", cascade = CascadeType.ALL, orphanRemoval = true)
    List<UserAnswer> answers;
}

