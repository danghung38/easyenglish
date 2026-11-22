package com.dxh.Elearning.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "user_vocabulary_quizzes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_vocabulary_quiz_term",
                columnNames = {"user_id", "normalized_term"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class UserVocabularyQuiz extends AbstractEntity<Long> {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 80)
    private String term;

    @Column(name = "normalized_term", nullable = false, length = 80)
    private String normalizedTerm;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String meaning;

    private String pronunciation;

    @Column(name = "part_of_speech", length = 50)
    private String partOfSpeech;

    @Column(columnDefinition = "TEXT")
    private String example;

    @Column(name = "quiz_question", nullable = false, columnDefinition = "TEXT")
    private String quizQuestion;

    @Column(name = "option_one", nullable = false, columnDefinition = "TEXT")
    private String optionOne;

    @Column(name = "option_two", nullable = false, columnDefinition = "TEXT")
    private String optionTwo;

    @Column(name = "option_three", nullable = false, columnDefinition = "TEXT")
    private String optionThree;

    @Column(name = "option_four", nullable = false, columnDefinition = "TEXT")
    private String optionFour;

    @Column(name = "correct_answer", nullable = false, columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "source_message", columnDefinition = "TEXT")
    private String sourceMessage;

    @Column(nullable = false)
    private Double confidence;
}
