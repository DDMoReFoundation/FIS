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

import eu.ddmore.fis.service.cts.SimpleHealth;

/**
 * Verifies that standalone service fulfils functional requirements
 */
public class ServiceHealthcheckAT extends SystemPropertiesAware {
    
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
