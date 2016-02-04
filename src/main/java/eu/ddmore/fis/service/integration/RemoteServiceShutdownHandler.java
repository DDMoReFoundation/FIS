/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.integration;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Preconditions;


/**
 * Requests a shutdown of a remote Spring Boot service
 */
public class RemoteServiceShutdownHandler implements ShutdownHandler {
    private static final Logger LOG = Logger.getLogger(RemoteServiceShutdownHandler.class);
    private final RestTemplate restTemplate;
    private final String managementUrl;
    private final String shutdownEndpoint;
    
    /**
     * Creates a new instance.
     * @param restTemplate - template that should be used to invoke REST service
     * @param managementUrl - url at which the remote service is available
     * @param shutdownEndpoint - shutdown endpoint name
     */
    public RemoteServiceShutdownHandler(RestTemplate restTemplate, String managementUrl, String shutdownEndpoint) {
        Preconditions.checkNotNull(restTemplate, "Rest template can't be null.");
        Preconditions.checkArgument(StringUtils.isNotEmpty(managementUrl), "Management URL can't be blank.");
        Preconditions.checkArgument(StringUtils.isNotEmpty(shutdownEndpoint), "Shutdown endpoint name can't be blank.");
        this.restTemplate = restTemplate;
        this.managementUrl = managementUrl;
        this.shutdownEndpoint = shutdownEndpoint;
    }

    @Override
    public void invoke() {
        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(String.format("%s/%s",managementUrl,shutdownEndpoint), null, Map.class, new Object[0]);
            LOG.info(String.format("Remote service at [%s] responded with status [%s] and content [%s] to shutdown request.", managementUrl, response.getStatusCode(), response.getBody()));
        } catch(Exception ex) {
            LOG.error(String.format("Error when trying to shutdown remote service at [%s].", managementUrl),ex);
        }
    }

}
