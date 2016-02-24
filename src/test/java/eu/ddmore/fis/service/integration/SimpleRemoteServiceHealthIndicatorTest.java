/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.integration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doThrow;
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

import eu.ddmore.fis.service.HealthDetail;
import eu.ddmore.fis.service.integration.SimpleHealth;
import eu.ddmore.fis.service.integration.SimpleRemoteServiceHealthIndicator;


/**
 * Tests {@link SimpleRemoteServiceHealthIndicator}
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleRemoteServiceHealthIndicatorTest {
    
    private
    @Mock RestTemplate restTemplate;
    
    @Test(expected=NullPointerException.class)
    public void constructor_shouldThrowNullPointExceptionIfRestTemplateNull() {
        new SimpleRemoteServiceHealthIndicator(null, "mock-url", "mock-health");
    }

    @Test(expected=IllegalArgumentException.class)
    public void constructor_shouldThrowIllegalArgumentExceptionIfRemoteService_URLIsBlank() {
        new SimpleRemoteServiceHealthIndicator(restTemplate, "", "mock-health");
    }

    @Test(expected=IllegalArgumentException.class)
    public void constructor_shouldThrowIllegalArgumentExceptionIfRemoteServiceHealthEndpointIsBlank() {
        new SimpleRemoteServiceHealthIndicator(restTemplate, "mock-url", "");
    }
    
    @Test
    public void health_shouldReturn_UP_HealthIfRemoteServiceisUP() {
        SimpleRemoteServiceHealthIndicator check = new SimpleRemoteServiceHealthIndicator(restTemplate, "mock-url", "mock-health");
        SimpleHealth healthMock = new SimpleHealth();
        healthMock.setStatus(Status.UP.getCode());
        ResponseEntity<SimpleHealth> healthResponse = new ResponseEntity<SimpleHealth>(healthMock,HttpStatus.OK);
        when(restTemplate.getForEntity(eq("mock-url/mock-health"), same(SimpleHealth.class))).thenReturn(healthResponse);
        Health health = check.health();
        assertEquals("Health status should be 'UP'", Status.UP,health.getStatus());
    }
    
    @Test
    public void health_shouldReturn_DOWN_ifRemoteServiceisDOWN() {
        SimpleRemoteServiceHealthIndicator check = new SimpleRemoteServiceHealthIndicator(restTemplate, "mock-url", "mock-health");
        SimpleHealth healthMock = new SimpleHealth();
        healthMock.setStatus(Status.DOWN.getCode());
        ResponseEntity<SimpleHealth> healthResponse = new ResponseEntity<SimpleHealth>(healthMock,HttpStatus.OK);
        when(restTemplate.getForEntity(eq("mock-url/mock-health"), same(SimpleHealth.class))).thenReturn(healthResponse);
        Health health = check.health();
        verifyServiceDown(health);
    }

	@Test
    public void health_shouldReturn_DOWN_IfRemoteServiceSuffersFromInternalError() {
        SimpleRemoteServiceHealthIndicator check = new SimpleRemoteServiceHealthIndicator(restTemplate, "mock-url", "mock-health");
        ResponseEntity<SimpleHealth> healthResponse = new ResponseEntity<SimpleHealth>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.getForEntity(eq("mock-url/mock-health"), same(SimpleHealth.class))).thenReturn(healthResponse);
        Health health = check.health();
        verifyServiceDown(health);
    }

    @Test
    public void health_shouldReturn_DOWN_InCaseOfAnyError() {
        SimpleRemoteServiceHealthIndicator check = new SimpleRemoteServiceHealthIndicator(restTemplate, "mock-url", "mock-health");
        doThrow(RuntimeException.class).when(restTemplate).getForEntity(eq("mock-url/mock-health"), same(SimpleHealth.class));
        Health health = check.health();
        verifyServiceDown(health);
    }
    
    private void verifyServiceDown(Health health) {
        assertEquals("Health status should be 'DOWN'", Status.DOWN,health.getStatus());
        assertEquals("Health object should contain error message", "Remote service is down.", health.getDetails().get(HealthDetail.ERROR));
        assertEquals("Health object should contain remote service URL", "mock-url", health.getDetails().get(HealthDetail.URL));
	}
}
