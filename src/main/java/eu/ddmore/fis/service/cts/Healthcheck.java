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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Preconditions;

import eu.ddmore.fis.service.HealthDetail;


/**
 * Invokes health check on CTS  instance
 */
@Component("ctsHealth")
public class Healthcheck implements HealthIndicator {
    private static final Logger LOG = Logger.getLogger(Healthcheck.class);
    private final RestTemplate restTemplate;
    private final String ctsUrl;
    private final String healthcheckEndpoint;
    
    @Autowired(required=true)
    public Healthcheck(@Qualifier("ctsRestTemplate") RestTemplate restTemplate, @Value("${fis.cts.management.url}") String ctsUrl, @Value("${fis.cts.management.healthcheck}") String healthcheckEndpoint) {
        Preconditions.checkNotNull(restTemplate, "Rest template can't be null.");
        Preconditions.checkArgument(StringUtils.isNotEmpty(ctsUrl), "CTS Management URL can't be blank.");
        Preconditions.checkArgument(StringUtils.isNotEmpty(healthcheckEndpoint), "CTS healthcheck endpoint can't be blank.");
        this.restTemplate = restTemplate;
        this.ctsUrl = ctsUrl;
        this.healthcheckEndpoint = healthcheckEndpoint;
    }
    
    @Override
    public Health health() {
        ResponseEntity<SimpleHealth> ctsHealthResponse = null;
        try {
            ctsHealthResponse = restTemplate.getForEntity(String.format("%s/%s",ctsUrl,healthcheckEndpoint), SimpleHealth.class);
            if(!isUP(ctsHealthResponse)) {
                return buildDownHealth();
            } else {
                return Health.up().build();
            }
        } catch (Exception ex) {
            LOG.error(String.format("Error when trying to check health of CTS at %s", ctsUrl));
            return buildDownHealth();
        }
    }
    
    private Health buildDownHealth() {
        return Health.down().withDetail(HealthDetail.ERROR, "CTS is not running").withDetail(HealthDetail.URL, ctsUrl).build();
    }

    private boolean isUP(ResponseEntity<SimpleHealth> ctsHealthResponse) {
        return ctsHealthResponse.getStatusCode().is2xxSuccessful() 
                && ctsHealthResponse.hasBody()
                && Status.UP.equals(ctsHealthResponse.getBody().asHealth().getStatus());
    }
}
