package com.dxh.Elearning.repo;


import com.dxh.Elearning.entity.Question;
import com.dxh.Elearning.enums.SkillType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    Page<Question> findByExamPartId(Long examPartId, Pageable pageable);

    List<Question> findBySkillType(SkillType skillType);
    
    // Get all questions for examPart
    List<Question> findByExamPartId(Long examPartId);
    
    // Filter by examPartId and skillType
    Page<Question> findByExamPartIdAndSkillType(Long examPartId, SkillType skillType, Pageable pageable);
    
    // Methods for section-based queries - simplified
    List<Question> findByExamPartIdAndSection(Long examPartId, String section);
    List<Question> findByExamPartIdAndSectionOrderByIdAsc(Long examPartId, String section);
    List<Question> findByExamPartAndSection(com.dxh.Elearning.entity.ExamPart examPart, String section);
}
