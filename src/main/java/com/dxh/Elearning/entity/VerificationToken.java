package com.dxh.Elearning.entity;

import com.dxh.Elearning.enums.VerifyType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "verificationTokens")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerificationToken implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    String secretKey;

    @ManyToOne
    @JoinColumn(nullable = false,name = "user_id")
    User user;

    LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    VerifyType verifyType;

}
