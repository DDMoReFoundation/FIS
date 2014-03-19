/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers.converters;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import eu.ddmore.fis.domain.LocalJob;

@Component
public class LocalJobToStringConverter implements Converter<LocalJob, String> {
    @Override
    public String convert(LocalJob job) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(job);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Could not produce json for %s",job), e);
        }
    }

}
