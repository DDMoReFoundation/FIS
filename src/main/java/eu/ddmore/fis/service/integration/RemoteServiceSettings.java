/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.integration;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

/**
 * Configuration object with common remote service integration properties.
 */
public class RemoteServiceSettings {

    @NotBlank
    private String url;
    
    @NotNull
    private Management management;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Management getManagement() {
        return management;
    }

    public void setManagement(Management management) {
        this.management = management;
    }

    /**
     * Configuration object holding management HTTP endpoint details.
     */
    public static class Management {
        @NotBlank
        private String url;
        @NotBlank
        private String shutdown;
        @NotBlank
        private String healthcheck;
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public String getShutdown() {
            return shutdown;
        }
        
        public void setShutdown(String shutdown) {
            this.shutdown = shutdown;
        }
    
        public String getHealthcheck() {
            return healthcheck;
        }
    
        public void setHealthcheck(String healthcheck) {
            this.healthcheck = healthcheck;
        }
    }
}