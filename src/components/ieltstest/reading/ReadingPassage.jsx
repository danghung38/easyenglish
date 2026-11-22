import React from "react";

const ReadingPassage = ({ section, imageUrl }) => {
  // TOEIC format: Only display image
  const displayImageUrl = imageUrl || (section && section.imageUrl);

  if (!displayImageUrl) {
    return (
      <div className="text-center p-5 text-muted">
        <p>No reading passage image available</p>
      </div>
    );
  }

  const imageStyles = {
    width: "100%",
    height: "auto",
    maxWidth: "100%",
    objectFit: "contain",
    borderRadius: "8px",
    boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
  };

  return (
    <div className="reading-passage-container">
      <img 
        src={displayImageUrl} 
        alt="Reading passage" 
        style={imageStyles}
        loading="lazy"
      />
    </div>
  );
};

export default ReadingPassage;
