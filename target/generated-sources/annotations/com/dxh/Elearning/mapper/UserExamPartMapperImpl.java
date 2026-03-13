package com.dxh.Elearning.mapper;

import com.dxh.Elearning.dto.response.AnswerRLPartResponse;
import com.dxh.Elearning.dto.response.SubmitRLPartResponse;
import com.dxh.Elearning.dto.response.UserExamPartResponse;
import com.dxh.Elearning.entity.UserAnswer;
import com.dxh.Elearning.entity.UserExam;
import com.dxh.Elearning.entity.UserExamPart;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-18T23:49:22+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
)
@Component
public class UserExamPartMapperImpl implements UserExamPartMapper {

    @Autowired
    private UserAnswerMapper userAnswerMapper;

    @Override
    public UserExamPartResponse toUserExamResponse(UserExamPart userExamPart) {
        if ( userExamPart == null ) {
            return null;
        }

        UserExamPartResponse.UserExamPartResponseBuilder userExamPartResponse = UserExamPartResponse.builder();

        userExamPartResponse.userExamId( userExamPartUserExamId( userExamPart ) );
        userExamPartResponse.id( userExamPart.getId() );
        userExamPartResponse.skillType( userExamPart.getSkillType() );
        userExamPartResponse.submitted( userExamPart.getSubmitted() );
        userExamPartResponse.score( userExamPart.getScore() );

        return userExamPartResponse.build();
    }

    @Override
    public SubmitRLPartResponse toSubmitRLPartResponse(UserExamPart save) {
        if ( save == null ) {
            return null;
        }

        SubmitRLPartResponse.SubmitRLPartResponseBuilder submitRLPartResponse = SubmitRLPartResponse.builder();

        submitRLPartResponse.userExamId( userExamPartUserExamId( save ) );
        submitRLPartResponse.answers( userAnswerListToAnswerRLPartResponseList( save.getAnswers() ) );
        submitRLPartResponse.id( save.getId() );
        submitRLPartResponse.skillType( save.getSkillType() );
        submitRLPartResponse.submitted( save.getSubmitted() );
        submitRLPartResponse.score( save.getScore() );

        return submitRLPartResponse.build();
    }

    private Long userExamPartUserExamId(UserExamPart userExamPart) {
        if ( userExamPart == null ) {
            return null;
        }
        UserExam userExam = userExamPart.getUserExam();
        if ( userExam == null ) {
            return null;
        }
        Long id = userExam.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected List<AnswerRLPartResponse> userAnswerListToAnswerRLPartResponseList(List<UserAnswer> list) {
        if ( list == null ) {
            return null;
        }

        List<AnswerRLPartResponse> list1 = new ArrayList<AnswerRLPartResponse>( list.size() );
        for ( UserAnswer userAnswer : list ) {
            list1.add( userAnswerMapper.toAnswerRLPartResponse( userAnswer ) );
        }

        return list1;
    }
}
