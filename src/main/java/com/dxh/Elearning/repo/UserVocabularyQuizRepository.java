package com.dxh.Elearning.repo;

import com.dxh.Elearning.entity.UserVocabularyQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserVocabularyQuizRepository extends JpaRepository<UserVocabularyQuiz, Long> {
    Optional<UserVocabularyQuiz> findByUserIdAndNormalizedTerm(Long userId, String normalizedTerm);

    List<UserVocabularyQuiz> findByUserIdOrderByUpdatedAtDesc(Long userId);

    long countByUserId(Long userId);
}
