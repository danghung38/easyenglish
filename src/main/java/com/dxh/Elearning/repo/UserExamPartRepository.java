package com.dxh.Elearning.repo;

import com.dxh.Elearning.entity.UserExamPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserExamPartRepository extends JpaRepository<UserExamPart, Long> {
    @Query("SELECT COALESCE(SUM(p.score), 0) FROM UserExamPart p WHERE p.userExam.id = :examId")
    double sumScoreByUserExamId(@Param("examId") Long examId);

    @Query("SELECT COALESCE(AVG(p.score), 0) FROM UserExamPart p WHERE p.userExam.id = :examId AND p.submitted = true")
    double calculateAverageScoreByUserExamId(@Param("examId") Long examId);

    List<UserExamPart> findAllByUserExam_Id(Long userExamId);
    
    List<UserExamPart> findByUserExamId(Long userExamId);
}
