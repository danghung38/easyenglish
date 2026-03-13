package com.dxh.Elearning.entity;

import com.dxh.Elearning.enums.SkillType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Table(name = "exam_parts")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamPart extends AbstractEntity<Long> {

    @Enumerated(EnumType.STRING)
    SkillType skillType;

    Integer duration; // phút

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id")
    Exam exam;

    @OneToMany(mappedBy = "examPart", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Question> questions;
}

