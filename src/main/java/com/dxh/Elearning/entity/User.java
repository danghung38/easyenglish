package com.dxh.Elearning.entity;

import com.dxh.Elearning.enums.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "users")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends AbstractEntity<Long> {

    @Column(unique = true, nullable = false)
    String username;

    @Enumerated(EnumType.STRING)
    Gender gender;

    String fullName;

    @Column(unique = true, nullable = false)
    String email;

    String avatar;

    @Column(unique = true, nullable = false)
    String phoneNumber;

    String password;

    @Column(name = "enabled")
    Boolean enabled;

    @DateTimeFormat(pattern = "yyyy/MM/dd")
    LocalDate dob;

    Double bandsTarget;

    @ManyToMany
    Set<Role> roles;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    Set<VerificationToken> verificationToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    Set<UserExam> exams;


}