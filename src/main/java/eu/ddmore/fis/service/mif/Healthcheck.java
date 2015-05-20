/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.mif;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.mango.mif.MIFHttpRestClient;

import eu.ddmore.fis.service.HealthDetail;


/**
 * Invokes health check on MIF instance
 */
@Component("mifHealth")
public class Healthcheck implements HealthIndicator {
    private static final Logger LOG = Logger.getLogger(Healthcheck.class);
    private MIFHttpRestClient mifClient;
    private String mifUrl;
    
    @Autowired(required=true)
    public Healthcheck(@Qualifier("mifRestClient") MIFHttpRestClient mifClient, @Value("${mif.url}") String mifUrl) {
        Preconditions.checkNotNull(mifClient, "MIF HTTP Rest client can't be null.");
        Preconditions.checkArgument(StringUtils.isNotEmpty(mifUrl), "MIF URL can't be blank.");
        this.mifClient = mifClient;
        this.mifUrl = mifUrl;
    }
    
    @Override
    public Health health() {
        try {
            if(!mifClient.healthcheck()) {
                return buildDownHealth();
            } else {
                return Health.up().build();
            }
        } catch (Exception ex) {
            LOG.error(String.format("Error when trying to check health of MIF at %s", mifUrl));
            return buildDownHealth();
        }
    }

    private Health buildDownHealth() {
        return Health.down().withDetail(HealthDetail.ERROR, "MIF is not running").withDetail(HealthDetail.URL, mifUrl).build();
    }
}
