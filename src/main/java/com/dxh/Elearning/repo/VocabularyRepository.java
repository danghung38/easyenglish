package com.dxh.Elearning.repo;

import com.dxh.Elearning.entity.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {
    List<Vocabulary> findByTopic(String topic);
    
    @Query("SELECT DISTINCT v.topic FROM Vocabulary v")
    List<String> findAllDistinctTopics();
}
