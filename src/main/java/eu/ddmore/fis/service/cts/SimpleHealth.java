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
package eu.ddmore.fis.service.cts;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.boot.actuate.health.Health;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

/**
 * This class is used to unmarshall an unfortunate implementation of Health object in Spring (no no-arg constructor) which
 * can't be automatically unmarshalled.
 */
public class SimpleHealth {
    private String code;
    private Map<String, Object> details = new HashMap<>();
    public void setStatus(String code) {
        this.code = code;
    }
    
    public String getStatus() {
        return code;
    }
    /**
     * @return Health object that this SimpleHealth instance represents
     */
    public Health asHealth() {
        Health.Builder builder = Health.status(code);
        for(Entry<String, Object> en : details.entrySet()) {
            builder.withDetail(en.getKey(), en.getValue());
        }
        return builder.build();
    }
    
    @JsonAnyGetter
    public Map<String,Object> getDetails() {
        return details;
    }

    @JsonAnySetter
    public void setDetails(String name, Object value) {
        details.put(name, value);
    }
}
