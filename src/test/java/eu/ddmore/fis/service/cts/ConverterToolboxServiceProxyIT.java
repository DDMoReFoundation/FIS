/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.cts;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.ddmore.fis.CommonIntegrationTestContextConfiguration;
import eu.ddmore.fis.SystemPropertiesAware;


/**
 * Integration test for {@link ConverterToolboxServiceProxy}
 * 
 * This test only verifies that ConverterToolboxServiceProxy bean is correctly configured
 * and available in SpringContext, it does not (and it never should) perform any submissions
 * of conversion tasks nor try to mock integration with remote CTS. The reason for this is that:
 * * CTS is a remote service and IT should not depend on remote services
 * * mocking REST calls is already tested by {@link ConverterToolboxServiceProxyTest} 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CommonIntegrationTestContextConfiguration.class })
public class ConverterToolboxServiceProxyIT extends SystemPropertiesAware {

    @Autowired
    private ConverterToolboxService converterToolboxService;
    
    @Test
    public void converterToolboxServiceShouldBeAvailable() {
        assertNotNull("Converter toolbox service bean should be available",converterToolboxService);
    }
}
