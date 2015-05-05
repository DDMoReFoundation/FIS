/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.cts;

import org.springframework.boot.actuate.health.Health;

/**
 * This class is used to unmarshall an unfortunate implementation of Health object in Spring (no no-arg constructor) which
 * can't be automatically unmarshalled.
 */
public class SimpleHealth {
    private String code;
    public void setStatus(String code) {
        this.code = code;
    }
    
    public String getStatus() {
        return code;
    }
    
    public Health asHealth() {
        return Health.status(code).build();
    }
    
}
