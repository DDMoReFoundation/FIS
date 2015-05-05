/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.cts;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


/**
 * Tests {@link Healthcheck}
 */
@RunWith(MockitoJUnitRunner.class)
public class HealthcheckTest {
    
    private
    @Mock RestTemplate restTemplate;
    
    @Test(expected=NullPointerException.class)
    public void constructor_shouldThrowNullPointExceptionIfRestTemplateNull() {
        new Healthcheck(null, "mock-url", "mock-health");
    }

    @Test(expected=IllegalArgumentException.class)
    public void constructor_shouldThrowIllegalArgumentExceptionIfCTS_URLIsBlank() {
        new Healthcheck(restTemplate, "", "mock-health");
    }

    @Test(expected=IllegalArgumentException.class)
    public void constructor_shouldThrowIllegalArgumentExceptionIfCTSHealthEndpointIsBlank() {
        new Healthcheck(restTemplate, "mock-url", "");
    }
    
    @Test
    public void health_shouldReturn_UP_HealthIfCTSisUP() {
        Healthcheck check = new Healthcheck(restTemplate, "mock-url", "mock-health");
        SimpleHealth healthMock = new SimpleHealth();
        healthMock.setStatus(Status.UP.getCode());
        ResponseEntity<SimpleHealth> healthResponse = new ResponseEntity<SimpleHealth>(healthMock,HttpStatus.OK);
        when(restTemplate.getForEntity(eq("mock-url/mock-health"), same(SimpleHealth.class))).thenReturn(healthResponse);
        Health health = check.health();
        assertEquals("Health status is 'UP'", Status.UP,health.getStatus());
    }
    
    @Test
    public void health_shouldReturn_DOWN_ifCTSisDOWN() {
        Healthcheck check = new Healthcheck(restTemplate, "mock-url", "mock-health");
        SimpleHealth healthMock = new SimpleHealth();
        healthMock.setStatus(Status.DOWN.getCode());
        ResponseEntity<SimpleHealth> healthResponse = new ResponseEntity<SimpleHealth>(healthMock,HttpStatus.OK);
        when(restTemplate.getForEntity(eq("mock-url/mock-health"), same(SimpleHealth.class))).thenReturn(healthResponse);
        Health health = check.health();
        assertEquals("Health status is 'DONW'", Status.DOWN,health.getStatus());
    }
    
    @Test
    public void health_shouldReturn_DOWN_IfCantAccessCTS() {
        Healthcheck check = new Healthcheck(restTemplate, "mock-url", "mock-health");
        ResponseEntity<SimpleHealth> healthResponse = new ResponseEntity<SimpleHealth>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.getForEntity(eq("mock-url/mock-health"), same(SimpleHealth.class))).thenReturn(healthResponse);
        Health health = check.health();
        assertEquals("Health status is 'DONW'", Status.DOWN,health.getStatus());
    }
}
