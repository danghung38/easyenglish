package com.dxh.Elearning.repo;

import com.dxh.Elearning.dto.response.StorySummaryResponse;
import com.dxh.Elearning.entity.Story;
import com.dxh.Elearning.enums.StoryLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface StoryRepository extends JpaRepository<Story, Long> {
    @Query("""
            select new com.dxh.Elearning.dto.response.StorySummaryResponse(
                s.id, s.title, s.titleVi, s.summary, s.imageUrl, s.level
            )
            from Story s
            where s.deletedAt is null and (:level is null or s.level = :level)
            order by s.id
            """)
    List<StorySummaryResponse> findSummaries(@Param("level") StoryLevel level);

    Optional<Story> findByIdAndDeletedAtIsNull(Long id);

    // Keep deleted sample keys so a restart does not recreate them.
    @Query("select s.seedKey from Story s where s.seedKey is not null")
    Set<String> findSeedKeys();
}
