import React from "react";
import parse from "html-react-parser";

const WritingTask = ({ currentSection }) => {
  return (
    <div className="task-instructions bg-light p-4 rounded" style={{lineHeight: '1.8'}}>
      {parse(currentSection.task)}
    </div>
  );
};

export default WritingTask;
