/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers.converters;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import eu.ddmore.fis.domain.WriteMdlResponse;


@Component
public class StringToWriteMdlResponseConverter implements Converter<String, WriteMdlResponse> {
    @Override
    public WriteMdlResponse convert(String writeMdlResponse) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(writeMdlResponse, WriteMdlResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Couldn't parse json %s", writeMdlResponse), e);
        }
    }
}