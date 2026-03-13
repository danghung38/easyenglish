package com.dxh.Elearning.dto.response;

import com.dxh.Elearning.entity.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse implements Serializable {
    Long id;
    String username;
    String name;
    String email;
    String phoneNumber;
    LocalDate dob;
    Double bandsTarget;
    String gender;
    String avatar;
    Set<Role> roles;
}

