package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.dto.request.ExamRequest;
import com.dxh.Elearning.dto.response.ExamResponse;
import com.dxh.Elearning.dto.response.PageResponse;
import com.dxh.Elearning.dto.response.UserResponse;
import com.dxh.Elearning.entity.Exam;
import com.dxh.Elearning.entity.User;
import com.dxh.Elearning.mapper.ExamMapper;
import com.dxh.Elearning.repo.ExamRepository;
import com.dxh.Elearning.service.AwsS3Service;
import com.dxh.Elearning.service.interfac.ExamService;
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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dxh.Elearning.utils.AppConstant.SORT_BY;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ExamServiceImpl implements ExamService {

    ExamRepository examRepository;
    ExamMapper examMapper;
    AwsS3Service awsS3Service;
    static Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "title");

    @Override
    public ExamResponse create(ExamRequest req, MultipartFile image) {
        Exam exam = examMapper.toExam(req);
        
        // Upload image to S3 if provided
        if (image != null && !image.isEmpty()) {
            String imageUrl = awsS3Service.saveImageToS3(image);
            exam.setImageUrl(imageUrl);
        }
        
        exam = examRepository.save(exam);
        return examMapper.toExamResponse(exam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamResponse update(Long examId, ExamRequest req, MultipartFile image) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + examId));
        
        // Update fields
        exam.setTitle(req.getTitle());
        exam.setDescription(req.getDescription());
        exam.setTotalDuration(req.getTotalDuration());
        
        // Upload new image to S3 if provided
        if (image != null && !image.isEmpty()) {
            String imageUrl = awsS3Service.saveImageToS3(image);
            exam.setImageUrl(imageUrl);
        }
        
        exam = examRepository.save(exam);
        return examMapper.toExamResponse(exam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long examId) {
        examRepository.deleteById(examId);
    }

    @Override
    public PageResponse<List<ExamResponse>> getAllExamsSortBy(int pageNo, int pageSize, String sortBy) {
        int page = pageNo>0?(pageNo-1):0;
        List<Sort.Order> sorts = new ArrayList<>();


        if (StringUtils.hasLength(sortBy)) {
            // name:asc|desc
            Pattern pattern = Pattern.compile(SORT_BY); // AppConstant.SORT_BY = "(\\w+?)(:)(.*)"
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                String field = matcher.group(1);
                String direction = matcher.group(3);
                if (!ALLOWED_SORT_FIELDS.contains(field)) {
                    throw new IllegalArgumentException("Invalid sort field: " + field);
                }
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

        Page<Exam> exams = examRepository.findAll(pageable);
        List<ExamResponse> examResponseList = exams.stream().map(examMapper::toExamResponse).toList();
        return PageResponse.<List<ExamResponse>>builder()
                .pageNo(page+1)
                .pageSize(pageSize)
                .totalPage(exams.getTotalPages())
                .items(examResponseList)
                .totalElements(exams.getTotalElements())
                .build();
    }
}
