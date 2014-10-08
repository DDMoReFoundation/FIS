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
		mifConnectorDetails1.setOutputFilenamesRegex(".*\\.out");
		final ClientAvailableConnectorDetails mifConnectorDetails2 = new ClientAvailableConnectorDetails();
		mifConnectorDetails2.setOutputFilenamesRegex(".*\\.lst");
		
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
			".*\\.lst", clientAvailableConnectorDetails.getOutputFilenamesRegex());
	}
	
	@Test(expected=NoSuchElementException.class)
	public void testResolveClientAvailableConnectorDetailsWhereExecutionTypeNotExists() throws JsonGenerationException, JsonMappingException, IOException {
		final CommandRegistryImpl commandRegistry = new CommandRegistryImpl();
		commandRegistry.setMifClient(this.mockMifClient);
		
		commandRegistry.resolveClientAvailableConnectorDetailsFor("MYEXECTYPE3");
	}

}
