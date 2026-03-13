package com.dxh.Elearning.repo;

import com.dxh.Elearning.entity.ExamPart;
import com.dxh.Elearning.enums.SkillType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamPartRepository extends JpaRepository<ExamPart, Long> {

    List<ExamPart> findAllByExam_Id(Long examId);
    
    Page<ExamPart> findAllByExam_Id(Long examId, Pageable pageable);
    
    // Changed to List to handle potential duplicates
    List<ExamPart> findByExam_IdAndSkillType(Long examId, SkillType skillType);
}

