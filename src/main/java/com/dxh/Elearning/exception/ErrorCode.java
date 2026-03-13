package com.dxh.Elearning.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(400, "User existed", HttpStatus.BAD_REQUEST),
    PHONE_EXISTED(400, "Phone existed", HttpStatus.BAD_REQUEST),
    ROLE_NOT_EXISTED(404, "Role not existed", HttpStatus.NOT_FOUND),
    USERNAME_INVALID(400, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(400, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_OLD_PASSWORD(400, "Old password not match", HttpStatus.BAD_REQUEST),
    INVALID_NAME(400, "Name cannot be left blank", HttpStatus.BAD_REQUEST),
    INVALID_PHONENUMBER(400, "Enter correct phone number format", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(400, "Enter correct email format", HttpStatus.BAD_REQUEST),
    INVALID_GENDER(400, "gender must be any of {MALE, FEMALE, OTHERS}", HttpStatus.BAD_REQUEST),
    INVALID_ORDERSTATUS(400, "order status must be any of {PENDING_CONFIRMATION, CANCELED, DELIVERED, SHIPPING}", HttpStatus.BAD_REQUEST),
    INVALID_BLANK(400, "Please do not leave the required information blank", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(404, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(401, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(403, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(400, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    UPLOAD_FAIL(400, "Error uploading file to S3", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(400, "Invalid request", HttpStatus.BAD_REQUEST),
    INVALID_FORMATDOB(400, "Dob must be format yyyy/MM/dd", HttpStatus.BAD_REQUEST),
    CATEGORY_EXISTED(400, "Category existed", HttpStatus.BAD_REQUEST),
    CART_NOT_EXISTED(400, "Cart not existed", HttpStatus.BAD_REQUEST),
    CARTITEM_NOT_EXISTED(400, "Cartitem not existed", HttpStatus.BAD_REQUEST),
    ADDRESS_NOT_EXITSTED(400, "Address not existed", HttpStatus.BAD_REQUEST),
    PRODUCT_EXISTED(400, "Product existed", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_EXISTED(400, "Product not existed", HttpStatus.BAD_REQUEST),
    INVALID_NULL(400, "Please do not leave the required information blank", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_EXIST(400, "Category not exist", HttpStatus.BAD_REQUEST),
    INVALID_NUMBER_QUANTITY(400, "quantity not match", HttpStatus.BAD_REQUEST),
    QUANTITY_INVALID(400, "quantity must be greater or equal than {value} ", HttpStatus.BAD_REQUEST),
    DISCOUNT_INVALID(400, "discount must be greater or equal than {value} percent", HttpStatus.BAD_REQUEST),

    ADDRESS_DUPLICATE(400,"dulicate addess" ,HttpStatus.BAD_REQUEST ),
    DISCOUNT_NOT_EXISTED(400, "discount not existed", HttpStatus.BAD_REQUEST),
    DISCOUNT_DUPLICATE(400,"dulicate discount" ,HttpStatus.BAD_REQUEST ),
    CART_EMPTY(400,"cart is empty" ,HttpStatus.BAD_REQUEST ),
    DISCOUNT_EXPIRED(400,"discount expired" ,HttpStatus.BAD_REQUEST ),
    ORDER_NOT_EXISTED(400,"order not existed" ,HttpStatus.BAD_REQUEST ),
    EMAIL_EXISTED(400, "email existed",HttpStatus.BAD_REQUEST ),
    SEND_FAILED(400,"Send email verify failed" ,HttpStatus.BAD_REQUEST ),
    INVALID_VERIFY_KEY(400,"verify key not match" ,HttpStatus.BAD_REQUEST ),
    VERIFY_KEY_EXPIRED(400,"verify key expiredd" ,HttpStatus.BAD_REQUEST ),
    ALREADY_VERIFIED(400,"user already verified" ,HttpStatus.BAD_REQUEST ),
    ACCOUNT_NOT_VERIFIED(400, "Account not verified. Please check your email to verify your account.", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(400, "Invalid username or password", HttpStatus.BAD_REQUEST),
    NOT_FOUND_IMAGE(400,"Image not found" ,HttpStatus.BAD_REQUEST ),
    EXAM_NOT_EXISTED(400, "exam not found",HttpStatus.BAD_REQUEST ),
    USER_EXAM_PART_NOT_EXISTED(400, "exam not found",HttpStatus.BAD_REQUEST ),
    QUESTION_NOT_FOUND(400, "question not found",HttpStatus.BAD_REQUEST ),
    EXAM_PART_NOT_EXISTED(400, "exam part not found",HttpStatus.BAD_REQUEST ),
    USER_EXAM_PART_NOT_FOUND(404, "User exam part not found", HttpStatus.NOT_FOUND),
    INVALID_SKILL_TYPE(400, "Invalid skill type for this operation", HttpStatus.BAD_REQUEST),
    USER_ANSWER_NOT_FOUND(404, "User answer not found", HttpStatus.NOT_FOUND),
    TRANSCRIPT_REQUIRED(400, "Transcript is required for speaking grading", HttpStatus.BAD_REQUEST),
    AI_RESPONSE_PARSE_ERROR(500, "Failed to parse AI response", HttpStatus.INTERNAL_SERVER_ERROR),
    TRANSCRIPTION_FAILED(500, "Audio transcription failed", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_FILE(400, "Invalid audio file", HttpStatus.BAD_REQUEST),
    WHISPER_SERVICE_UNAVAILABLE(503, "Whisper service is unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    EVALUATION_FAILED(500, "AI evaluation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    IELTS_SERVICE_UNAVAILABLE(503, "IELTSensei service is unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    INVALID_QUESTION_TYPE(400, "Question type does not match the requested skill (SPEAKING/WRITING)", HttpStatus.BAD_REQUEST),
    INVALID_QUESTION_CONTENT(400, "Question content is empty or invalid", HttpStatus.BAD_REQUEST),
    USER_EXAM_NOT_EXISTED(400,"user exam not found" ,HttpStatus.BAD_REQUEST ),
    EXAM_PART_NOT_FOUND(404, "Exam part not found", HttpStatus.NOT_FOUND),
    SCHEDULE_NOT_FOUND(404, "Schedule event not found", HttpStatus.NOT_FOUND),
    EXAM_NOT_FOUND(404, "Exam not found", HttpStatus.NOT_FOUND),
    
    // RAG Examples Management
    RAG_FILE_READ_ERROR(500, "Failed to read RAG examples file", HttpStatus.INTERNAL_SERVER_ERROR),
    RAG_FILE_WRITE_ERROR(500, "Failed to write RAG examples file", HttpStatus.INTERNAL_SERVER_ERROR),
    EXAMPLE_NOT_FOUND(404, "Training example not found", HttpStatus.NOT_FOUND),
    INVALID_EXAMPLE_TYPE(400, "Invalid example type. Must be 'speaking' or 'writing'", HttpStatus.BAD_REQUEST),
    INVALID_EXAMPLE_ID(400, "Invalid example ID format", HttpStatus.BAD_REQUEST),
    AI_SERVICE_UNAVAILABLE(503, "AI service is unavailable", HttpStatus.SERVICE_UNAVAILABLE);

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;
}
