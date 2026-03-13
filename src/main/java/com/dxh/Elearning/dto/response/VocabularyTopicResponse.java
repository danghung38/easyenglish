package com.dxh.Elearning.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VocabularyTopicResponse {
    String title;       // Tên chủ đề: Environment, Technology, etc.
    Integer wordCount;  // Số lượng từ trong chủ đề
    String gradient;    // Gradient màu cho UI
    String iconColor;   // Màu icon
}
