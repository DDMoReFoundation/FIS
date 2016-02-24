/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.integration;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Preconditions;

import eu.ddmore.fis.service.HealthDetail;


/**
 * Invokes health check on remote Spring Boot instance and parses it to a {@link SimpleHealth} structure
 */
public class SimpleRemoteServiceHealthIndicator implements HealthIndicator {
    private static final Logger LOG = Logger.getLogger(SimpleRemoteServiceHealthIndicator.class);
    private final RestTemplate restTemplate;
    private final String managementUrl;
    private final String healthcheckEndpoint;
    
    /**
     * Creates a new instance.
     * @param restTemplate - template that should be used to invoke REST service
     * @param managementUrl - url at which the remote service is available
     * @param shutdownEndpoint - health endpoint name
     */
    public SimpleRemoteServiceHealthIndicator(RestTemplate restTemplate, String managementUrl, String healthcheckEndpoint) {
        Preconditions.checkNotNull(restTemplate, "Rest template can't be null.");
        Preconditions.checkArgument(StringUtils.isNotEmpty(managementUrl), "Management URL can't be blank.");
        Preconditions.checkArgument(StringUtils.isNotEmpty(healthcheckEndpoint), "Healthcheck endpoint name can't be blank.");
        this.restTemplate = restTemplate;
        this.managementUrl = managementUrl;
        this.healthcheckEndpoint = healthcheckEndpoint;
    }
    
    @Override
    public Health health() {
        ResponseEntity<SimpleHealth> healthResponse = null;
        try {
            healthResponse = restTemplate.getForEntity(String.format("%s/%s",managementUrl,healthcheckEndpoint), SimpleHealth.class);
            if(!isUP(healthResponse)) {
                return buildDownHealth();
            } else {
                return Health.up().build();
            }
        } catch (Exception ex) {
            LOG.error(String.format("Error when trying to check health of remote service at %s", managementUrl), ex);
            return buildDownHealth();
        }
    }
    
    private Health buildDownHealth() {
        return Health.down().withDetail(HealthDetail.ERROR, "Remote service is down.").withDetail(HealthDetail.URL, managementUrl).build();
    }

    private boolean isUP(ResponseEntity<SimpleHealth> healthResponse) {
        return healthResponse.getStatusCode().is2xxSuccessful() 
                && healthResponse.hasBody()
                && Status.UP.equals(healthResponse.getBody().asHealth().getStatus());
    }
}
