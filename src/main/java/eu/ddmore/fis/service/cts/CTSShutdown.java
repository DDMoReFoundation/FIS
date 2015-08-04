/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.cts;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import eu.ddmore.fis.service.Shutdown;


/**
 * Requests a shutdown of a remote CTS instance
 */
@Component("ctsShutdown")
public class CTSShutdown implements Shutdown {
    private static final Logger LOG = Logger.getLogger(CTSShutdown.class);
    private final RestTemplate restTemplate;
    private final String ctsUrl;
    private final String shutdownEndpoint;
    
    @Autowired(required=true)
    public CTSShutdown(@Qualifier("ctsRestTemplate") RestTemplate restTemplate, @Value("${fis.cts.management.url}") String ctsUrl, @Value("${fis.cts.management.shutdown}") String shutdownEndpoint) {
        this.restTemplate = restTemplate;
        this.ctsUrl = ctsUrl;
        this.shutdownEndpoint = shutdownEndpoint;
    }

    @Override
    public void invoke() {
        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(String.format("%s/%s",ctsUrl,shutdownEndpoint), null, Map.class, new Object[0]);
            LOG.info(String.format("Remote Converter Toolbox Service at [%s] responded with status [%s] and content [%s] to shutdown request.", ctsUrl, response.getStatusCode(), response.getBody()));
        } catch(Exception ex) {
            LOG.error(String.format("Error when trying to shutdown Converter Toolbox Service at [%s].", ctsUrl),ex);
        }
    }

}
