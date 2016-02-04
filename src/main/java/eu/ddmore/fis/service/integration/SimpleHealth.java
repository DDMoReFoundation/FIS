/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.integration;

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
