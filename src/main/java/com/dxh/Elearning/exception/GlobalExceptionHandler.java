package com.dxh.Elearning.exception;


import com.dxh.Elearning.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    //các lỗi khác
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<String>> handlingRuntimeException(Exception exception){
        log.error("Exception: ", exception);
        ApiResponse<String> apiResponse = new ApiResponse();

        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());
        String now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        apiResponse.setResult(now);

        return ResponseEntity.badRequest().body(apiResponse);
    }

    //bắt lỗi runtime
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<String>> handlingAppException(AppException exception){
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse<String> apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        String now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        apiResponse.setResult(now);

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(apiResponse);
    }

    //lỗi 403
    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse<String>> handlingAccessDeniedException(AccessDeniedException exception){
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        ApiResponse<String> apiResponse = new ApiResponse();
        String now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        apiResponse.setResult(now);

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    //lỗi size, notnull..
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<String>> handlingValidation(MethodArgumentNotValidException exception){
        String enumKey = exception.getFieldError().getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_KEY;

        Map<String, Object> attributes = null;
        try {
            errorCode = ErrorCode.valueOf(enumKey);

            var constraintViolation = exception.getBindingResult()
                    .getAllErrors()
                    .get(0)   // lấy phần tử đầu tiên
                    .unwrap(ConstraintViolation.class);

            attributes = constraintViolation.getConstraintDescriptor().getAttributes();
            log.info("attribute {}", attributes.toString());

        } catch (IllegalArgumentException e) {
            log.info(e.getMessage());
        }

        ApiResponse<String> apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(Objects.nonNull(attributes) ?
                mapAttribute(errorCode.getMessage(), attributes)
                : errorCode.getMessage());
        String now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        apiResponse.setResult(now);

        return ResponseEntity.badRequest().body(apiResponse);
    }


    //thay message cho số tuổi
    private String mapAttribute(String message, Map<String, Object> attributes){
        if (attributes.containsKey("min")) {
            String minValue = String.valueOf(attributes.get("min"));
            message = message.replace("{min}", minValue);
        }

        if (attributes.containsKey("value")) {
            String value = String.valueOf(attributes.get("value"));
            message = message.replace("{value}", value);
        }

        return message;
    }

//    min max trên params
    //    validate min max
    @ExceptionHandler(value = HandlerMethodValidationException.class)
    ResponseEntity<ApiResponse> handlerMethodValidationException(HandlerMethodValidationException exception){

        String now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String detailedMessage = exception.getAllValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ApiResponse apiResponse = ApiResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(detailedMessage) // Gán message cụ thể
                .result(now)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(apiResponse);

    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleEnumParseError(IllegalArgumentException ex) {
        String now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String message = ex.getMessage();
        if (message != null && message.contains("Gender")) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(ErrorCode.INVALID_GENDER.getMessage())
                            .result(now)
                            .build()
            );
        }

//        lỗi value sort not match
        if (message != null && message.contains("Invalid sort field")) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(message) // dùng message gốc
                            .result(now)
                            .build()
            );
        }

        if (message != null && message.contains("Sort direction")) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(message) // dùng message gốc
                            .result(now)
                            .build()
            );
        }

        return ResponseEntity.badRequest().body(
                ApiResponse.builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .message(ErrorCode.INVALID_REQUEST.getMessage())
                        .build()
        );
    }

//    bắt lỗi ngày tháng định dạng yyyy/mm/dd
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidDateFormat(HttpMessageNotReadableException ex) {

        String now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (ex.getMessage() != null && ex.getMessage().contains("LocalDate")) {
            ErrorCode errorCode= ErrorCode.INVALID_FORMATDOB;
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .code(errorCode.getCode())
                            .message(errorCode.getMessage())
                            .result(now)
                            .build()
            );
        }

        return ResponseEntity.badRequest().body(
                new ApiResponse<>(400, ex.getMessage(), null)
        );
    }

}
