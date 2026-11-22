package com.dxh.Elearning.dto.request;


import com.dxh.Elearning.enums.SkillType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserExamRequest {

    @NotNull(message = "INVALID_NULL")
    Long examId;
    
    // Optional: Chỉ định part cụ thể cần tạo (LISTENING, READING, WRITING, SPEAKING)
    // Nếu null hoặc rỗng = tạo Full Test (tất cả 4 parts)
    SkillType skillType;
}
