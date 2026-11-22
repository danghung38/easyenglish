package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.dto.request.*;
import com.dxh.Elearning.dto.response.ExamResponse;
import com.dxh.Elearning.dto.response.PageResponse;
import com.dxh.Elearning.dto.response.QuestionResponse;
import com.dxh.Elearning.entity.*;
import com.dxh.Elearning.enums.SkillType;
import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import com.dxh.Elearning.mapper.OptionMapper;
import com.dxh.Elearning.mapper.QuestionMapper;
import com.dxh.Elearning.repo.ExamPartRepository;
import com.dxh.Elearning.repo.OptionRepository;
import com.dxh.Elearning.repo.QuestionRepository;

import com.dxh.Elearning.service.AwsS3Service;
import com.dxh.Elearning.service.interfac.QuestionService;

import java.util.Map;
import java.util.HashMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dxh.Elearning.utils.AppConstant.SORT_BY;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QuestionServiceImpl implements QuestionService {

    OptionRepository optionRepository;
    QuestionRepository questionRepository;
    ExamPartRepository examPartRepository;
    QuestionMapper questionMapper;
    AwsS3Service awsS3Service;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionResponse createQuestionReading(QuestionRequest req) {
        // 1️ Lấy ExamPart
        ExamPart examPart = examPartRepository.findById(req.getExamPartId()).orElseThrow(() -> new AppException(ErrorCode.EXAM_PART_NOT_EXISTED));

        // 2️ Tạo Question
        Question question = Question.builder().examPart(examPart).skillType(req.getSkillType()).content(req.getContent()).audioUrl(req.getAudioUrl()).section(req.getSection()).explain(req.getExplain()).maxScore(req.getMaxScore()).isSection(false).build();

        question = questionRepository.save(question);

        // 3️ Xử lý đáp án - READING/LISTENING có options (trắc nghiệm)
        // Skip if options is null (e.g., creating section container)
        if ((req.getSkillType() == SkillType.READING || req.getSkillType() == SkillType.LISTENING) 
            && req.getOptions() != null && !req.getOptions().isEmpty()) {
            // Tạo Options cho câu trắc nghiệm
            Option correctOption = null;
            List<Option> optionList = new ArrayList<>();
            for (OptionRequest o : req.getOptions()) {
                Option option = Option.builder().content(o.getContent()).question(question).build();
                option = optionRepository.save(option);
                optionList.add(option);

                if (o.getTempId().equals(req.getCorrectTempId())) {
                    correctOption = option;
                }
            }
            question.setOptions(optionList);
            question.setCorrectOption(correctOption);
        }
        questionRepository.save(question);
        return questionMapper.toQuestionResponse(question);

    }

    // Version with audio file (for legacy support or special cases)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionResponse createQuestionListening(QuestionRequest req, MultipartFile audioFile) {
        // 1️ Lấy ExamPart
        ExamPart examPart = examPartRepository.findById(req.getExamPartId())
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_PART_NOT_EXISTED));

        // 2️ Upload audio lên S3
        String audioUrl = awsS3Service.saveAudioToS3(audioFile);

        // 2️⃣ Tạo Question
        Question question = Question.builder()
                .examPart(examPart)
                .skillType(req.getSkillType())
                .content(req.getContent())
                .audioUrl(audioUrl)
                .section(req.getSection())                .explain(req.getExplain())                .maxScore(req.getMaxScore())
                .isSection(false)
                .build();

        question = questionRepository.save(question);

        // 4️ Xử lý đáp án - LISTENING luôn có options (trắc nghiệm)
        if (req.getSkillType() == SkillType.LISTENING) {
            // Tạo Options cho câu trắc nghiệm
            Option correctOption = null;
            List<Option> optionList = new ArrayList<>();
            for (OptionRequest o : req.getOptions()) {
                Option option = Option.builder()
                        .content(o.getContent())
                        .question(question)
                        .build();
                option = optionRepository.save(option);
                optionList.add(option);

                if (o.getTempId().equals(req.getCorrectTempId())) {
                    correctOption = option;
                }
            }
            question.setOptions(optionList);
            question.setCorrectOption(correctOption);
        }
        questionRepository.save(question);

        return questionMapper.toQuestionResponse(question);
    }

    // New version without audio file (recommended for individual questions)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionResponse createQuestionListening(QuestionRequest req) {
        // 1️ Lấy ExamPart
        ExamPart examPart = examPartRepository.findById(req.getExamPartId())
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_PART_NOT_EXISTED));

        // 2️⃣ Tạo Question (không cần upload audio)
        Question question = Question.builder()
                .examPart(examPart)
                .skillType(req.getSkillType())
                .content(req.getContent())
                .audioUrl(req.getAudioUrl()) // Sử dụng audioUrl từ request nếu có
                .section(req.getSection())                .explain(req.getExplain())                .maxScore(req.getMaxScore())
                .isSection(false)
                .build();

        question = questionRepository.save(question);

        // 3️ Xử lý đáp án - LISTENING luôn có options (trắc nghiệm)
        if (req.getSkillType() == SkillType.LISTENING) {
            // Tạo Options cho câu trắc nghiệm
            Option correctOption = null;
            List<Option> optionList = new ArrayList<>();
            for (OptionRequest o : req.getOptions()) {
                Option option = Option.builder()
                        .content(o.getContent())
                        .question(question)
                        .build();
                option = optionRepository.save(option);
                optionList.add(option);

                if (o.getTempId().equals(req.getCorrectTempId())) {
                    correctOption = option;
                }
            }
            question.setOptions(optionList);
            question.setCorrectOption(correctOption);
        }
        questionRepository.save(question);

        return questionMapper.toQuestionResponse(question);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionResponse createQuestionWriting(QuestionRequest req, MultipartFile imageFile) {
        ExamPart examPart = examPartRepository.findById(req.getExamPartId())
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_PART_NOT_EXISTED));

        // Upload image to S3 if provided (for Writing Task 1)
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = awsS3Service.saveImageToS3(imageFile);
            log.info("Uploaded Writing Task 1 image to S3: {}", imageUrl);
        }

        Question question = Question.builder()
                .examPart(examPart)
                .skillType(req.getSkillType())
                .content(req.getContent())
                .section(req.getSection()) // task1 or task2
                .imageUrl(imageUrl)
                .explain(req.getExplain())
                .maxScore(req.getMaxScore())
                .isSection(false)
                .build();

        question = questionRepository.save(question);
        return questionMapper.toQuestionResponse(question);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionResponse createQuestionSpeaking(QuestionRequest req) {
        ExamPart examPart = examPartRepository.findById(req.getExamPartId())
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_PART_NOT_EXISTED));

        Question question = Question.builder()
                .examPart(examPart)
                .skillType(req.getSkillType())
                .content(req.getContent())
                .section(req.getSection())
                .explain(req.getExplain())
                .maxScore(req.getMaxScore())
                .isSection(false)
                .build();

        question = questionRepository.save(question);
        return questionMapper.toQuestionResponse(question);
    }

    @Override
    public PageResponse<List<QuestionResponse>> getQuestionsByExamPart(Long examPartId, int pageNo, int pageSize, String sortBy) {
        int page = pageNo>0?(pageNo-1):0;
        List<Sort.Order> sorts = new ArrayList<>();


        if (StringUtils.hasLength(sortBy)) {
            // name:asc|desc
            Pattern pattern = Pattern.compile(SORT_BY); // AppConstant.SORT_BY = "(\\w+?)(:)(.*)"
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                String field = matcher.group(1);
                String direction = matcher.group(3);
                if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
                    throw new IllegalArgumentException("Sort direction must be 'asc' or 'desc'");
                }
                if (direction.equalsIgnoreCase("asc")) {
                    sorts.add(new Sort.Order(Sort.Direction.ASC, field));
                } else {
                    sorts.add(new Sort.Order(Sort.Direction.DESC, field));
                }
            }
        }

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sorts));

        Page<Question> questions = questionRepository.findByExamPartId(examPartId,pageable);
        List<QuestionResponse> list = questions.stream().map(questionMapper::toQuestionResponse).toList();
        return PageResponse.<List<QuestionResponse>>builder()
                .pageNo(page+1)
                .pageSize(pageSize)
                .totalPage(questions.getTotalPages())
                .items(list)
                .totalElements(questions.getTotalElements())
                .build();
    }

    @Override
    public PageResponse<List<QuestionResponse>> getQuestionsByExamIdAndSkillType(Long examId, SkillType skillType, int pageNo, int pageSize, String sortBy) {
        int page = pageNo>0?(pageNo-1):0;
        List<Sort.Order> sorts = new ArrayList<>();

        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile(SORT_BY);
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                String field = matcher.group(1);
                String direction = matcher.group(3);
                if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
                    throw new IllegalArgumentException("Sort direction must be 'asc' or 'desc'");
                }
                if (direction.equalsIgnoreCase("asc")) {
                    sorts.add(new Sort.Order(Sort.Direction.ASC, field));
                } else {
                    sorts.add(new Sort.Order(Sort.Direction.DESC, field));
                }
            }
        }

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sorts));

        // Find exam part with the specified skillType for this exam
        List<ExamPart> examParts = examPartRepository.findByExam_IdAndSkillType(examId, skillType);
        
        if (examParts.isEmpty()) {
            throw new AppException(ErrorCode.EXAM_PART_NOT_FOUND);
        }
        
        // Use the first exam part if multiple exist (should not happen in normal case)
        ExamPart examPart = examParts.get(0);
        
        if (examParts.size() > 1) {
            log.warn("Found {} exam parts for examId={} and skillType={}. Using first one (id={})", 
                    examParts.size(), examId, skillType, examPart.getId());
        }

        // Get questions by the found exam part AND filter by skillType to ensure consistency
        Page<Question> questions = questionRepository.findByExamPartIdAndSkillType(examPart.getId(), skillType, pageable);
        List<QuestionResponse> list = questions.stream().map(questionMapper::toQuestionResponse).toList();
        
        return PageResponse.<List<QuestionResponse>>builder()
                .pageNo(page+1)
                .pageSize(pageSize)
                .totalPage(questions.getTotalPages())
                .items(list)
                .totalElements(questions.getTotalElements())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionResponse updateQuestionReading(Long questionId, UpdateQuestionReadingRequest req) {
        // 1️⃣ Lấy Question
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        // 2️⃣ Update nội dung
        question.setContent(req.getContent());
        question.setAudioUrl(req.getAudioUrl());        question.setExplain(req.getExplain());        question.setMaxScore(req.getMaxScore());

        // 3️⃣ Xử lý Option - UPDATE existing options instead of deleting and creating new ones
        List<Option> existingOptions = question.getOptions();
        Option correctOption = null;
        
        // Update existing options or create new ones if needed
        for (int i = 0; i < req.getOptions().size(); i++) {
            OptionRequest optionReq = req.getOptions().get(i);
            Option option;
            
            if (i < existingOptions.size()) {
                // Update existing option (keep same ID)
                option = existingOptions.get(i);
                option.setContent(optionReq.getContent());
            } else {
                // Create new option only if there are more options than before
                option = Option.builder()
                        .content(optionReq.getContent())
                        .question(question)
                        .build();
                existingOptions.add(option);
            }
            
            option = optionRepository.save(option);
            
            if (optionReq.getTempId().equals(req.getCorrectTempId())) {
                correctOption = option;
            }
        }
        
        // Remove extra options if new list is shorter than existing
        if (req.getOptions().size() < existingOptions.size()) {
            List<Option> optionsToRemove = new ArrayList<>(
                existingOptions.subList(req.getOptions().size(), existingOptions.size())
            );
            existingOptions.removeAll(optionsToRemove);
            optionRepository.deleteAll(optionsToRemove);
        }

        // 4️⃣ Gán đáp án đúng
        question.setCorrectOption(correctOption);
        question = questionRepository.save(question);

        // 5️⃣ Map sang Response
        return questionMapper.toQuestionResponse(question);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionResponse updateQuestionListening(Long questionId, UpdateQuestionListeningRequest req, MultipartFile audioFile) {
        // 1️⃣ Lấy question
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        // 2️⃣ Update nội dung
        question.setContent(req.getContent());        question.setExplain(req.getExplain());        question.setMaxScore(req.getMaxScore());

        // 3️⃣ Upload audio mới nếu có
        if (audioFile != null && !audioFile.isEmpty()) {
            String audioUrl = awsS3Service.saveAudioToS3(audioFile);
            question.setAudioUrl(audioUrl);
        }

        // 4️⃣ Xử lý option - UPDATE existing options instead of deleting and creating new ones
        List<Option> existingOptions = question.getOptions();
        Option correctOption = null;
        
        // Update existing options or create new ones if needed
        for (int i = 0; i < req.getOptions().size(); i++) {
            OptionRequest optionReq = req.getOptions().get(i);
            Option option;
            
            if (i < existingOptions.size()) {
                // Update existing option (keep same ID)
                option = existingOptions.get(i);
                option.setContent(optionReq.getContent());
            } else {
                // Create new option only if there are more options than before
                option = Option.builder()
                        .content(optionReq.getContent())
                        .question(question)
                        .build();
                existingOptions.add(option);
            }
            
            option = optionRepository.save(option);
            
            if (optionReq.getTempId().equals(req.getCorrectTempId())) {
                correctOption = option;
            }
        }
        
        // Remove extra options if new list is shorter than existing
        if (req.getOptions().size() < existingOptions.size()) {
            List<Option> optionsToRemove = new ArrayList<>(
                existingOptions.subList(req.getOptions().size(), existingOptions.size())
            );
            existingOptions.removeAll(optionsToRemove);
            optionRepository.deleteAll(optionsToRemove);
        }

        // 5️⃣ Gán đáp án đúng
        question.setCorrectOption(correctOption);
        question = questionRepository.save(question);

        return questionMapper.toQuestionResponse(question);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionResponse updateQuestionSpeaking(Long questionId, UpdateQuestionSpeakingRequest req) {
        // 1️⃣ Lấy question
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        // 2️⃣ Update nội dung
        question.setContent(req.getContent());        question.setExplain(req.getExplain());        question.setMaxScore(req.getMaxScore());
        
        // Update section if provided (part1, part2, part3)
        if (req.getSection() != null && !req.getSection().trim().isEmpty()) {
            question.setSection(req.getSection());
        }

        // 3️⃣ Lưu lại DB
        question = questionRepository.save(question);

        // 4️⃣ Map sang response
        return questionMapper.toQuestionResponse(question);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionResponse updateQuestionWriting(Long questionId, UpdateQuestionWritingRequest req, MultipartFile imageFile) {
        // 1️⃣ Lấy question
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        // 2️⃣ Update nội dung và điểm tối đa
        question.setContent(req.getContent());        question.setExplain(req.getExplain());        question.setMaxScore(req.getMaxScore());
        
        // Update section if provided (task1, task2)
        if (req.getSection() != null && !req.getSection().trim().isEmpty()) {
            question.setSection(req.getSection());
        }
        
        // Update image if provided (for Task 1)
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = awsS3Service.saveImageToS3(imageFile);
            question.setImageUrl(imageUrl);
            log.info("Updated Writing Task 1 image: {}", imageUrl);
        } else if (req.getImageUrl() != null) {
            // Allow setting imageUrl from request (e.g., removing image by setting to null)
            question.setImageUrl(req.getImageUrl());
        }

        // 3️⃣ Lưu lại DB
        question = questionRepository.save(question);

        // 4️⃣ Map sang response
        return questionMapper.toQuestionResponse(question);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<QuestionResponse> createListeningSection(SectionRequest req, MultipartFile audioFile) {
        // Validation
        if (req.getQuestionIds() == null || req.getQuestionIds().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        // 1️⃣ Lấy ExamPart
        ExamPart examPart = examPartRepository.findById(req.getExamPartId())
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_PART_NOT_EXISTED));

        // 2️⃣ Upload audio lên S3
        String audioUrl = awsS3Service.saveAudioToS3(audioFile);
        
        List<QuestionResponse> allResponses = new ArrayList<>();
        String sectionDisplayName = req.getSectionDisplayName(); // e.g., "Section 1"

        // 3️⃣ Tạo Section Instructions (optional - chỉ khi có sectionContent)
        if (req.getSectionContent() != null && !req.getSectionContent().trim().isEmpty()) {
            Question sectionQuestion = Question.builder()
                    .examPart(examPart)
                    .skillType(req.getSkillType())
                    .isSection(true)
                    .content(req.getSectionContent())
                    .audioUrl(audioUrl)
                    .section(sectionDisplayName)
                    .transcript(req.getTranscript())
                    .maxScore(0.0)
                    .build();
            
            sectionQuestion = questionRepository.save(sectionQuestion);
            allResponses.add(questionMapper.toQuestionResponse(sectionQuestion));
        }
        
        // 4️⃣ Lấy và cập nhật các questions đã tạo sẵn
        List<Question> existingQuestions = questionRepository.findAllById(req.getQuestionIds());
        
        // Kiểm tra xem tất cả question IDs có tồn tại không
        if (existingQuestions.size() != req.getQuestionIds().size()) {
            throw new AppException(ErrorCode.QUESTION_NOT_FOUND);
        }
        
        Double totalScore = 0.0;
        for (Question question : existingQuestions) {
            // Cập nhật section cho question
            question.setSection(sectionDisplayName);
            
            // Kiểm tra xem question có thuộc đúng examPart không
            if (!question.getExamPart().getId().equals(req.getExamPartId())) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            
            // Kiểm tra xem question có đúng skillType không  
            if (!question.getSkillType().equals(req.getSkillType())) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            
            totalScore += question.getMaxScore();
            question = questionRepository.save(question);
            allResponses.add(questionMapper.toQuestionResponse(question));
        }
        
        log.info("Created section '{}' ({}) with {} existing questions, total score: {}", 
                req.getSectionName(), sectionDisplayName, req.getQuestionIds().size(), totalScore);
        
        return allResponses;
    }

    @Override
    public List<QuestionResponse> getQuestionsBySection(Long examPartId, String sectionName) {
        // Lấy tất cả questions trong section (bao gồm section instruction + individual questions)
        List<Question> questions = questionRepository.findByExamPartIdAndSection(examPartId, sectionName);
        
        List<QuestionResponse> responses = new ArrayList<>();
        for (Question question : questions) {
            responses.add(questionMapper.toQuestionResponse(question));
        }
        
        return responses;
    }

    @Override
    public Object getAllListeningSections(Long examPartId) {
        // Lấy tất cả questions cho examPart
        List<Question> allQuestions = questionRepository.findByExamPartId(examPartId);
        
        // Tạo structure cho 4 sections
        List<Object> sections = new ArrayList<>();
        String[] sectionNames = {"Section 1", "Section 2", "Section 3", "Section 4"};
        
        for (String sectionName : sectionNames) {
            List<Question> sectionQuestions = allQuestions.stream()
                .filter(q -> sectionName.equals(q.getSection()))
                .toList();
            
            // Tạo section object
            Map<String, Object> section = new HashMap<>();
            section.put("sectionName", sectionName);
            section.put("questionCount", sectionQuestions.size());
            
            // Lấy audio URL từ question đầu tiên
            String audioUrl = sectionQuestions.stream()
                .map(Question::getAudioUrl)
                .filter(url -> url != null && !url.isEmpty())
                .findFirst()
                .orElse(null);
            section.put("audioUrl", audioUrl);
            
            // Lấy instructions từ section question (isSection = true)
            String instructions = sectionQuestions.stream()
                .filter(q -> Boolean.TRUE.equals(q.getIsSection()))
                .map(Question::getContent)
                .findFirst()
                .orElse(null);
            section.put("instructions", instructions);
            
            // Map questions to responses (exclude section instruction)
            List<QuestionResponse> questionResponses = sectionQuestions.stream()
                .filter(q -> !Boolean.TRUE.equals(q.getIsSection()))
                .map(questionMapper::toQuestionResponse)
                .toList();
            section.put("questions", questionResponses);
            
            sections.add(section);
        }
        
        log.info("Retrieved {} sections for examPart {}", sections.size(), examPartId);
        return sections;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<QuestionResponse> addQuestionsToSection(AddQuestionsToSectionRequest req, MultipartFile audioFile) {
        // Validation
        if (req.getQuestions() == null || req.getQuestions().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        // 1️⃣ Lấy ExamPart
        ExamPart examPart = examPartRepository.findById(req.getExamPartId())
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_PART_NOT_EXISTED));

        // 2️⃣ Upload audio lên S3
        String audioUrl = awsS3Service.saveAudioToS3(audioFile);
        
        List<QuestionResponse> allResponses = new ArrayList<>();
        String sectionDisplayName = req.getSectionName().getDisplayName(); // e.g., "Section 1"

        // 3️⃣ Tạo Section Instructions (optional - chỉ khi có sectionContent)
        if (req.getSectionContent() != null && !req.getSectionContent().trim().isEmpty()) {
            Question sectionQuestion = Question.builder()
                    .examPart(examPart)
                    .skillType(SkillType.LISTENING)
                    .isSection(true)
                    .content(req.getSectionContent())
                    .audioUrl(audioUrl)
                    .section(sectionDisplayName)                    .transcript(req.getTranscript())                    .maxScore(0.0)
                    .build();
            
            sectionQuestion = questionRepository.save(sectionQuestion);
            allResponses.add(questionMapper.toQuestionResponse(sectionQuestion));
        }
        
        // 4️⃣ Tạo từng question trong section
        Double totalScore = 0.0;
        for (AddQuestionsToSectionRequest.QuestionInSectionRequest questionReq : req.getQuestions()) {
            // Tạo Question entity
            Question question = Question.builder()
                    .examPart(examPart)
                    .skillType(SkillType.LISTENING)
                    .isSection(false)
                    .content(questionReq.getContent())
                    .audioUrl(audioUrl) // Tất cả questions dùng chung 1 audio
                    .section(sectionDisplayName)
                    .maxScore(questionReq.getMaxScore() != null ? questionReq.getMaxScore() : 1.0)
                    .build();
            
            question = questionRepository.save(question);
            
            // Xử lý đáp án - LISTENING luôn có options (trắc nghiệm)
            Option correctOption = null;
            List<Option> optionList = new ArrayList<>();
            
            for (int i = 0; i < questionReq.getOptions().size(); i++) {
                String optionContent = questionReq.getOptions().get(i);
                Option option = Option.builder()
                        .content(optionContent)
                        .question(question)
                        .build();
                option = optionRepository.save(option);
                optionList.add(option);
                
                // Kiểm tra đáp án đúng bằng cách so sánh content
                if (optionContent.equalsIgnoreCase(questionReq.getCorrectAnswer())) {
                    correctOption = option;
                }
            }
            
            question.setOptions(optionList);
            question.setCorrectOption(correctOption);
            
            question = questionRepository.save(question);
            totalScore += question.getMaxScore();
            allResponses.add(questionMapper.toQuestionResponse(question));
        }
        
        log.info("Added {} questions to section '{}' ({}), total score: {}, audio: {}", 
                req.getQuestions().size(), req.getSectionName(), sectionDisplayName, totalScore, audioUrl);
        
        return allResponses;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<QuestionResponse> createReadingPassage(CreateReadingPassageRequest req, MultipartFile imageFile) {
        // 1️⃣ Validate
        if (req.getQuestions() == null || req.getQuestions().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        // 2️⃣ Lấy ExamPart
        ExamPart examPart = examPartRepository.findById(req.getExamPartId())
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_PART_NOT_EXISTED));

        // 3️⃣ Upload image lên S3
        String imageUrl = awsS3Service.saveImageToS3(imageFile);
        
        List<QuestionResponse> responses = new ArrayList<>();
        
        // 4️⃣ Tạo từng question trong passage
        for (ReadingQuestionRequest questionReq : req.getQuestions()) {
            // Tạo Question entity
            Question question = Question.builder()
                    .examPart(examPart)
                    .skillType(SkillType.READING)
                    .isSection(false)
                    .content(questionReq.getContent())
                    .imageUrl(imageUrl) // Tất cả questions dùng chung 1 ảnh
                    .section(req.getSection()) // Tên section - dùng cho backend note
                    .maxScore(questionReq.getMaxScore())
                    .build();
            
            question = questionRepository.save(question);
            
            // Tạo Options (A, B, C, D)
            Option correctOption = null;
            List<Option> optionList = new ArrayList<>();
            
            for (OptionRequest optionReq : questionReq.getOptions()) {
                Option option = Option.builder()
                        .content(optionReq.getContent())
                        .question(question)
                        .build();
                option = optionRepository.save(option);
                optionList.add(option);
                
                if (optionReq.getTempId().equals(questionReq.getCorrectTempId())) {
                    correctOption = option;
                }
            }
            
            question.setOptions(optionList);
            question.setCorrectOption(correctOption);
            question = questionRepository.save(question);
            
            responses.add(questionMapper.toQuestionResponse(question));
        }
        
        log.info("Created reading passage '{}' with {} questions and image URL: {}", 
                req.getSection(), req.getQuestions().size(), imageUrl);
        
        return responses;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<QuestionResponse> uploadAudioForSection(Long examPartId, String sectionName, MultipartFile audioFile, String transcript) {
        // 1️⃣ Validate
        if (audioFile == null || audioFile.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        // 2️⃣ Kiểm tra examPart tồn tại
        ExamPart examPart = examPartRepository.findById(examPartId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_PART_NOT_EXISTED));
        
        // 3️⃣ Upload audio lên S3
        String audioUrl = awsS3Service.saveAudioToS3(audioFile);
        
        // 4️⃣ Tìm tất cả questions có section này
        List<Question> questions = questionRepository.findByExamPartAndSection(examPart, sectionName);
        
        if (questions.isEmpty()) {
            throw new AppException(ErrorCode.QUESTION_NOT_FOUND);
        }
        
        // 5️⃣ Update audioUrl và transcript cho tất cả questions trong section
        List<QuestionResponse> responses = new ArrayList<>();
        for (Question question : questions) {
            question.setAudioUrl(audioUrl);
            // Set transcript cho tất cả questions trong section (để dễ truy xuất)
            if (transcript != null && !transcript.trim().isEmpty()) {
                question.setTranscript(transcript);
            }
            question = questionRepository.save(question);
            responses.add(questionMapper.toQuestionResponse(question));
        }
        
        log.info("Uploaded audio for section {} - Updated {} questions with audio URL: {} and transcript", 
                sectionName, questions.size(), audioUrl);
        
        return responses;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<QuestionResponse> uploadImageForSection(Long examPartId, String sectionName, MultipartFile imageFile) {
        // 1️⃣ Validate
        if (imageFile == null || imageFile.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        // 2️⃣ Kiểm tra examPart tồn tại
        ExamPart examPart = examPartRepository.findById(examPartId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_PART_NOT_EXISTED));
        
        // 3️⃣ Upload image lên S3
        String imageUrl = awsS3Service.saveImageToS3(imageFile);
        
        // 4️⃣ Tìm tất cả questions có section này
        List<Question> questions = questionRepository.findByExamPartAndSection(examPart, sectionName);
        
        if (questions.isEmpty()) {
            throw new AppException(ErrorCode.QUESTION_NOT_FOUND);
        }
        
        // 5️⃣ Update imageUrl cho tất cả questions trong section
        List<QuestionResponse> responses = new ArrayList<>();
        for (Question question : questions) {
            question.setImageUrl(imageUrl);
            question = questionRepository.save(question);
            responses.add(questionMapper.toQuestionResponse(question));
        }
        
        log.info("Uploaded image for section {} - Updated {} questions with image URL: {}", 
                sectionName, questions.size(), imageUrl);
        
        return responses;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
        questionRepository.delete(question);
        log.info("Deleted question with ID: {}", questionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<QuestionResponse> createReadingQuestionsBatch(Long examPartId, String sectionName, List<ReadingQuestionRequest> questions) {
        // 1️⃣ Validate
        if (questions == null || questions.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        // 2️⃣ Lấy ExamPart
        ExamPart examPart = examPartRepository.findById(examPartId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_PART_NOT_EXISTED));
        
        List<QuestionResponse> responses = new ArrayList<>();
        
        // 3️⃣ Tạo từng question trong section
        for (ReadingQuestionRequest questionReq : questions) {
            // Tạo Question entity
            Question question = Question.builder()
                    .examPart(examPart)
                    .skillType(SkillType.READING)
                    .isSection(false)
                    .content(questionReq.getContent())
                    .section(sectionName) // Tên section đã tạo trước
                    .maxScore(questionReq.getMaxScore())
                    .build();
            
            question = questionRepository.save(question);
            
            // Tạo Options (A, B, C, D)
            Option correctOption = null;
            List<Option> optionList = new ArrayList<>();
            
            for (OptionRequest optionReq : questionReq.getOptions()) {
                Option option = Option.builder()
                        .content(optionReq.getContent())
                        .question(question)
                        .build();
                option = optionRepository.save(option);
                optionList.add(option);
                
                if (optionReq.getTempId().equals(questionReq.getCorrectTempId())) {
                    correctOption = option;
                }
            }
            
            question.setOptions(optionList);
            question.setCorrectOption(correctOption);
            question = questionRepository.save(question);
            
            responses.add(questionMapper.toQuestionResponse(question));
        }
        
        log.info("Created {} reading questions for section '{}'", questions.size(), sectionName);
        
        return responses;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<QuestionResponse> createListeningQuestionsBatch(Long examPartId, String sectionName, List<ReadingQuestionRequest> questions) {
        // 1️⃣ Validate
        if (questions == null || questions.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        // 2️⃣ Lấy ExamPart
        ExamPart examPart = examPartRepository.findById(examPartId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_PART_NOT_EXISTED));
        
        List<QuestionResponse> responses = new ArrayList<>();
        
        // 3️⃣ Tạo từng question trong section
        for (ReadingQuestionRequest questionReq : questions) {
            // Tạo Question entity
            Question question = Question.builder()
                    .examPart(examPart)
                    .skillType(SkillType.LISTENING)
                    .isSection(false)
                    .content(questionReq.getContent())
                    .section(sectionName) // Tên section (SECTION1-5)
                    .maxScore(questionReq.getMaxScore())
                    .build();
            
            question = questionRepository.save(question);
            
            // Tạo Options (A, B, C, D)
            Option correctOption = null;
            List<Option> optionList = new ArrayList<>();
            
            for (OptionRequest optionReq : questionReq.getOptions()) {
                Option option = Option.builder()
                        .content(optionReq.getContent())
                        .question(question)
                        .build();
                option = optionRepository.save(option);
                optionList.add(option);
                
                if (optionReq.getTempId().equals(questionReq.getCorrectTempId())) {
                    correctOption = option;
                }
            }
            
            question.setOptions(optionList);
            question.setCorrectOption(correctOption);
            question = questionRepository.save(question);
            
            responses.add(questionMapper.toQuestionResponse(question));
        }
        
        log.info("Created {} listening questions for section '{}'", questions.size(), sectionName);
        
        return responses;
    }

}
