package com.dxh.Elearning.entity;

import com.dxh.Elearning.enums.StoryLevel;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stories")
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Story {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 160)
    String title;

    @Column(nullable = false, length = 160)
    String titleVi;

    @Column(length = 500)
    String summary;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    String content;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    String translation;

    @Column(length = 2048)
    String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    StoryLevel level;

    @Column(unique = true, length = 80)
    String seedKey;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;
}
