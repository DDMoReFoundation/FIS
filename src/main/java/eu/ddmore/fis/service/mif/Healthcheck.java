/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.mif;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.mango.mif.MIFHttpRestClient;

import eu.ddmore.fis.service.HealthDetail;


/**
 * Invokes health check on MIF instance
 */
@Component("mifHealth")
public class Healthcheck implements HealthIndicator {

    private MIFHttpRestClient mifClient;
    
    @Value("${mif.url}")
    private String mifUrl;
    
    @Override
    public Health health() {
        if(!mifClient.healthcheck()) {
            return Health.down().withDetail(HealthDetail.ERROR, "MIF is not running").withDetail(HealthDetail.URL, mifUrl).build();
        } else {
            return Health.up().build();
        }
    }

    @Required
    public void setMifClient(MIFHttpRestClient mifClient) {
        this.mifClient = mifClient;
    }
    
    public MIFHttpRestClient getMifClient() {
        return mifClient;
    }
}
