package com.dxh.Elearning.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Table(name = "exams")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Exam extends AbstractEntity<Long> {

    @Column(nullable = false)
    String title;

    String description;

    Integer totalDuration; // tổng thời gian phút

    String imageUrl; // URL của ảnh exam từ S3

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<ExamPart> parts;
}

