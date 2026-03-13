package com.dxh.Elearning.mapper;

import com.dxh.Elearning.dto.request.ExamRequest;
import com.dxh.Elearning.dto.response.ExamPartResponse;
import com.dxh.Elearning.dto.response.ExamResponse;
import com.dxh.Elearning.entity.Exam;
import com.dxh.Elearning.entity.ExamPart;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-18T23:49:22+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
)
@Component
public class ExamMapperImpl implements ExamMapper {

    @Override
    public Exam toExam(ExamRequest req) {
        if ( req == null ) {
            return null;
        }

        Exam.ExamBuilder exam = Exam.builder();

        exam.title( req.getTitle() );
        exam.description( req.getDescription() );
        exam.totalDuration( req.getTotalDuration() );

        return exam.build();
    }

    @Override
    public ExamResponse toExamResponse(Exam exam) {
        if ( exam == null ) {
            return null;
        }

        ExamResponse.ExamResponseBuilder examResponse = ExamResponse.builder();

        examResponse.examParts( examPartSetToExamPartResponseList( exam.getParts() ) );
        examResponse.id( exam.getId() );
        examResponse.title( exam.getTitle() );
        examResponse.description( exam.getDescription() );
        examResponse.totalDuration( exam.getTotalDuration() );
        examResponse.imageUrl( exam.getImageUrl() );

        return examResponse.build();
    }

    @Override
    public ExamPartResponse toExamPartResponse(ExamPart examPart) {
        if ( examPart == null ) {
            return null;
        }

        ExamPartResponse.ExamPartResponseBuilder examPartResponse = ExamPartResponse.builder();

        examPartResponse.examId( examPartExamId( examPart ) );
        examPartResponse.id( examPart.getId() );
        examPartResponse.skillType( examPart.getSkillType() );
        examPartResponse.duration( examPart.getDuration() );

        return examPartResponse.build();
    }

    protected List<ExamPartResponse> examPartSetToExamPartResponseList(Set<ExamPart> set) {
        if ( set == null ) {
            return null;
        }

        List<ExamPartResponse> list = new ArrayList<ExamPartResponse>( set.size() );
        for ( ExamPart examPart : set ) {
            list.add( toExamPartResponse( examPart ) );
        }

        return list;
    }

    private Long examPartExamId(ExamPart examPart) {
        if ( examPart == null ) {
            return null;
        }
        Exam exam = examPart.getExam();
        if ( exam == null ) {
            return null;
        }
        Long id = exam.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
