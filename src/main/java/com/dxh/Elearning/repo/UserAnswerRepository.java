package com.dxh.Elearning.repo;

import com.dxh.Elearning.entity.UserAnswer;
import com.dxh.Elearning.entity.UserExam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    @Query("SELECT COALESCE(SUM(a.score), 0) FROM UserAnswer a WHERE a.userExamPart.id = :partId")
    double sumScoreByUserExamPartId(@Param("partId") Long partId);

    //fix: Thêm method để xóa answers cũ
    @Modifying
    @Query("DELETE FROM UserAnswer ua WHERE ua.userExamPart.id = :userExamPartId")
    void deleteByUserExamPartId(@Param("userExamPartId") Long userExamPartId);

    List<UserAnswer> findAllByUserExamPart_Id(Long userExamPartId);
}
