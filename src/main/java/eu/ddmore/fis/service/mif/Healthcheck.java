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
