package com.dxh.Elearning.controller;

import com.dxh.Elearning.dto.request.*;
import com.dxh.Elearning.dto.response.ApiResponse;
import com.dxh.Elearning.dto.response.PageResponse;
import com.dxh.Elearning.dto.response.UserResponse;
import com.dxh.Elearning.dto.response.UserUpdateResponse;
import com.dxh.Elearning.service.AwsS3Service;
import com.dxh.Elearning.service.interfac.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {
    UserService userService;
    AwsS3Service awsS3Service;

    @Operation(method = "POST", summary = "Add new user",
            description = "Send a request via this API to create new user")
    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request){
        return ApiResponse.<UserResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Successfully created new user")
                .result(userService.createUser(request))
                .build();
    }

    @Operation(method = "GET", summary = "Get my info",
            description = "Send a request via this API to get my info")
    @GetMapping("/myInfo")
    ApiResponse<UserResponse> getMyInfo(){
        return ApiResponse.<UserResponse>builder()
                .code(HttpStatus.OK.value())
                .result(userService.getMyInfo())
                .build();
    }

    @Operation(method = "PUT", summary = "Update my user",
            description = "Send a request via this API to update my user")
    @PutMapping
    ApiResponse<UserUpdateResponse> updateUser(@RequestPart("user") @Valid UserUpdateRequest request,
                                               @RequestPart(value = "file",required = false) MultipartFile userImage){
        return ApiResponse.<UserUpdateResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully update user")
                .result(userService.updateMyUser(request,userImage))
                .build();
    }

    @Operation(method = "POST", summary = "Forgot password",
            description = "Fogot password")
    @PostMapping("/forgot-password")
    ApiResponse<?> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request){
        userService.forgotPassword(request);
        return ApiResponse.<UserResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Send otp reset password successful")
                .build();
    }

    @Operation(method = "POST", summary = "Reset password",
            description = "Reset password")
    @PostMapping("/reset-password")
    ApiResponse<?> resetPassword(@RequestBody @Valid ResetPasswordRequest request){
        userService.resetPassword(request);
        return ApiResponse.<UserResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Reset password successful")
                .build();
    }

    @Operation(method = "POST", summary = "Change password",
            description = "Change password for authenticated user")
    @PostMapping("/change-password")
    ApiResponse<?> changePassword(@RequestBody @Valid ChangePasswordRequest request){
        userService.changePassword(request);
        return ApiResponse.builder()
                .code(HttpStatus.OK.value())
                .message("Password changed successfully")
                .build();
    }

    //test ảnh
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<String> addNewRoom(
            @RequestParam(value = "photo", required = false) MultipartFile photo
    ) {

        return ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .result(awsS3Service.saveImageToS3(photo))
                .build();
    }

    //xác thực email
    @Operation(summary = "Confirm Email", description = "Confirm email for account")
    @GetMapping("/confirm-email")
    public void confirmEmail(@RequestParam String secretKey, HttpServletResponse response) throws IOException {
        log.info("Confirm email for account with secretCode: {}", secretKey);
        try {
            userService.verifyRegister(secretKey);
            response.sendRedirect("http://localhost:8080/api/admin-notify.html?verify=success");
        } catch (Exception e) {
            log.error("VerificationToken fail", e.getMessage(), e);
            response.sendRedirect("http://localhost:8080/api/admin-notify.html?verify=fail");
        }
    }

//    quên mật khẩu


    @Operation(summary = "Get list of users per pageNo and sort by one column", description = "Send a request via this API to get user list by pageNo and pageSize")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<PageResponse<List<UserResponse>>> getAllUsersSortBy(@RequestParam(defaultValue = "1", required = false) Integer pageNo,
                                                                     @Min(value = 1,message = "pageSize must be greater than 1") @RequestParam(defaultValue = "20", required = false) Integer pageSize,
                                                                     @RequestParam(required = false) String sortBy) {
        log.info("get all users");
        return ApiResponse.<PageResponse<List<UserResponse>>>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully get user list")
                .result(userService.getAllUsersSortBy(pageNo,pageSize,sortBy))
                .build();
    }


    @Operation(summary = "Advance search query by specifications", description = "Return list of users")
    @GetMapping( "/advance-search-with-specification")
    public ApiResponse<PageResponse<List<UserResponse>>> advanceSearchWithSpecifications(Pageable pageable,
                                                           @RequestParam(required = false) String[] user,
                                                           @RequestParam(required = false) String[] role) {
        return ApiResponse.<PageResponse<List<UserResponse>>>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully get user list")
                .result(userService.advanceSearchWithSpecifications(pageable,user,role))
                .build();
    }
}
