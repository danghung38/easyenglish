package com.dxh.Elearning.repo;

import com.dxh.Elearning.entity.UserExam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserExamRepository extends JpaRepository<UserExam, Long> {

    List<UserExam> findAllByUser_Id(Long user_id);
}
