/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis;

import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.ddmore.fis.configuration.RestClientConfiguration;

/**
 * Parent class for integration tests specifying common integration test settings
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes={ CommonIntegrationTestContextConfiguration.class,RestClientConfiguration.class }, initializers = TestPropertyMockingApplicationContextInitializer.class)
@ActiveProfiles({"test"})
public class IntegrationTestParent {

}
