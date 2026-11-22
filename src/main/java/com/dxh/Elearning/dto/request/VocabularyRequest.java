package com.dxh.Elearning.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VocabularyRequest {
    String word;                  // Từ tiếng Anh
    String meaning;               // Nghĩa tiếng Việt
    String pronunciation;         // Phát âm IPA text
    String pronunciationAudioUrl; // URL file audio phát âm
    String example;               // Ví dụ đặt câu
    String imageUrl;              // URL hình ảnh
    String topic;                 // Chủ đề
}
