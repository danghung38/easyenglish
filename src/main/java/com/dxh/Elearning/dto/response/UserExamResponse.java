package com.dxh.Elearning.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserExamResponse {
    Long id;
    Long userId;
    Long examId;
    LocalDateTime startedAt;
    List<UserExamPartResponse> parts;
    
    // For leaderboard
    String userName;
    String fullName;
    Double score;
    Integer rank;
    Integer totalTests;
    Boolean currentUser;
}
