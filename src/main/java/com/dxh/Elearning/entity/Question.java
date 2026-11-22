package com.dxh.Elearning.entity;

import com.dxh.Elearning.enums.SkillType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "questions")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Question extends AbstractEntity<Long> {

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_type", length = 15)
    SkillType skillType;

    @Column(name = "is_section")
    @Builder.Default
    Boolean isSection = false; // true nếu đây là section instruction, false nếu là câu hỏi thật

    @Column(nullable = true, columnDefinition = "TEXT")
    String content; // Nullable - reading section containers don't need content

    @Column(name = "audio_url")
    String audioUrl; // dùng cho Listening hoặc Speaking mẫu

    @Column(name = "image_url")
    String imageUrl; // dùng cho Reading - ảnh đề bài từ S3, Writing Task 1 - biểu đồ/bảng/sơ đồ

    @Column(name = "section", length = 50)
    String section;

    @Column(name = "`explain`", columnDefinition = "TEXT")
    String explain; // Giải thích đáp án cho từng câu hỏi

    @Column(columnDefinition = "TEXT")
    String transcript; // Transcript cho audio của listening section

    @OneToOne
    @JoinColumn(name = "correct_option_id")
    Option correctOption; // Đáp án đúng cho câu trắc nghiệm (READING/LISTENING)

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<Option> options = new ArrayList<>();

    @Column(name = "max_score")
    Double maxScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_part_id")
    ExamPart examPart;
}
