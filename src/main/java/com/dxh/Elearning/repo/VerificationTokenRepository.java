package com.dxh.Elearning.repo;

import com.dxh.Elearning.entity.User;
import com.dxh.Elearning.entity.VerificationToken;
import com.dxh.Elearning.enums.VerifyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer> {
    Optional<VerificationToken> findBySecretKeyAndVerifyType(String secretKey, VerifyType verifyType);

    void deleteByUserAndVerifyType(User user, VerifyType verifyType);
}
