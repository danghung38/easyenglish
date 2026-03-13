package com.dxh.Elearning.mapper;

import com.dxh.Elearning.dto.response.OptionResponse;
import com.dxh.Elearning.entity.Option;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-18T23:49:22+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
)
@Component
public class OptionMapperImpl implements OptionMapper {

    @Override
    public OptionResponse toOptionResponse(Option option) {
        if ( option == null ) {
            return null;
        }

        OptionResponse.OptionResponseBuilder optionResponse = OptionResponse.builder();

        optionResponse.id( option.getId() );
        optionResponse.content( option.getContent() );

        return optionResponse.build();
    }
}
