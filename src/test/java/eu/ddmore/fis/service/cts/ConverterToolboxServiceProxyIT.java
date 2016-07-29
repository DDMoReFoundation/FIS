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
