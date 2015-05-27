/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Verifies that standalone service fulfils functional requirements
 */
public class ServiceShutdownAT extends SystemPropertiesAware {
    
    @Test
    public void shouldShutDownService() {
        FISHttpRestClient fisClient = new FISHttpRestClient(System.getProperty("fis.url"),System.getProperty("fis.management.url"));
        assertEquals(fisClient.shutdown(),"OK");
    }
    
}
