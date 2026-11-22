import React from "react";
import { Badge, Form } from "react-bootstrap";
import parse from "html-react-parser";

const WritingSection = ({
  currentSection,
  deviceType,
  formRef,
  handleChange,
  currentFormData,
  userAnswerBySection, // Pass this as a prop
}) => {
  // Function to count the words
  const countWords = (text) => {
    if (!text) return 0;
    return text.trim().split(/\s+/).length;
  };

  // Get the current section's user input
  const currentAnswer = userAnswerBySection[currentSection.id] || "";

  return (
    <form className="writing-questions" ref={formRef}>
      <Form.Control
        as={"textarea"}
        rows={deviceType === "mobile" ? 15 : 20}
        placeholder="Write your answer here..."
        name={`task-${currentSection.id}`}
        value={currentAnswer}
        onChange={handleChange}
        style={{
          fontSize: '1rem',
          lineHeight: '1.8',
          padding: '1rem',
          border: '2px solid #e9ecef',
          borderRadius: '8px',
          resize: 'none'
        }}
      />
    </form>
  );
};

export default WritingSection;
