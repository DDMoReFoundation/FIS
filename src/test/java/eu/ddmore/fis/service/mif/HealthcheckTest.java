/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.mif;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import com.mango.mif.MIFHttpRestClient;

import eu.ddmore.fis.service.HealthDetail;


/**
 * Tests {@link Healthcheck}
 */
@RunWith(MockitoJUnitRunner.class)
public class HealthcheckTest {
    
    private
    @Mock MIFHttpRestClient restClient;
    
    @Test(expected=NullPointerException.class)
    public void constructor_shouldThrowNullPointExceptionIfRestTemplateNull() {
        new Healthcheck(null, "mock-url");
    }

    @Test(expected=IllegalArgumentException.class)
    public void constructor_shouldThrowIllegalArgumentExceptionIfCTS_URLIsBlank() {
        new Healthcheck(restClient, "");
    }
    
    @Test
    public void health_shouldReturn_UP_HealthIfCTSisUP() {
        Healthcheck check = new Healthcheck(restClient, "mock-url");
        when(restClient.healthcheck()).thenReturn(true);
        Health health = check.health();
        assertEquals("Health status is 'UP'", Status.UP,health.getStatus());
    }
    
    @Test
    public void health_shouldReturn_DOWN_ifCTSisDOWN() {
        Healthcheck check = new Healthcheck(restClient, "mock-url");
        when(restClient.healthcheck()).thenReturn(false);
        Health health = check.health();
        assertEquals("Health status is 'DOWN'", Status.DOWN,health.getStatus());
        assertEquals("And the health object contains Error message", "MIF is not running", health.getDetails().get(HealthDetail.ERROR));
        assertEquals("And the health object contains MIF URL", "mock-url", health.getDetails().get(HealthDetail.URL));
    }

    @Test
    public void health_shouldReturn_DOWN_InCaseOfAnyError() {
        Healthcheck check = new Healthcheck(restClient, "mock-url");
        doThrow(RuntimeException.class).when(restClient).healthcheck();
        Health health = check.health();
        assertEquals("Health status is 'DOWN'", Status.DOWN,health.getStatus());
        assertEquals("And the health object contains Error message", "MIF is not running", health.getDetails().get(HealthDetail.ERROR));
        assertEquals("And the health object contains MIF URL", "mock-url", health.getDetails().get(HealthDetail.URL));
    }
}
