package com.dxh.Elearning.chat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChatAssistantResult {
    private String answer;
    private String intent;
    private Double confidence;
    private LearningCandidate learningCandidate;
    private QuizCandidate quiz;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class LearningCandidate {
        private String term;
        private String meaning;
        private String pronunciation;
        private String partOfSpeech;
        private String example;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class QuizCandidate {
        private String question;
        private List<String> options;
        private String correctAnswer;
        private String explanation;
    }
}
