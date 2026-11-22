import React, { useState, useEffect } from "react";
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';
import './QuillCustom.css';
import {
  createReadingSection,
  addReadingQuestion,
  addReadingQuestionsBatch,
  addListeningQuestionsBatch,
  createSimpleListeningQuestion,
  updateReadingQuestion,
  updateListeningQuestion,
  updateWritingQuestion,
  updateSpeakingQuestion,
  deleteQuestion,
  getQuestionsByExamIdAndSkillType,
  uploadSectionAudio,
} from "../../services/adminQuestionService";
import { getAllExams, getExamPartsByExamId } from "../../services/adminExamService";
import { DJANGO_BASE_URL } from "../../utils/config";
import Alert from "../../components/Alert";

const QuestionsManagement = () => {
  const [questions, setQuestions] = useState([]);
  const [exams, setExams] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editingQuestion, setEditingQuestion] = useState(null);
  const [selectedExamId, setSelectedExamId] = useState(null);
  const [selectedSkillType, setSelectedSkillType] = useState(null);
  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [currentImageUrl, setCurrentImageUrl] = useState(null);
  const [audioFile, setAudioFile] = useState(null);
  const [isSectionMode, setIsSectionMode] = useState(false);
  const [sectionQuestions, setSectionQuestions] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [readingMode, setReadingMode] = useState('single'); // 'single' | 'createSection' | 'batchQuestions'
  const [listeningMode, setListeningMode] = useState('single'); // 'single' | 'batchQuestions'

  const skillTypes = [
    { value: "LISTENING", label: "Listening" },
    { value: "READING", label: "Reading" },
    { value: "WRITING", label: "Writing" },
    { value: "SPEAKING", label: "Speaking" },
  ];

  const showAlert = (type, message) => {
    const id = Date.now();
    setAlerts(prev => [...prev, { id, type, message }]);
  };

  const removeAlert = (id) => {
    setAlerts(prev => prev.filter(alert => alert.id !== id));
  };

  const [formData, setFormData] = useState({
    examPartId: "",
    questionType: "READING",
    questionNumber: 1,
    questionText: "",
    options: ["", "", "", ""],
    correctTempId: "",
    points: 1,
    explanation: "",
    transcript: "",
    sectionName: "",
    sectionContent: "",
  });

  useEffect(() => {
    fetchExams();
  }, []);

  useEffect(() => {
    if (selectedExamId) {
      fetchExamParts();
    }
  }, [selectedExamId]);

  useEffect(() => {
    if (selectedExamId && selectedSkillType) {
      fetchQuestions();
    }
  }, [selectedExamId, selectedSkillType]);

  const fetchExamParts = async () => {
    if (!selectedExamId) {
      console.log("⚠️ fetchExamParts: No selectedExamId, skipping");
      return;
    }
    
    try {
      console.log("🔍 Fetching exam parts for exam ID:", selectedExamId);
      const response = await getExamPartsByExamId(selectedExamId);
      console.log("📦 Exam parts response:", response);
      
      if (response.code === 200 && response.result) {
        const examParts = response.result.items || [];
        console.log("✅ Fetched exam parts:", examParts);
        console.log("✅ Number of exam parts:", examParts.length);
        
        if (examParts.length === 0) {
          console.warn("⚠️ No exam parts found for exam ID:", selectedExamId);
          showAlert('warning', 'No exam parts found for this exam. Please create exam parts first in ExamParts Management.');
        }
        
        // Update the selected exam with examParts
        setExams(prevExams => {
          const updated = prevExams.map(exam => 
            exam.id === parseInt(selectedExamId) 
              ? { ...exam, examParts: examParts }
              : exam
          );
          console.log("🔄 Updated exams state:", updated);
          return updated;
        });
      }
    } catch (err) {
      console.error("❌ Failed to fetch exam parts:", err);
      showAlert('error', 'Failed to fetch exam parts. Please try again.');
    }
  };

  const fetchExams = async () => {
    try {
      const response = await getAllExams(1, 100, "id");
      console.log("📦 Fetched exams response:", response);
      if (response.code === 200 && response.result) {
        const examsData = response.result.items || [];
        console.log("📋 Exams data:", examsData);
        console.log("📋 First exam examParts:", examsData[0]?.examParts);
        setExams(examsData);
      }
    } catch (err) {
      console.error("Failed to fetch exams:", err);
    }
  };

  const fetchQuestions = async () => {
    if (!selectedExamId || !selectedSkillType) return;

    try {
      setLoading(true);
      const response = await getQuestionsByExamIdAndSkillType(selectedExamId, selectedSkillType);
      if (response.code === 200) {
        setQuestions(response.result || []);
      }
      setError(null);
    } catch (err) {
      setError("Failed to fetch questions: " + err.message);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenModal = (question = null, sectionMode = false, mode = 'single') => {
    setIsSectionMode(sectionMode);
    
    // Set appropriate mode based on question type
    if (selectedSkillType === 'READING') {
      setReadingMode(mode);
      setListeningMode('single');
    } else if (selectedSkillType === 'LISTENING') {
      setListeningMode(mode);
      setReadingMode('single');
    } else {
      setReadingMode('single');
      setListeningMode('single');
    }
    
    setSectionQuestions([]);
    
    if (question) {
      setEditingQuestion(question);
      
      // Find correct option for READING/LISTENING
      let correctTempId = "";
      if (question.correctOptionId && question.optionRes) {
        const correctIndex = question.optionRes.findIndex(opt => opt.id === question.correctOptionId);
        if (correctIndex >= 0) {
          correctTempId = `temp-${correctIndex}`;
        }
      }
      
      setFormData({
        examPartId: question.examPartId || "",
        questionType: question.skillType || "READING",
        questionNumber: 1,
        questionText: question.content || "",
        options: question.optionRes?.map(opt => opt.content) || ["", "", "", ""],
        correctTempId: correctTempId,
        points: question.maxScore || 1,
        explanation: question.explain || "",
        transcript: question.transcript || "",
        section: question.section || "",
        sectionName: question.section || "",
      });
      setImageFile(null);
      setAudioFile(null);
    } else {
      setEditingQuestion(null);
      setFormData({
        examPartId: "",
        questionType: selectedSkillType || "READING",
        questionNumber: 1,
        questionText: "",
        options: ["", "", "", ""],
        correctTempId: "",
        points: 1,
        explanation: "",
        section: "",
        sectionName: "",
        sectionContent: "",
      });
      setImageFile(null);
      setAudioFile(null);
    }
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingQuestion(null);
    setImageFile(null);
    setImagePreview(null);
    setCurrentImageUrl(null);
    setAudioFile(null);
    setReadingMode('single');
    setListeningMode('single');
    setSectionQuestions([]);
  };

  const handleInputChange = async (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    // When section is selected in Upload Section Image mode, fetch current image
    if (name === 'sectionName' && readingMode === 'createSection' && value) {
      try {
        const readingExamPart = exams
          .find(exam => exam.id === parseInt(selectedExamId))
          ?.examParts?.find(part => part.skillType === "READING");
        
        if (readingExamPart) {
          const response = await getQuestionsByExamIdAndSkillType(selectedExamId, "READING");
          const sectionQuestions = response.result?.filter(q => q.section === value) || [];
          
          // Get image URL from any question in the section
          const questionWithImage = sectionQuestions.find(q => q.imageUrl);
          if (questionWithImage) {
            setCurrentImageUrl(questionWithImage.imageUrl);
          } else {
            setCurrentImageUrl(null);
          }
        }
      } catch (error) {
        console.error("Error fetching section image:", error);
      }
    }
  };

  const handleOptionChange = (index, value) => {
    const newOptions = [...formData.options];
    newOptions[index] = value;
    setFormData((prev) => ({
      ...prev,
      options: newOptions,
    }));
  };

  const handleImageChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      setImageFile(file);
      
      // Create preview URL
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreview(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleAudioChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setAudioFile(e.target.files[0]);
    }
  };

  // Note: Audio file is only required for Section Mode (multiple questions sharing one audio)
  // Single listening questions do NOT require audio upload

  const handleAddQuestionToSection = () => {
    // Validate
    const filledOptions = formData.options.filter(opt => opt.trim() !== "");
    if (filledOptions.length === 0) {
      showAlert('error', 'Please add at least one option');
      return;
    }
    
    if (!formData.correctTempId) {
      showAlert('error', 'Please select the correct answer');
      return;
    }
    
    // Create options with tempId format (a1, a2, a3, a4)
    const optionsWithTempId = filledOptions.map((content, index) => ({
      tempId: `a${index + 1}`,
      content: content
    }));
    
    // Find correct answer text
    const correctIndex = formData.correctTempId.startsWith('a') 
      ? parseInt(formData.correctTempId.replace("a", "")) - 1
      : parseInt(formData.correctTempId.replace("temp-", ""));
    const correctAnswerText = filledOptions[correctIndex] || filledOptions[0];
    const correctTempId = `a${correctIndex + 1}`;
    
    const newQuestion = {
      content: formData.questionText,
      maxScore: formData.points,
      options: optionsWithTempId,
      correctTempId: correctTempId,
      correctAnswer: correctAnswerText, // For display only
    };
    
    setSectionQuestions([...sectionQuestions, newQuestion]);
    
    // Reset form
    setFormData(prev => ({
      ...prev,
      questionText: "",
      options: ["", "", "", ""],
      correctTempId: "",
      points: 1,
    }));
  };

  const handleRemoveQuestionFromSection = (index) => {
    setSectionQuestions(sectionQuestions.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    console.log("=== Form submitted ===");
    console.log("Form data:", formData);
    console.log("Is section mode:", isSectionMode);
    console.log("Editing question:", editingQuestion);
    console.log("Selected Skill Type:", selectedSkillType);
    console.log("Selected Exam ID:", selectedExamId);
    
    // Clear previous errors
    setError(null);
    
    try {
      console.log("🔍 Checking conditions...");
      console.log("- isSectionMode:", isSectionMode);
      console.log("- formData.questionType:", formData.questionType);
      console.log("- editingQuestion:", editingQuestion);
      
      if (isSectionMode && formData.questionType === "LISTENING") {
        console.log("✓ Entering LISTENING SECTION MODE branch");
        // Section mode - upload audio for section (questions already created)
        if (!audioFile) {
          setError("Please upload an audio file for the section");
          return;
        }
        if (!formData.sectionName) {
          setError("Please select a section name (SECTION1-SECTION5)");
          return;
        }

        // Find examPartId from selected exam
        const listeningExamPart = exams
          .find(exam => exam.id === parseInt(selectedExamId))
          ?.examParts?.find(part => part.skillType === "LISTENING");

        if (!listeningExamPart) {
          setError("No Listening exam part found for this exam");
          return;
        }

        console.log("📤 Uploading audio for section:", formData.sectionName);
        const response = await uploadSectionAudio(
          listeningExamPart.id, 
          formData.sectionName.toUpperCase(), 
          audioFile,
          formData.transcript || null
        );
        showAlert('success', `Successfully uploaded audio for ${formData.sectionName} (${response.result?.length || 0} questions updated)`);
      } else if (readingMode === 'createSection') {
        console.log("✓ Entering READING CREATE SECTION MODE branch");
        // Mode 1: Create section with image only
        if (!imageFile) {
          setError("Please upload an image file for the section");
          return;
        }
        if (!formData.sectionName || formData.sectionName.trim() === "") {
          setError("Please enter a section name");
          return;
        }

        // Find examPartId from selected exam
        const readingExamPart = exams
          .find(exam => exam.id === parseInt(selectedExamId))
          ?.examParts?.find(part => part.skillType === "READING");

        if (!readingExamPart) {
          setError("No Reading exam part found for this exam");
          return;
        }

        console.log("📤 Creating reading section:", formData.sectionName);
        await createReadingSection(readingExamPart.id, formData.sectionName, imageFile);
        showAlert('success', `Successfully created reading section '${formData.sectionName}' with image. You can now add questions to this section.`);
      } else if (readingMode === 'batchQuestions') {
        console.log("✓ Entering READING BATCH QUESTIONS MODE branch");
        console.log("📊 Section Questions:", sectionQuestions);
        console.log("📝 Section Name:", formData.sectionName);
        
        // Mode 3: Add multiple questions to existing section
        if (!formData.sectionName || formData.sectionName.trim() === "") {
          setError("Please select a section");
          return;
        }
        if (sectionQuestions.length === 0) {
          setError("Please add at least one question using 'Add Question to Passage' button");
          return;
        }

        // Find examPartId from selected exam
        const readingExamPart = exams
          .find(exam => exam.id === parseInt(selectedExamId))
          ?.examParts?.find(part => part.skillType === "READING");

        if (!readingExamPart) {
          setError("No Reading exam part found for this exam");
          return;
        }

        console.log("📤 Adding questions batch to section:", formData.sectionName);
        console.log("📦 Questions to send:", sectionQuestions);
        const questionsForAPI = sectionQuestions.map(({ correctAnswer, ...rest }) => rest);
        console.log("🚀 Calling API with:", { examPartId: readingExamPart.id, section: formData.sectionName, questions: questionsForAPI });
        const response = await addReadingQuestionsBatch(readingExamPart.id, formData.sectionName, questionsForAPI);
        console.log("✅ API Response:", response);
        
        showAlert('success', `Successfully added ${response.result?.length || 0} questions to section '${formData.sectionName}'`);
      } else if (listeningMode === 'batchQuestions') {
        console.log("✓ Entering LISTENING BATCH QUESTIONS MODE branch");
        console.log("📊 Section Questions:", sectionQuestions);
        console.log("📝 Section Name:", formData.sectionName);
        
        // Mode: Add multiple listening questions to existing section
        if (!formData.sectionName || formData.sectionName.trim() === "") {
          setError("Please select a section");
          return;
        }
        if (sectionQuestions.length === 0) {
          setError("Please add at least one question using 'Add Question to Section' button");
          return;
        }

        // Find examPartId from selected exam
        const listeningExamPart = exams
          .find(exam => exam.id === parseInt(selectedExamId))
          ?.examParts?.find(part => part.skillType === "LISTENING");

        if (!listeningExamPart) {
          setError("No Listening exam part found for this exam");
          return;
        }

        console.log("📤 Adding listening questions batch to section:", formData.sectionName);
        console.log("📦 Questions to send:", sectionQuestions);
        const questionsForAPI = sectionQuestions.map(({ correctAnswer, ...rest }) => rest);
        console.log("🚀 Calling API with:", { examPartId: listeningExamPart.id, section: formData.sectionName, questions: questionsForAPI });
        const response = await addListeningQuestionsBatch(listeningExamPart.id, formData.sectionName, questionsForAPI);
        console.log("✅ API Response:", response);
        
        showAlert('success', `Successfully added ${response.result?.length || 0} listening questions to section '${formData.sectionName}'`);
      } else if (editingQuestion) {
        console.log("✓ Entering EDIT MODE branch");
        // Edit mode
        if (formData.questionType === "READING") {
          await updateReadingQuestion(editingQuestion.id, formData);
          showAlert('success', 'Reading question updated successfully');
        } else if (formData.questionType === "LISTENING") {
          await updateListeningQuestion(editingQuestion.id, formData, audioFile);
          showAlert('success', 'Listening question updated successfully');
        } else if (formData.questionType === "WRITING") {
          const writingData = {
            content: formData.questionText,
            maxScore: formData.points,
            section: formData.sectionName,
            explain: formData.explanation || null
          };
          await updateWritingQuestion(editingQuestion.id, writingData, imageFile);
          showAlert('success', 'Writing question updated successfully');
        } else if (formData.questionType === "SPEAKING") {
          const speakingData = {
            content: formData.questionText,
            maxScore: formData.points,
            section: formData.sectionName,
            explain: formData.explanation || null
          };
          await updateSpeakingQuestion(editingQuestion.id, speakingData);
          showAlert('success', 'Speaking question updated successfully');
        }
      } else {
        console.log("✓ Entering SINGLE QUESTION MODE branch");
        // Single question mode - tìm examPartId từ selected exam và skill type
        console.log("📋 Exams array:", exams);
        console.log("🔍 Looking for exam with ID:", selectedExamId, "Type:", typeof selectedExamId);
        
        const selectedExam = exams.find(exam => exam.id === parseInt(selectedExamId));
        console.log("📝 Selected exam:", selectedExam);
        
        if (!selectedExam) {
          console.error("❌ No exam found!");
          setError("Please select an exam first");
          return;
        }
        
        console.log("📦 Exam parts:", selectedExam.examParts);
        console.log("🎯 Looking for examPart with skillType:", formData.questionType);
        
        const examPart = selectedExam.examParts?.find(part => part.skillType === formData.questionType);
        console.log("✅ Found examPart:", examPart);
        
        if (!examPart) {
          console.error("❌ No exam part found for this skill type!");
          setError(`No ${formData.questionType} exam part found for this exam`);
          return;
        }
        
        console.log("🎉 Using examPart ID:", examPart.id);
        
        // Validation for Reading and Listening only (they need options)
        if (formData.questionType === "READING" || formData.questionType === "LISTENING") {
          console.log("🔍 Validating options:", formData.options);
          const filledOptions = formData.options.filter(opt => opt.trim() !== "");
          console.log("✓ Filled options:", filledOptions, "Count:", filledOptions.length);
          
          if (filledOptions.length === 0) {
            console.error("❌ No options provided");
            setError("Please add at least one option");
            return;
          }
          
          console.log("🔍 Checking correctTempId:", formData.correctTempId);
          if (!formData.correctTempId) {
            console.error("❌ No correct answer selected");
            setError("Please select a correct answer");
            return;
          }
        }
        
        // Process based on question type
        if (formData.questionType === "READING" || formData.questionType === "LISTENING") {
          // Tạo options với tempId đơn giản (a1, a2, a3, a4...)
          console.log("🔧 Creating options with tempId...");
          const filledOptions = formData.options.filter(opt => opt.trim() !== "");
          const optionsWithTempId = filledOptions.map((content, index) => ({
            tempId: `a${index + 1}`,
            content: content
          }));
          console.log("✓ Options with tempId:", optionsWithTempId);
          
          // Tìm correctTempId tương ứng
          console.log("🔧 Processing correctTempId:", formData.correctTempId);
          const correctIndex = parseInt(formData.correctTempId.replace("temp-", ""));
          const correctTempId = `a${correctIndex + 1}`;
          console.log("✓ Correct index:", correctIndex, "-> Correct tempId:", correctTempId);
          
          // Chuẩn bị data gửi lên backend - đơn giản và rõ ràng
          const requestData = {
            examPartId: examPart.id,
            skillType: formData.questionType,
            content: formData.questionText,
            maxScore: formData.points,
            options: optionsWithTempId,
            correctTempId: correctTempId,
            section: formData.sectionName || null,
            explain: formData.explanation || null
          };
          
          console.log("=== Sending request to API ===");
          console.log("Request data:", JSON.stringify(requestData, null, 2));
          console.log("Question Type check:", formData.questionType);
          
          if (formData.questionType === "READING") {
            // Validate section name is required for reading
            if (!formData.sectionName || formData.sectionName.trim() === "") {
              setError("Section name is required for reading questions");
              return;
            }
            
            console.log("→ Calling addReadingQuestion API...");
            console.log("API URL:", `${DJANGO_BASE_URL}/questions/reading/question`);
            const response = await addReadingQuestion(requestData);
            console.log("✓ Response:", response);
            showAlert('success', `Reading question added to section '${formData.sectionName}' successfully`);
          } else if (formData.questionType === "LISTENING") {
            console.log("→ Calling createSimpleListeningQuestion API...");
            console.log("API URL:", `${DJANGO_BASE_URL}/questions/listening/simple`);
            const response = await createSimpleListeningQuestion(requestData);
            console.log("✓ Response:", response);
            showAlert('success', `Listening question created successfully${formData.sectionName ? ` in ${formData.sectionName}` : ''}`);
          }
        } else if (formData.questionType === "WRITING") {
          // Writing question - requires section (task1/task2)
          if (!formData.sectionName) {
            setError("Please select a task (task1 or task2)");
            return;
          }
          if (formData.sectionName === 'task1' && !imageFile) {
            setError("Task 1 requires an image for charts/graphs");
            return;
          }
          
          const writingData = {
            examPartId: examPart.id,
            skillType: formData.questionType,
            content: formData.questionText,
            maxScore: formData.points,
            section: formData.sectionName,
            explain: formData.explanation || null
          };
          
          console.log("→ Calling createWritingQuestion API...");
          const formDataToSend = new FormData();
          formDataToSend.append('question', new Blob([JSON.stringify(writingData)], { type: 'application/json' }));
          if (imageFile) {
            formDataToSend.append('image', imageFile);
          }
          
          // Get auth token using the same method as other APIs
          const authTokensStr = localStorage.getItem('authTokens');
          let authHeaders = {};
          if (authTokensStr) {
            try {
              const authTokens = JSON.parse(authTokensStr);
              const token = authTokens.token;
              if (token) {
                authHeaders = { 'Authorization': `Bearer ${token}` };
              }
            } catch (e) {
              console.error('Error parsing auth tokens:', e);
            }
          }
          
          const response = await fetch(`${DJANGO_BASE_URL}/questions/writing`, {
            method: 'POST',
            headers: authHeaders,
            body: formDataToSend
          });
          
          if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Failed to create writing question');
          }
          
          console.log("✓ Writing question created");
          showAlert('success', `Writing question (${formData.sectionName.toUpperCase()}) created successfully`);
        } else if (formData.questionType === "SPEAKING") {
          // Speaking question - requires section (part1/part2/part3)
          const speakingData = {
            examPartId: examPart.id,
            skillType: formData.questionType,
            content: formData.questionText,
            maxScore: formData.points,
            section: formData.sectionName || null,
            explain: formData.explanation || null
          };
          
          console.log("→ Calling createSpeakingQuestion API...");
          
          // Get auth token using the same method as other APIs
          const authTokensStr = localStorage.getItem('authTokens');
          let authHeaders = { 'Content-Type': 'application/json' };
          if (authTokensStr) {
            try {
              const authTokens = JSON.parse(authTokensStr);
              const token = authTokens.token;
              if (token) {
                authHeaders['Authorization'] = `Bearer ${token}`;
              }
            } catch (e) {
              console.error('Error parsing auth tokens:', e);
            }
          }
          
          const response = await fetch(`${DJANGO_BASE_URL}/questions/speaking`, {
            method: 'POST',
            headers: authHeaders,
            body: JSON.stringify(speakingData)
          });
          
          if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Failed to create speaking question');
          }
          
          console.log("✓ Speaking question created");
          showAlert('success', 'Speaking question created successfully');
        } else {
          console.error("❌ Unknown question type:", formData.questionType);
          showAlert('error', `Unknown question type: ${formData.questionType}`);
          return;
        }
      }
      
      console.log("Success! Closing modal and fetching questions...");
      handleCloseModal();
      fetchQuestions();
    } catch (err) {
      console.error("Error in handleSubmit:", err);
      showAlert('error', err.response?.data?.message || err.message || 'Failed to save question');
    }
  };

  const handleDelete = async (questionId) => {
    if (window.confirm("Are you sure you want to delete this question?")) {
      try {
        await deleteQuestion(questionId);
        showAlert('success', 'Question deleted successfully');
        fetchQuestions();
      } catch (err) {
        console.error("Error deleting question:", err);
        showAlert('error', err.response?.data?.message || 'Failed to delete question');
      }
    }
  };

  return (
    <div className="admin-page">
      {/* Alerts Container */}
      <div className="alerts-container">
        {alerts.map(alert => (
          <Alert
            key={alert.id}
            type={alert.type}
            message={alert.message}
            onClose={() => removeAlert(alert.id)}
          />
        ))}
      </div>

      {/* Main Content */}
      <div className="admin-page">
      <div className="admin-page-header">
        <h1>Questions Management</h1>
        <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
          {selectedSkillType === "READING" ? (
            <>
              <button
                className="btn-primary"
                onClick={() => handleOpenModal(null, false, 'createSection')}
                disabled={!selectedExamId}
                style={{ backgroundColor: "#0284c7" }}
              >
                🖼️ Upload Section Image
              </button>
              <button
                className="btn-primary"
                onClick={() => handleOpenModal()}
                disabled={!selectedExamId}
                style={{ backgroundColor: "#16a34a" }}
              >
                ➕ Add Single Question
              </button>
              <button
                className="btn-primary"
                onClick={() => handleOpenModal(null, false, 'batchQuestions')}
                disabled={!selectedExamId}
                style={{ backgroundColor: "#059669" }}
              >
                📝 Add Questions Batch
              </button>
            </>
          ) : selectedSkillType === "LISTENING" ? (
            <>
              <button
                className="btn-primary"
                onClick={() => handleOpenModal()}
                disabled={!selectedExamId}
              >
                + Add Single Question
              </button>
              <button
                className="btn-primary"
                onClick={() => handleOpenModal(null, false, 'batchQuestions')}
                disabled={!selectedExamId}
                style={{ backgroundColor: "#059669" }}
              >
                📝 Add Questions Batch
              </button>
              <button
                className="btn-primary"
                onClick={() => handleOpenModal(null, true)}
                disabled={!selectedExamId}
                style={{ backgroundColor: "#dc2626" }}
              >
                🔊 Upload Section Audio
              </button>
            </>
          ) : (
            <button
              className="btn-primary"
              onClick={() => handleOpenModal()}
              disabled={!selectedExamId || !selectedSkillType}
            >
              + Add Single Question
            </button>
          )}
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div style={{ display: "flex", gap: "20px", marginBottom: "20px" }}>
        <div className="form-group" style={{ flex: 1 }}>
          <label>Select Exam:</label>
          <select
            value={selectedExamId || ""}
            onChange={(e) => {
              console.log("🔄 Exam selection changed to:", e.target.value);
              setSelectedExamId(e.target.value);
              setQuestions([]);
            }}
            style={{ padding: "10px", fontSize: "1rem" }}
          >
            <option value="">-- Select an Exam --</option>
            {exams.map((exam) => (
              <option key={exam.id} value={exam.id}>
                {exam.title} {exam.examParts ? `(${exam.examParts.length} parts)` : '(no parts)'}
              </option>
            ))}
          </select>
        </div>

        <div className="form-group" style={{ flex: 1 }}>
          <label>Select Skill Type:</label>
          <select
            value={selectedSkillType || ""}
            onChange={(e) => {
              setSelectedSkillType(e.target.value);
              setQuestions([]);
            }}
            disabled={!selectedExamId}
            style={{ padding: "10px", fontSize: "1rem" }}
          >
            <option value="">-- Select a Skill Type --</option>
            {skillTypes.map((skill) => (
              <option key={skill.value} value={skill.value}>
                {skill.label}
              </option>
            ))}
          </select>
        </div>
      </div>

      {!selectedExamId || !selectedSkillType ? (
        <div className="empty-state">
          <div className="empty-icon">❓</div>
          <h3>Please select an exam and skill type</h3>
          <p>Choose an exam and skill type to manage questions</p>
        </div>
      ) : loading ? (
        <div className="loading">Loading questions...</div>
      ) : questions.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">❓</div>
          <h3>No questions found</h3>
          <p>Get started by adding a new question</p>
        </div>
      ) : (
        <table className="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Skill Type</th>
              <th>Section</th>
              <th>Content</th>
              {(selectedSkillType === "LISTENING" || selectedSkillType === "READING") && (
                <th>Correct Answer</th>
              )}
              <th>Max Score</th>
              <th>Options</th>
              <th>Media</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {questions.map((question, index) => {
              // Find correct answer text for READING/LISTENING
              let correctAnswerText = "-";
              if (question.correctOptionId && question.optionRes) {
                const correctOption = question.optionRes.find(opt => opt.id === question.correctOptionId);
                correctAnswerText = correctOption ? correctOption.content : "-";
              }

              return (
                <tr key={question.id}>
                  <td>{question.id}</td>
                  <td>{question.skillType}</td>
                  <td>{question.section || "-"}</td>
                  <td title={question.content}>{question.content?.substring(0, 40) || "..."}...</td>
                  {(selectedSkillType === "LISTENING" || selectedSkillType === "READING") && (
                    <td>{correctAnswerText || "-"}</td>
                  )}
                  <td>{question.maxScore || "-"}</td>
                  <td>{question.optionRes?.length > 0 ? `${question.optionRes.length} options` : "-"}</td>
                  <td>
                    {question.audioUrl && <span title={question.audioUrl}>🔊 </span>}
                    {question.imageUrl && <span title={question.imageUrl}>🖼️ </span>}
                    {!question.audioUrl && !question.imageUrl && "-"}
                  </td>
                  <td>
                    <div className="action-buttons">
                      <button
                        className="btn-edit"
                        onClick={() => handleOpenModal(question)}
                      >
                        Edit
                      </button>
                      <button
                        className="btn-delete"
                        onClick={() => handleDelete(question.id)}
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      )}

      {showModal && (
        <div className="modal-overlay" onClick={handleCloseModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: (isSectionMode || readingMode !== 'single' || listeningMode !== 'single') ? "800px" : "600px" }}>
            <h2>
              {editingQuestion ? "Edit Question" : 
               readingMode === 'createSection' ? "🖼️ Upload Reading Section Image" :
               readingMode === 'batchQuestions' ? "📝 Add Multiple Reading Questions to Section" :
               listeningMode === 'batchQuestions' ? "📝 Add Multiple Listening Questions to Section" :
               isSectionMode ? "Upload Audio for Section" : 
               "Add New Question"}
            </h2>
            <form onSubmit={handleSubmit}>
              {isSectionMode && formData.questionType === "LISTENING" && (
                <>
                  <div className="alert" style={{ backgroundColor: "#e0f2fe", padding: "15px", borderRadius: "8px", marginBottom: "20px", border: "1px solid #0284c7" }}>
                    <p style={{ margin: 0, color: "#0c4a6e", fontSize: "0.95rem" }}>
                      ℹ️ <strong>Upload audio for existing questions:</strong> First create listening questions with a section assigned, then use this form to upload the audio file for that section. All questions in the section will share the same audio.
                    </p>
                  </div>
                  <div className="form-group">
                    <label>Section Name *</label>
                    <select
                      name="sectionName"
                      value={formData.sectionName}
                      onChange={handleInputChange}
                      required
                    >
                      <option value="">-- Select Section --</option>
                      <option value="SECTION1">Section 1</option>
                      <option value="SECTION2">Section 2</option>
                      <option value="SECTION3">Section 3</option>
                      <option value="SECTION4">Section 4</option>
                      <option value="SECTION5">Section 5</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Section Audio File *</label>
                    <input
                      type="file"
                      accept="audio/*"
                      onChange={handleAudioChange}
                      required={!audioFile}
                    />
                    {audioFile && (
                      <p style={{ marginTop: "10px", fontSize: "0.9rem", color: "#059669" }}>
                        ✓ Selected: {audioFile.name}
                      </p>
                    )}
                  </div>
                  <div className="form-group">
                    <label>Audio Transcript (Optional)</label>
                    <textarea
                      name="transcript"
                      value={formData.transcript}
                      onChange={handleInputChange}
                      rows="8"
                      placeholder="Enter the transcript of the audio file here...\n\nExample:\nSpeaker 1: Hello, welcome to our store.\nSpeaker 2: Thank you! I'm looking for..."
                      style={{ fontFamily: 'monospace', fontSize: '0.9rem' }}
                    />
                    <small style={{ color: '#666', fontSize: '0.85rem', marginTop: '5px', display: 'block' }}>
                      Provide the full text transcript of the audio for student reference
                    </small>
                  </div>
                </>
              )}

              {readingMode === 'createSection' && (
                <>
                  <div className="alert" style={{ backgroundColor: "#dbeafe", padding: "15px", borderRadius: "8px", marginBottom: "20px", border: "1px solid #0284c7" }}>
                    <p style={{ margin: 0, color: "#1e3a8a", fontSize: "0.95rem" }}>
                      ℹ️ <strong>Upload image for existing questions:</strong> First create reading questions with a section assigned, then use this form to upload the image file for that section. All questions in the section will share the same image.
                    </p>
                  </div>
                  <div className="form-group">
                    <label>Section *</label>
                    <select
                      name="sectionName"
                      value={formData.sectionName}
                      onChange={handleInputChange}
                      required
                    >
                      <option value="">-- Select Section --</option>
                      <option value="SECTION1">Section 1</option>
                      <option value="SECTION2">Section 2</option>
                      <option value="SECTION3">Section 3</option>
                      <option value="SECTION4">Section 4</option>
                      <option value="SECTION5">Section 5</option>
                    </select>
                    <small style={{ color: '#666', fontSize: '0.85rem', marginTop: '5px', display: 'block' }}>
                      Select which section this image belongs to
                    </small>
                  </div>
                  <div className="form-group">
                    <label>Section Image File {currentImageUrl && "(Optional - Leave empty to keep current)"}</label>
                    <input
                      type="file"
                      accept="image/*"
                      onChange={handleImageChange}
                      required={!imageFile && !currentImageUrl}
                    />
                    {currentImageUrl && !imageFile && (
                      <div style={{ marginTop: "15px" }}>
                        <p style={{ fontSize: "0.9rem", color: "#059669", marginBottom: "10px", fontWeight: "500" }}>
                          📸 Current Image:
                        </p>
                        <div style={{ 
                          border: "2px solid #059669", 
                          borderRadius: "8px", 
                          padding: "10px", 
                          backgroundColor: "#f0fdf4",
                          textAlign: "center"
                        }}>
                          <img 
                            src={currentImageUrl} 
                            alt="Current Section" 
                            style={{ 
                              maxWidth: "100%", 
                              maxHeight: "300px", 
                              objectFit: "contain",
                              borderRadius: "4px"
                            }}
                          />
                          <p style={{ 
                            marginTop: "8px", 
                            fontSize: "0.85rem", 
                            color: "#166534" 
                          }}>
                            Current image will be kept if you don't upload a new one
                          </p>
                        </div>
                      </div>
                    )}
                    {imageFile && (
                      <div style={{ marginTop: "15px" }}>
                        <p style={{ fontSize: "0.9rem", color: "#0284c7", marginBottom: "10px", fontWeight: "500" }}>
                          {currentImageUrl ? "🔄 New Image (will replace current):" : "✓ Selected:"}
                        </p>
                        <p style={{ fontSize: "0.85rem", color: "#666", marginBottom: "10px" }}>
                          {imageFile.name}
                        </p>
                        {imagePreview && (
                          <div style={{ 
                            border: "2px solid #0284c7", 
                            borderRadius: "8px", 
                            padding: "10px", 
                            backgroundColor: "#f0f9ff",
                            textAlign: "center"
                          }}>
                            <img 
                              src={imagePreview} 
                              alt="Preview" 
                              style={{ 
                                maxWidth: "100%", 
                                maxHeight: "300px", 
                                objectFit: "contain",
                                borderRadius: "4px"
                              }}
                            />
                            <p style={{ 
                              marginTop: "8px", 
                              fontSize: "0.85rem", 
                              color: "#0369a1" 
                            }}>
                              New image preview - Click "Choose File" again to change
                            </p>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                </>
              )}

              {readingMode === 'batchQuestions' && (
                <>
                  <div className="alert" style={{ backgroundColor: "#dcfce7", padding: "15px", borderRadius: "8px", marginBottom: "20px", border: "1px solid #16a34a" }}>
                    <p style={{ margin: 0, color: "#14532d", fontSize: "0.95rem" }}>
                      ℹ️ <strong>Add multiple questions to section.</strong> Select a section and add questions. After creating questions, use "Upload Section Image" to add the passage image.
                    </p>
                  </div>
                  <div className="form-group">
                    <label>Section *</label>
                    <select
                      name="sectionName"
                      value={formData.sectionName}
                      onChange={handleInputChange}
                      required
                    >
                      <option value="">-- Select Section --</option>
                      <option value="SECTION1">Section 1</option>
                      <option value="SECTION2">Section 2</option>
                      <option value="SECTION3">Section 3</option>
                      <option value="SECTION4">Section 4</option>
                      <option value="SECTION5">Section 5</option>
                    </select>
                    <small style={{ color: '#666', fontSize: '0.85rem', marginTop: '5px', display: 'block' }}>
                      Select which section these questions belong to
                    </small>
                  </div>

                  {/* Questions List for Reading Passage */}
                  <div style={{ marginTop: "20px", padding: "15px", backgroundColor: "#f9fafb", borderRadius: "8px", border: "1px solid #e5e7eb" }}>
                    <h3 style={{ marginTop: 0, marginBottom: "15px", fontSize: "1.1rem" }}>Questions ({sectionQuestions.length})</h3>
                    
                    {/* Add Question Form */}
                    <div className="form-group">
                      <label>Question Text (Optional)</label>
                      <textarea
                        name="questionText"
                        value={formData.questionText}
                        onChange={handleInputChange}
                        rows="2"
                        placeholder="Optional - Leave blank if questions share common passage text"
                      />
                    </div>

                    <div className="form-group">
                      <label>Options *</label>
                      {formData.options.map((option, index) => (
                        <input
                          key={index}
                          type="text"
                          value={option}
                          onChange={(e) => handleOptionChange(index, e.target.value)}
                          placeholder={`Option ${index + 1}`}
                          style={{ marginBottom: "10px" }}
                        />
                      ))}
                    </div>

                    <div className="form-group">
                      <label>Correct Answer *</label>
                      <select
                        name="correctTempId"
                        value={formData.correctTempId}
                        onChange={handleInputChange}
                      >
                        <option value="">-- Select correct option --</option>
                        {formData.options.filter(opt => opt.trim() !== "").map((option, idx) => (
                          <option key={idx} value={`a${idx + 1}`}>
                            Option {idx + 1}: {option}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="form-group">
                      <label>Points *</label>
                      <input
                        type="number"
                        name="points"
                        value={formData.points}
                        onChange={handleInputChange}
                        min="1"
                      />
                    </div>

                    <button
                      type="button"
                      className="btn-primary"
                      onClick={handleAddQuestionToSection}
                      style={{ width: "100%", marginBottom: "15px" }}
                    >
                      + Add Question to Passage
                    </button>

                    {/* Display Added Questions */}
                    {sectionQuestions.length > 0 && (
                      <div style={{ marginTop: "15px" }}>
                        {sectionQuestions.map((q, index) => (
                          <div key={index} style={{ padding: "10px", backgroundColor: "white", borderRadius: "6px", marginBottom: "10px", border: "1px solid #d1d5db" }}>
                            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "start" }}>
                              <div style={{ flex: 1 }}>
                                <strong>Q{index + 1}:</strong> {q.content}
                                <div style={{ marginTop: "5px", fontSize: "0.9rem", color: "#059669" }}>
                                  ✓ Correct: {q.correctAnswer} | Points: {q.maxScore}
                                </div>
                              </div>
                              <button
                                type="button"
                                onClick={() => handleRemoveQuestionFromSection(index)}
                                style={{ marginLeft: "10px", padding: "5px 10px", backgroundColor: "#ef4444", color: "white", border: "none", borderRadius: "4px", cursor: "pointer" }}
                              >
                                Remove
                              </button>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </>
              )}

              {listeningMode === 'batchQuestions' && (
                <>
                  <div className="alert" style={{ backgroundColor: "#e0f2fe", padding: "15px", borderRadius: "8px", marginBottom: "20px", border: "1px solid #0284c7" }}>
                    <p style={{ margin: 0, color: "#0c4a6e", fontSize: "0.95rem" }}>
                      ℹ️ <strong>Add multiple listening questions to section.</strong> Select a section and add questions. After creating questions, use "Upload Section Audio" to add the audio file.
                    </p>
                  </div>
                  <div className="form-group">
                    <label>Section *</label>
                    <select
                      name="sectionName"
                      value={formData.sectionName}
                      onChange={handleInputChange}
                      required
                    >
                      <option value="">-- Select Section --</option>
                      <option value="SECTION1">Section 1</option>
                      <option value="SECTION2">Section 2</option>
                      <option value="SECTION3">Section 3</option>
                      <option value="SECTION4">Section 4</option>
                      <option value="SECTION5">Section 5</option>
                    </select>
                    <small style={{ color: '#666', fontSize: '0.85rem', marginTop: '5px', display: 'block' }}>
                      Select which section these listening questions belong to
                    </small>
                  </div>

                  {/* Questions List for Listening Section */}
                  <div style={{ marginTop: "20px", padding: "15px", backgroundColor: "#f9fafb", borderRadius: "8px", border: "1px solid #e5e7eb" }}>
                    <h3 style={{ marginTop: 0, marginBottom: "15px", fontSize: "1.1rem" }}>Listening Questions ({sectionQuestions.length})</h3>
                    
                    {/* Add Question Form */}
                    <div className="form-group">
                      <label>Question Text (Optional)</label>
                      <textarea
                        name="questionText"
                        value={formData.questionText}
                        onChange={handleInputChange}
                        rows="2"
                        placeholder="Optional - Leave blank if questions share common audio"
                      />
                    </div>

                    <div className="form-group">
                      <label>Options *</label>
                      {formData.options.map((option, index) => (
                        <input
                          key={index}
                          type="text"
                          value={option}
                          onChange={(e) => handleOptionChange(index, e.target.value)}
                          placeholder={`Option ${index + 1}`}
                          style={{ marginBottom: "10px" }}
                        />
                      ))}
                    </div>

                    <div className="form-group">
                      <label>Correct Answer *</label>
                      <select
                        name="correctTempId"
                        value={formData.correctTempId}
                        onChange={handleInputChange}
                      >
                        <option value="">-- Select correct option --</option>
                        {formData.options.filter(opt => opt.trim() !== "").map((option, idx) => (
                          <option key={idx} value={`a${idx + 1}`}>
                            Option {idx + 1}: {option}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="form-group">
                      <label>Points *</label>
                      <input
                        type="number"
                        name="points"
                        value={formData.points}
                        onChange={handleInputChange}
                        min="1"
                      />
                    </div>

                    <button
                      type="button"
                      className="btn-primary"
                      onClick={handleAddQuestionToSection}
                      style={{ width: "100%", marginBottom: "15px" }}
                    >
                      + Add Question to Section
                    </button>

                    {/* Display Added Questions */}
                    {sectionQuestions.length > 0 && (
                      <div style={{ marginTop: "15px" }}>
                        {sectionQuestions.map((q, index) => (
                          <div key={index} style={{ padding: "10px", backgroundColor: "white", borderRadius: "6px", marginBottom: "10px", border: "1px solid #d1d5db" }}>
                            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "start" }}>
                              <div style={{ flex: 1 }}>
                                <strong>Q{index + 1}:</strong> {q.content || "(No text)"}
                                <div style={{ marginTop: "5px", fontSize: "0.9rem", color: "#0284c7" }}>
                                  ✓ Correct: {q.correctAnswer} | Points: {q.maxScore}
                                </div>
                              </div>
                              <button
                                type="button"
                                onClick={() => handleRemoveQuestionFromSection(index)}
                                style={{ marginLeft: "10px", padding: "5px 10px", backgroundColor: "#ef4444", color: "white", border: "none", borderRadius: "4px", cursor: "pointer" }}
                              >
                                Remove
                              </button>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </>
              )}

              {!isSectionMode && readingMode !== 'batchQuestions' && listeningMode !== 'batchQuestions' && (
                <div className="form-group">
                  <label>Question Type * (Current: {selectedSkillType})</label>
                  <select
                    name="questionType"
                    value={formData.questionType}
                    onChange={handleInputChange}
                    required
                    disabled={true}
                  >
                    <option value="READING">Reading</option>
                    <option value="LISTENING">Listening</option>
                    <option value="WRITING">Writing</option>
                    <option value="SPEAKING">Speaking</option>
                  </select>
                  <small style={{ color: '#666', fontSize: '0.85rem' }}>
                    Based on selected skill type above
                  </small>
                </div>
              )}

              {!isSectionMode && listeningMode === 'single' && formData.questionType === "LISTENING" && (
                <div className="form-group">
                  <label>Section (Optional - for grouping questions)</label>
                  <select
                    name="sectionName"
                    value={formData.sectionName}
                    onChange={handleInputChange}
                  >
                    <option value="">-- No Section --</option>
                    <option value="SECTION1">Section 1</option>
                    <option value="SECTION2">Section 2</option>
                    <option value="SECTION3">Section 3</option>
                    <option value="SECTION4">Section 4</option>
                    <option value="SECTION5">Section 5</option>
                  </select>
                  <small style={{ color: '#666', fontSize: '0.85rem', marginTop: '5px', display: 'block' }}>
                    Create questions first, then upload audio for the section using "Add Section" button
                  </small>
                </div>
              )}

              {!isSectionMode && readingMode === 'single' && formData.questionType === "READING" && (
                <div className="form-group">
                  <label>Section *</label>
                  <select
                    name="sectionName"
                    value={formData.sectionName}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="">-- Select Section --</option>
                    <option value="SECTION1">Section 1</option>
                    <option value="SECTION2">Section 2</option>
                    <option value="SECTION3">Section 3</option>
                    <option value="SECTION4">Section 4</option>
                    <option value="SECTION5">Section 5</option>
                  </select>
                  <small style={{ color: '#666', fontSize: '0.85rem', marginTop: '5px', display: 'block' }}>
                    Create questions first, then upload section image using "Upload Section Image" button
                  </small>
                </div>
              )}

              {!isSectionMode && readingMode === 'single' && listeningMode === 'single' && (formData.questionType === "READING" || formData.questionType === "LISTENING") && (
                <>
                  <div className="form-group">
                    <label>Question Text {formData.questionType === "READING" ? "(Optional)" : "*"}</label>
                    <textarea
                      name="questionText"
                      value={formData.questionText}
                      onChange={handleInputChange}
                      rows="3"
                      required={formData.questionType !== "READING"}
                      placeholder={formData.questionType === "READING" ? "Optional - Leave blank if questions share common passage text" : "Enter the question text here..."}
                    />
                  </div>

                  <div className="form-group">
                    <label>Options (leave empty if not multiple choice)</label>
                    {formData.options.map((option, index) => (
                      <input
                        key={index}
                        type="text"
                        value={option}
                        onChange={(e) => handleOptionChange(index, e.target.value)}
                        placeholder={`Option ${index + 1}`}
                        style={{ marginBottom: "10px" }}
                      />
                    ))}
                  </div>

                  {(formData.questionType === "READING" || formData.questionType === "LISTENING") && (
                    <div className="form-group">
                      <label>Correct Answer (Select from options) *</label>
                      <select
                        name="correctTempId"
                        value={formData.correctTempId}
                        onChange={handleInputChange}
                        required
                      >
                        <option value="">-- Select correct option --</option>
                        {formData.options.filter(opt => opt.trim() !== "").map((option, idx) => (
                          <option key={idx} value={`temp-${idx}`}>
                            Option {idx + 1}: {option}
                          </option>
                        ))}
                      </select>
                    </div>
                  )}

                  <div className="form-group">
                    <label>Points *</label>
                    <input
                      type="number"
                      name="points"
                      value={formData.points}
                      onChange={handleInputChange}
                      min="1"
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label>Explanation</label>
                    <textarea
                      name="explanation"
                      value={formData.explanation}
                      onChange={handleInputChange}
                      rows="3"
                    />
                  </div>
                </>
              )}

              {!isSectionMode && formData.questionType === "WRITING" && (
                <>
                  <div className="form-group">
                    <label>Task *</label>
                    <select
                      name="sectionName"
                      value={formData.sectionName}
                      onChange={handleInputChange}
                      required
                    >
                      <option value="">-- Select Task --</option>
                      <option value="task1">Task 1 (Report/Chart with Image)</option>
                      <option value="task2">Task 2 (Essay)</option>
                    </select>
                    <small style={{ color: '#666', fontSize: '0.85rem', marginTop: '5px', display: 'block' }}>
                      Task 1 requires an image for charts/graphs, Task 2 is essay writing
                    </small>
                  </div>

                  <div className="form-group">
                    <label>Question Text *</label>
                    <ReactQuill
                      theme="snow"
                      value={formData.questionText}
                      onChange={(value) => setFormData(prev => ({ ...prev, questionText: value }))}
                      placeholder="Enter the writing prompt or instructions..."
                      modules={{
                        toolbar: [
                          [{ 'header': [1, 2, 3, false] }],
                          ['bold', 'italic', 'underline', 'strike'],
                          [{ 'list': 'ordered'}, { 'list': 'bullet' }],
                          [{ 'indent': '-1'}, { 'indent': '+1' }],
                          ['link'],
                          ['clean']
                        ]
                      }}
                      style={{ backgroundColor: 'white', minHeight: '150px' }}
                    />
                  </div>

                  {formData.sectionName === 'task1' && (
                    <div className="form-group">
                      <label>Question Image {editingQuestion?.imageUrl ? "(Optional - Leave empty to keep current)" : "(Required for Task 1)"}</label>
                      <input
                        type="file"
                        accept="image/*"
                        onChange={handleImageChange}
                        required={formData.sectionName === 'task1' && !editingQuestion?.imageUrl}
                      />
                      {imageFile && (
                        <p style={{ marginTop: "10px", fontSize: "0.9rem", color: "#059669" }}>
                          ✓ New image selected: {imageFile.name}
                        </p>
                      )}
                      {editingQuestion?.imageUrl && !imageFile && (
                        <div style={{ marginTop: "10px" }}>
                          <p style={{ fontSize: "0.9rem", color: "#059669", marginBottom: "8px", fontWeight: "500" }}>
                            📸 Current Image:
                          </p>
                          <img 
                            src={editingQuestion.imageUrl} 
                            alt="Current question" 
                            style={{ maxWidth: "200px", maxHeight: "200px", objectFit: "contain", border: "1px solid #e5e7eb", borderRadius: "4px" }}
                          />
                          <p style={{ fontSize: "0.85rem", color: "#666", marginTop: "8px" }}>
                            Upload a new file to replace this image
                          </p>
                        </div>
                      )}
                    </div>
                  )}

                  <div className="form-group">
                    <label>Points *</label>
                    <input
                      type="number"
                      name="points"
                      value={formData.points}
                      onChange={handleInputChange}
                      min="1"
                      required
                    />
                  </div>
                </>
              )}

              {!isSectionMode && formData.questionType === "SPEAKING" && (
                <>
                  <div className="form-group">
                    <label>Part *</label>
                    <select
                      name="sectionName"
                      value={formData.sectionName}
                      onChange={handleInputChange}
                      required
                    >
                      <option value="">-- Select Part --</option>
                      <option value="part1">Part 1 (Interview)</option>
                      <option value="part2">Part 2 (Long Turn)</option>
                      <option value="part3">Part 3 (Discussion)</option>
                    </select>
                    <small style={{ color: '#666', fontSize: '0.85rem', marginTop: '5px', display: 'block' }}>
                      Part 1: Introduction and interview, Part 2: Individual long turn, Part 3: Two-way discussion
                    </small>
                  </div>

                  <div className="form-group">
                    <label>Question Text *</label>
                    <ReactQuill
                      theme="snow"
                      value={formData.questionText}
                      onChange={(value) => setFormData(prev => ({ ...prev, questionText: value }))}
                      placeholder="Enter the speaking question or topic..."
                      modules={{
                        toolbar: [
                          [{ 'header': [1, 2, 3, false] }],
                          ['bold', 'italic', 'underline', 'strike'],
                          [{ 'list': 'ordered'}, { 'list': 'bullet' }],
                          [{ 'indent': '-1'}, { 'indent': '+1' }],
                          ['link'],
                          ['clean']
                        ]
                      }}
                      style={{ backgroundColor: 'white', minHeight: '150px' }}
                    />
                  </div>

                  <div className="form-group">
                    <label>Points *</label>
                    <input
                      type="number"
                      name="points"
                      value={formData.points}
                      onChange={handleInputChange}
                      min="1"
                      required
                    />
                  </div>
                </>
              )}

              <div className="form-actions">
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={handleCloseModal}
                >
                  Cancel
                </button>
                <button 
                  type="submit" 
                  className="btn-primary"
                >
                  {editingQuestion ? "Update" : 
                   readingMode === 'createSection' ? "Upload Image" :
                   readingMode === 'batchQuestions' ? `Submit ${sectionQuestions.length} Reading Questions` :
                   listeningMode === 'batchQuestions' ? `Submit ${sectionQuestions.length} Listening Questions` :
                   isSectionMode ? "Upload Audio" : 
                   "Create"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
      </div>
    </div>
  );
};

export default QuestionsManagement;
