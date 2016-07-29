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
package eu.ddmore.fis.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Test;

import com.mango.mif.MIFHttpRestClient;
import com.mango.mif.domain.ClientAvailableConnectorDetails;


public class CommandRegistryImplTest {

	private MIFHttpRestClient mockMifClient;

	/**
	 * Set-up tasks prior to each test being run.
	 */
	@Before
	public void setUp() throws Exception {
		this.mockMifClient = mock(MIFHttpRestClient.class);
		
		final ClientAvailableConnectorDetails mifConnectorDetails1 = new ClientAvailableConnectorDetails();
		mifConnectorDetails1.setResultsIncludeRegex(".*\\.out");
		mifConnectorDetails1.setResultsExcludeRegex(".*\\.bat");
		final ClientAvailableConnectorDetails mifConnectorDetails2 = new ClientAvailableConnectorDetails();
		mifConnectorDetails2.setResultsIncludeRegex(".*\\.lst");
        mifConnectorDetails2.setResultsExcludeRegex(".*\\.exe");
		
		final Map<String, ClientAvailableConnectorDetails> allMifConnectorDetails = new HashMap<String, ClientAvailableConnectorDetails>();
		allMifConnectorDetails.put("MYEXECTYPE1", mifConnectorDetails1);
		allMifConnectorDetails.put("MYEXECTYPE2", mifConnectorDetails2);
		when(this.mockMifClient.getClientAvailableConnectorDetails()).thenReturn(allMifConnectorDetails);
	}

	@Test
	public void testResolveClientAvailableConnectorDetails() throws JsonGenerationException, JsonMappingException, IOException {
		final CommandRegistryImpl commandRegistry = new CommandRegistryImpl();
		commandRegistry.setMifClient(this.mockMifClient);
		
		final ClientAvailableConnectorDetails clientAvailableConnectorDetails
			= commandRegistry.resolveClientAvailableConnectorDetailsFor("MYEXECTYPE2");
		assertEquals("Checking the correct ClientAvailableConnectorDetails was returned",
			".*\\.lst", clientAvailableConnectorDetails.getResultsIncludeRegex());
        assertEquals("Checking the correct ClientAvailableConnectorDetails was returned",
            ".*\\.exe", clientAvailableConnectorDetails.getResultsExcludeRegex());
	}
	
	@Test(expected=NoSuchElementException.class)
	public void testResolveClientAvailableConnectorDetailsWhereExecutionTypeNotExists() throws JsonGenerationException, JsonMappingException, IOException {
		final CommandRegistryImpl commandRegistry = new CommandRegistryImpl();
		commandRegistry.setMifClient(this.mockMifClient);
		
		commandRegistry.resolveClientAvailableConnectorDetailsFor("MYEXECTYPE3");
	}

}
