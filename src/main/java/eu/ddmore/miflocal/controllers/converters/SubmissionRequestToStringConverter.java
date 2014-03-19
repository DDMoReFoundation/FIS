/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.miflocal.controllers.converters;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import eu.ddmore.miflocal.domain.SubmissionRequest;


@Component
public class SubmissionRequestToStringConverter implements Converter<SubmissionRequest, String> {

    @Override
    public String convert(SubmissionRequest submissionRequest) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(submissionRequest);
        } catch (Exception e) {
            throw new RuntimeException("Could not produce json", e);
        }
    }

}
