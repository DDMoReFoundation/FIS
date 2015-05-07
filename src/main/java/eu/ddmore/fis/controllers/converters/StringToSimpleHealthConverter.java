/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers.converters;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import eu.ddmore.fis.service.cts.SimpleHealth;


@Component
public class StringToSimpleHealthConverter implements Converter<String, SimpleHealth> {
    @Override
    public SimpleHealth convert(String health) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(health, SimpleHealth.class);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Couldn't parse json %s", health), e);
        }
    }

}