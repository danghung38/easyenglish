package com.dxh.Elearning.entity;

import com.dxh.Elearning.enums.SkillType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Table(name = "user_answers")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAnswer extends AbstractEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_exam_part_id")
    UserExamPart userExamPart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    Question question;

    Long selectedOptionId; // đáp án trắc nghiệm
    
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    String answerText;     // bài viết dài, transcript Speaking
    
    @Column(length = 1000)
    String audioUrl;       // link ghi âm Speaking

    Double score;          // điểm auto (Listening/Reading)
    Double aiScore;        // điểm AI (Writing/Speaking)
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    String aiFeedback;     // nhận xét từ AI
}

