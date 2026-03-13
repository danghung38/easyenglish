package com.dxh.Elearning.mapper;

import com.dxh.Elearning.dto.response.ExamPartResponse;
import com.dxh.Elearning.entity.Exam;
import com.dxh.Elearning.entity.ExamPart;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-18T23:49:22+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
)
@Component
public class ExamPartMapperImpl implements ExamPartMapper {

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
