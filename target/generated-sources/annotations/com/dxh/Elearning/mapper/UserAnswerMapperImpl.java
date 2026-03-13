package com.dxh.Elearning.mapper;

import com.dxh.Elearning.dto.response.AnswerRLPartResponse;
import com.dxh.Elearning.entity.UserAnswer;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-18T23:49:22+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
)
@Component
public class UserAnswerMapperImpl implements UserAnswerMapper {

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public AnswerRLPartResponse toAnswerRLPartResponse(UserAnswer userAnswer) {
        if ( userAnswer == null ) {
            return null;
        }

        AnswerRLPartResponse.AnswerRLPartResponseBuilder answerRLPartResponse = AnswerRLPartResponse.builder();

        answerRLPartResponse.questionResponse( questionMapper.toQuestionResponse( userAnswer.getQuestion() ) );
        answerRLPartResponse.id( userAnswer.getId() );
        answerRLPartResponse.selectedOptionId( userAnswer.getSelectedOptionId() );
        answerRLPartResponse.score( userAnswer.getScore() );

        return answerRLPartResponse.build();
    }
}
