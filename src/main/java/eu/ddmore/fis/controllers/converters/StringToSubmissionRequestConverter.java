/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.miflocal.controllers.converters;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import eu.ddmore.miflocal.domain.SubmissionRequest;


@Component
public class StringToSubmissionRequestConverter implements Converter<String, SubmissionRequest> {
    @Override
    public SubmissionRequest convert(String submissionRequest) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(submissionRequest,SubmissionRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Could parse json %s",submissionRequest), e);
        }
    }

}
