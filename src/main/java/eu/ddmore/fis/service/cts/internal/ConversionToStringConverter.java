/*******************************************************************************
 * Copyright (C) 2016 Mango Business Solutions Ltd, http://www.mango-solutions.com
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
 *******************************************************************************/
package eu.ddmore.fis.service.cts.internal;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ddmore.convertertoolbox.domain.Conversion;

@Component
public class ConversionToStringConverter implements Converter<Conversion, String> {
    @Override
    public String convert(Conversion conversion) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(conversion);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Could not produce json for %s",conversion), e);
        }
    }
}
