import React from "react";
import ToeicReadingQuestion from "./ToeicReadingQuestion";
import { Card } from "react-bootstrap";

const ToeicReadingSection = ({
  section,
  questions = [],
  selectedAnswers = {},
  onAnswerChange,
  showResult = false,
  userAnswers = null,
}) => {
  
  const handleAnswerChange = (questionId, optionId) => {
    if (onAnswerChange) {
      onAnswerChange(questionId, optionId);
    }
  };

  if (!questions || questions.length === 0) {
    return (
      <div className="text-center p-5 text-muted">
        <p>No questions available</p>
      </div>
    );
  }

  return (
    <div className="toeic-reading-section p-3">
      {/* Section Header */}
      {section?.sectionName && (
        <Card className="mb-4 border-0 bg-primary bg-opacity-10">
          <Card.Body>
            <h5 className="fw-bold text-primary mb-0">
              {section.sectionName}
            </h5>
            {section.sectionContent && (
              <p className="text-muted mb-0 mt-2">
                {section.sectionContent}
              </p>
            )}
          </Card.Body>
        </Card>
      )}

      {/* Questions */}
      {questions.map((question, index) => {
        const questionNumber = index + 1;
        const questionId = question.id;
        const correctOptionId = question.correctOption?.id || question.correctOptionId;
        
        return (
          <ToeicReadingQuestion
            key={questionId}
            question={question}
            questionNumber={questionNumber}
            selectedAnswer={selectedAnswers[questionId]}
            onAnswerChange={(optionId) => handleAnswerChange(questionId, optionId)}
            showResult={showResult}
            userAnswer={userAnswers?.[`que-${questionId}`]?.user_answer}
            correctAnswer={correctOptionId}
          />
        );
      })}
    </div>
  );
};

export default ToeicReadingSection;
