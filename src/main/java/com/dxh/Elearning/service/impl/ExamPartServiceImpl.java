package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.dto.request.ExamPartRequest;
import com.dxh.Elearning.dto.request.ExamRequest;
import com.dxh.Elearning.dto.response.ExamPartResponse;
import com.dxh.Elearning.dto.response.ExamResponse;
import com.dxh.Elearning.dto.response.PageResponse;
import com.dxh.Elearning.entity.Exam;
import com.dxh.Elearning.entity.ExamPart;
import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import com.dxh.Elearning.mapper.ExamMapper;
import com.dxh.Elearning.mapper.ExamPartMapper;
import com.dxh.Elearning.repo.ExamPartRepository;
import com.dxh.Elearning.repo.ExamRepository;
import com.dxh.Elearning.service.interfac.ExamPartService;
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
public class ExamPartServiceImpl implements ExamPartService {

    ExamRepository examRepository;
    ExamPartRepository examPartRepository;
    ExamPartMapper examPartMapper;

    @Override
    public ExamPartResponse create(ExamPartRequest req) {

        Exam exam = examRepository.findById(req.getExamId())
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_EXISTED));

        ExamPart examPart = ExamPart.builder()
                .exam(exam)
                .skillType(req.getSkillType())
                .duration(req.getDuration())
                .build();

        examPartRepository.save(examPart);
        return examPartMapper.toExamPartResponse(examPart);
    }

    @Override
    public List<ExamPartResponse> findAllByExamId(Long examId) {
        List<ExamPart> list = examPartRepository.findAllByExam_Id(examId);
        return list.stream().map(examPartMapper::toExamPartResponse).toList();
    }

    @Override
    public PageResponse<List<ExamPartResponse>> getAllExamPartsByExamId(Long examId, Integer pageNo, Integer pageSize, String sortBy) {
        int page = pageNo > 0 ? (pageNo - 1) : 0;
        List<Sort.Order> sorts = new ArrayList<>();
        
        Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "skillType", "duration");

        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile(SORT_BY);
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
        Page<ExamPart> examParts = examPartRepository.findAllByExam_Id(examId, pageable);
        
        List<ExamPartResponse> examPartResponseList = examParts.stream()
                .map(examPartMapper::toExamPartResponse)
                .toList();
        
        return PageResponse.<List<ExamPartResponse>>builder()
                .pageNo(page + 1)
                .pageSize(pageSize)
                .totalPage(examParts.getTotalPages())
                .items(examPartResponseList)
                .totalElements(examParts.getTotalElements())
                .build();
    }

    @Override
    public ExamPartResponse update(Long examPartId, ExamPartRequest req) {
        ExamPart examPart = examPartRepository.findById(examPartId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_PART_NOT_FOUND));

        // Update fields
        if (req.getSkillType() != null) {
            examPart.setSkillType(req.getSkillType());
        }
        if (req.getDuration() != null) {
            examPart.setDuration(req.getDuration());
        }
        if (req.getExamId() != null) {
            Exam exam = examRepository.findById(req.getExamId())
                    .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_EXISTED));
            examPart.setExam(exam);
        }

        examPartRepository.save(examPart);
        return examPartMapper.toExamPartResponse(examPart);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long examPartId) {
        examPartRepository.deleteById(examPartId);
    }


}
