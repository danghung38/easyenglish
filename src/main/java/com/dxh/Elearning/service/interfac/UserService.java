package com.dxh.Elearning.service.interfac;

import com.dxh.Elearning.dto.request.ChangePasswordRequest;
import com.dxh.Elearning.dto.request.ForgotPasswordRequest;
import com.dxh.Elearning.dto.request.ResetPasswordRequest;
import com.dxh.Elearning.dto.request.UserCreationRequest;
import com.dxh.Elearning.dto.request.UserUpdateRequest;
import com.dxh.Elearning.dto.response.PageResponse;
import com.dxh.Elearning.dto.response.UserResponse;
import com.dxh.Elearning.dto.response.UserUpdateResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserCreationRequest request);

    PageResponse<List<UserResponse>> getAllUsersSortBy(int pageNo, int pageSize, String sortBy);

    PageResponse<List<UserResponse>> advanceSearchWithSpecifications(Pageable pageable, String[] user, String[] role);

    void verifyRegister(String secretKey);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(ChangePasswordRequest request);

    UserUpdateResponse updateMyUser(UserUpdateRequest request, MultipartFile userImage);

    UserResponse getMyInfo();
}
