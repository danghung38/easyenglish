package com.dxh.Elearning.mapper;

import com.dxh.Elearning.dto.response.OptionResponse;
import com.dxh.Elearning.dto.response.QuestionResponse;
import com.dxh.Elearning.entity.ExamPart;
import com.dxh.Elearning.entity.Option;
import com.dxh.Elearning.entity.Question;
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
public class QuestionMapperImpl implements QuestionMapper {

    @Autowired
    private OptionMapper optionMapper;

    @Override
    public QuestionResponse toQuestionResponse(Question question) {
        if ( question == null ) {
            return null;
        }

        QuestionResponse.QuestionResponseBuilder questionResponse = QuestionResponse.builder();

        questionResponse.examPartId( questionExamPartId( question ) );
        questionResponse.correctOptionId( questionCorrectOptionId( question ) );
        questionResponse.optionRes( optionListToOptionResponseList( question.getOptions() ) );
        questionResponse.id( question.getId() );
        questionResponse.skillType( question.getSkillType() );
        questionResponse.isSection( question.getIsSection() );
        questionResponse.content( question.getContent() );
        questionResponse.audioUrl( question.getAudioUrl() );
        questionResponse.imageUrl( question.getImageUrl() );
        questionResponse.maxScore( question.getMaxScore() );
        questionResponse.section( question.getSection() );
        questionResponse.explain( question.getExplain() );
        questionResponse.transcript( question.getTranscript() );

        return questionResponse.build();
    }

    private Long questionExamPartId(Question question) {
        if ( question == null ) {
            return null;
        }
        ExamPart examPart = question.getExamPart();
        if ( examPart == null ) {
            return null;
        }
        Long id = examPart.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long questionCorrectOptionId(Question question) {
        if ( question == null ) {
            return null;
        }
        Option correctOption = question.getCorrectOption();
        if ( correctOption == null ) {
            return null;
        }
        Long id = correctOption.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected List<OptionResponse> optionListToOptionResponseList(List<Option> list) {
        if ( list == null ) {
            return null;
        }

        List<OptionResponse> list1 = new ArrayList<OptionResponse>( list.size() );
        for ( Option option : list ) {
            list1.add( optionMapper.toOptionResponse( option ) );
        }

        return list1;
    }
}
