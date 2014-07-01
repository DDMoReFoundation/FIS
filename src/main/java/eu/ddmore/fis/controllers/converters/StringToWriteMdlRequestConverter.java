/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers.converters;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import eu.ddmore.fis.domain.WriteMdlRequest;


@Component
public class StringToWriteMdlRequestConverter implements Converter<String, WriteMdlRequest> {
    @Override
    public WriteMdlRequest convert(String writeMdlRequest) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(writeMdlRequest, WriteMdlRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Couldn't parse json %s", writeMdlRequest), e);
        }
    }
}
