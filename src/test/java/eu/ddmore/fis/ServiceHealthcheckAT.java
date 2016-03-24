/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;
import org.springframework.boot.actuate.health.Health;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ddmore.fis.service.integration.SimpleHealth;

/**
 * Verifies that standalone service fulfils functional requirements
 */
public class ServiceHealthcheckAT extends AcceptanceTestParent {
    
    @SuppressWarnings("unchecked")
    @Test
    public void shouldHealthcheckService() throws JsonParseException, JsonMappingException, IOException {
        FISHttpRestClient fisClient = new FISHttpRestClient(System.getProperty("fis.url"),System.getProperty("fis.management.url"));
        String response = fisClient.healthcheck();
        ObjectMapper mapper = new ObjectMapper();
        SimpleHealth simpleHealth = mapper.readValue(response, SimpleHealth.class);
        Health health = simpleHealth.asHealth();
        assertEquals("UP",health.getStatus().getCode());
        assertNotNull("Health contains CTS health", health.getDetails().get("ctsHealth"));
        assertEquals("CTS Health is UP", "UP", ((Map<String,Object>)health.getDetails().get("ctsHealth")).get("status"));
        assertNotNull("Health contains MIF health", health.getDetails().get("mifHealth"));
        assertEquals("MIF Health is UP", "UP", ((Map<String,Object>)health.getDetails().get("ctsHealth")).get("status"));
    }
    
}
