package com.dxh.Elearning.mapper;
import com.dxh.Elearning.dto.request.UserCreationRequest;
import com.dxh.Elearning.dto.response.UserResponse;
import com.dxh.Elearning.dto.response.UserUpdateResponse;
import com.dxh.Elearning.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);
    
    @Mapping(source = "fullName", target = "name")
    UserResponse toUserResponse(User user);

    UserUpdateResponse toUserUpdateResponse(User user);
}
