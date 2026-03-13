package com.dxh.Elearning.mapper;

import com.dxh.Elearning.dto.response.UserExamPartResponse;
import com.dxh.Elearning.dto.response.UserExamResponse;
import com.dxh.Elearning.entity.Exam;
import com.dxh.Elearning.entity.User;
import com.dxh.Elearning.entity.UserExam;
import com.dxh.Elearning.entity.UserExamPart;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-18T23:49:22+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
)
@Component
public class UserExamMapperImpl implements UserExamMapper {

    @Autowired
    private UserExamPartMapper userExamPartMapper;

    @Override
    public UserExamResponse toUserExamResponse(UserExam userExam) {
        if ( userExam == null ) {
            return null;
        }

        UserExamResponse.UserExamResponseBuilder userExamResponse = UserExamResponse.builder();

        userExamResponse.examId( userExamExamId( userExam ) );
        userExamResponse.userId( userExamUserId( userExam ) );
        userExamResponse.id( userExam.getId() );
        userExamResponse.startedAt( userExam.getStartedAt() );
        userExamResponse.parts( userExamPartSetToUserExamPartResponseList( userExam.getParts() ) );

        return userExamResponse.build();
    }

    private Long userExamExamId(UserExam userExam) {
        if ( userExam == null ) {
            return null;
        }
        Exam exam = userExam.getExam();
        if ( exam == null ) {
            return null;
        }
        Long id = exam.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long userExamUserId(UserExam userExam) {
        if ( userExam == null ) {
            return null;
        }
        User user = userExam.getUser();
        if ( user == null ) {
            return null;
        }
        Long id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected List<UserExamPartResponse> userExamPartSetToUserExamPartResponseList(Set<UserExamPart> set) {
        if ( set == null ) {
            return null;
        }

        List<UserExamPartResponse> list = new ArrayList<UserExamPartResponse>( set.size() );
        for ( UserExamPart userExamPart : set ) {
            list.add( userExamPartMapper.toUserExamResponse( userExamPart ) );
        }

        return list;
    }
}
