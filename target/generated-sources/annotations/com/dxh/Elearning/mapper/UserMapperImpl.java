package com.dxh.Elearning.mapper;

import com.dxh.Elearning.dto.request.UserCreationRequest;
import com.dxh.Elearning.dto.response.UserResponse;
import com.dxh.Elearning.dto.response.UserUpdateResponse;
import com.dxh.Elearning.entity.Role;
import com.dxh.Elearning.entity.User;
import com.dxh.Elearning.enums.Gender;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-18T23:49:22+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toUser(UserCreationRequest request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.username( request.getUsername() );
        if ( request.getGender() != null ) {
            user.gender( Enum.valueOf( Gender.class, request.getGender() ) );
        }
        user.fullName( request.getFullName() );
        user.email( request.getEmail() );
        user.phoneNumber( request.getPhoneNumber() );
        user.password( request.getPassword() );
        user.dob( request.getDob() );

        return user.build();
    }

    @Override
    public UserResponse toUserResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.name( user.getFullName() );
        userResponse.id( user.getId() );
        userResponse.username( user.getUsername() );
        userResponse.email( user.getEmail() );
        userResponse.phoneNumber( user.getPhoneNumber() );
        userResponse.dob( user.getDob() );
        userResponse.bandsTarget( user.getBandsTarget() );
        if ( user.getGender() != null ) {
            userResponse.gender( user.getGender().name() );
        }
        userResponse.avatar( user.getAvatar() );
        Set<Role> set = user.getRoles();
        if ( set != null ) {
            userResponse.roles( new LinkedHashSet<Role>( set ) );
        }

        return userResponse.build();
    }

    @Override
    public UserUpdateResponse toUserUpdateResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserUpdateResponse.UserUpdateResponseBuilder userUpdateResponse = UserUpdateResponse.builder();

        userUpdateResponse.fullName( user.getFullName() );
        if ( user.getGender() != null ) {
            userUpdateResponse.gender( user.getGender().name() );
        }
        userUpdateResponse.dob( user.getDob() );
        userUpdateResponse.bandsTarget( user.getBandsTarget() );

        return userUpdateResponse.build();
    }
}
