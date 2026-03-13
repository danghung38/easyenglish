package com.dxh.Elearning.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamResponse {
    Long id;           // id của Exam sinh ra
    String title;
    String description;
    Integer totalDuration;
    String imageUrl;   // URL của ảnh exam
    List<ExamPartResponse> examParts;  // Danh sách các phần thi (Reading, Listening, Writing, Speaking)
}
