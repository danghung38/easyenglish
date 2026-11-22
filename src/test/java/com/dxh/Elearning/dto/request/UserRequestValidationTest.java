package com.dxh.Elearning.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserRequestValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void creationRequestRejectsMissingDateOfBirth() {
        UserCreationRequest request = UserCreationRequest.builder()
                .username("learner01")
                .password("secret12")
                .fullName("Test Learner")
                .phoneNumber("0912345678")
                .email("learner@example.com")
                .gender("OTHER")
                .dob(null)
                .build();

        long missingDobViolations = validator.validate(request).stream()
                .filter(violation -> violation.getPropertyPath().toString().equals("dob"))
                .count();

        assertEquals(1, missingDobViolations);
    }

    @Test
    void updateRequestRejectsMissingDateOfBirth() {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .fullName("Test Learner")
                .gender("OTHER")
                .dob(null)
                .bandsTarget(7.0)
                .build();

        long missingDobViolations = validator.validate(request).stream()
                .filter(violation -> violation.getPropertyPath().toString().equals("dob"))
                .count();

        assertEquals(1, missingDobViolations);
    }
}
