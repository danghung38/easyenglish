package com.dxh.Elearning.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "vocabularies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Vocabulary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @Column(nullable = false)
    String word; // Từ tiếng Anh
    
    @Column(nullable = false, columnDefinition = "TEXT")
    String meaning; // Nghĩa tiếng Việt
    
    String pronunciation; // Phát âm IPA text (ví dụ: /ɪnˈvaɪrənmənt/)
    
    @Column(name = "pronunciation_audio_url")
    String pronunciationAudioUrl; // URL file audio phát âm
    
    @Column(columnDefinition = "TEXT")
    String example; // Ví dụ đặt câu
    
    @Column(name = "image_url")
    String imageUrl; // URL hình ảnh minh họa
    
    @Column(nullable = false)
    String topic; // Chủ đề: Environment, Technology, Education, Health, Business
    
    @Column(name = "created_at")
    LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
