import React, { useState } from "react";
import { Form, Card, Row, Col, Badge } from "react-bootstrap";

const ToeicReadingQuestion = ({ 
  question, 
  questionNumber,
  selectedAnswer,
  onAnswerChange,
  showResult = false,
  userAnswer = null,
  correctAnswer = null
}) => {
  // Support both optionRes (from backend) and options (legacy)
  const options = question.options || question.optionRes || [];
  
  const getOptionLabel = (index) => {
    return String.fromCharCode(65 + index); // A, B, C, D
  };

  const isCorrect = userAnswer === correctAnswer;
  const isUserAnswer = (optionId) => userAnswer === optionId;
  const isCorrectOption = (optionId) => correctAnswer === optionId;

  const getOptionClass = (optionId) => {
    if (!showResult) return "";
    if (isCorrectOption(optionId)) return "border-success bg-success-subtle";
    if (isUserAnswer(optionId) && !isCorrect) return "border-danger bg-danger-subtle";
    return "";
  };

  return (
    <Card 
      className="mb-3 border-0 shadow-sm" 
      id={`question-${question.id}`}
      style={{ scrollMarginTop: '100px' }}
    >
      <Card.Body>
        {/* Question Number and Badge */}
        <div className="d-flex align-items-center mb-3">
          <Badge 
            bg={showResult ? (isCorrect ? "success" : "danger") : "primary"} 
            className="me-2"
          >
            Question {questionNumber}
          </Badge>
          {showResult && (
            <Badge bg={isCorrect ? "success" : "danger"}>
              {isCorrect ? "✓ Correct" : "✗ Incorrect"}
            </Badge>
          )}
        </div>

        {/* Question Content */}
        <p className="fw-bold text-dark mb-3" style={{ fontSize: "1.05rem" }}>
          {question.content}
        </p>

        {/* Options */}
        <Form>
          {options.map((option, index) => {
            const optionLabel = getOptionLabel(index);
            const optionId = option.id;
            
            return (
              <Card 
                key={option.id} 
                className={`mb-2 ${getOptionClass(optionId)} cursor-pointer`}
                style={{ cursor: "pointer" }}
                onClick={() => !showResult && onAnswerChange && onAnswerChange(optionId)}
              >
                <Card.Body className="py-2 px-3">
                  <Form.Check
                    type="radio"
                    id={`q${questionNumber}-option${index}`}
                    name={`question-${questionNumber}`}
                    label={
                      <span className="text-dark">
                        <strong>{optionLabel}.</strong> {option.content}
                      </span>
                    }
                    checked={selectedAnswer === optionId}
                    onChange={() => !showResult && onAnswerChange && onAnswerChange(optionId)}
                    disabled={showResult}
                    className="mb-0"
                  />
                </Card.Body>
              </Card>
            );
          })}
        </Form>

        {/* Show correct answer in result mode */}
        {showResult && !isCorrect && (
          <div className="mt-3 p-2 bg-success-subtle border border-success rounded">
            <small className="text-success fw-bold">
              Correct Answer: {options.find(opt => opt.id === correctAnswer)?.content}
            </small>
          </div>
        )}
      </Card.Body>
    </Card>
  );
};

export default ToeicReadingQuestion;
