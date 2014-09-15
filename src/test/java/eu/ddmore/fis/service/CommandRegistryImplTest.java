package eu.ddmore.fis.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.mango.mif.domain.ClientAvailableConnectorDetails;


public class CommandRegistryImplTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForEmptyCommand() {
        ClientAvailableConnectorDetails connectorDetails = createMockClientAvailableConnectorDetails();

        CommandRegistryImpl registry = new CommandRegistryImpl(Sets.newHashSet(connectorDetails));
        registry.resolveClientAvailableConnectorDetailsFor("");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNullCommand() {
    	ClientAvailableConnectorDetails connectorDetails = createMockClientAvailableConnectorDetails();

        CommandRegistryImpl registry = new CommandRegistryImpl(Sets.newHashSet(connectorDetails));
        registry.resolveClientAvailableConnectorDetailsFor(null);
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowExceptionIfNoExecutionTargetWasFound() {
    	ClientAvailableConnectorDetails connectorDetails = createMockClientAvailableConnectorDetails();

        CommandRegistryImpl registry = new CommandRegistryImpl(Sets.newHashSet(connectorDetails));
        registry.resolveClientAvailableConnectorDetailsFor("COMMAND2");
    }

    @Test
    public void shouldReturnExecutionTargetForGivenCommand() {
    	ClientAvailableConnectorDetails connectorDetails = createMockClientAvailableConnectorDetails();

    	CommandRegistryImpl registry = new CommandRegistryImpl(Sets.newHashSet(connectorDetails));
        
        assertTrue(registry.resolveClientAvailableConnectorDetailsFor("COMMAND")==connectorDetails);
    }
    
    @Test(expected=IllegalStateException.class)
    public void shouldThrowExceptionIfMoreThanOneExecutionTargetWasFoundForGivenCommand() {
    	ClientAvailableConnectorDetails connectorDetails = createMockClientAvailableConnectorDetails();

        ClientAvailableConnectorDetails connectorDetails2 = mock(ClientAvailableConnectorDetails.class);
        when(connectorDetails2.getCommand()).thenReturn("COMMAND");
        when(connectorDetails2.getExecutionType()).thenReturn("EXECUTION_TYPE");
        when(connectorDetails2.getOutputFilenamesRegex()).thenReturn(".*\\.out");
        
        CommandRegistryImpl registry = new CommandRegistryImpl(Sets.newHashSet(connectorDetails, connectorDetails2));
        
        registry.resolveClientAvailableConnectorDetailsFor("COMMAND");
    }

    private ClientAvailableConnectorDetails createMockClientAvailableConnectorDetails() {
    	ClientAvailableConnectorDetails connectorDetails = mock(ClientAvailableConnectorDetails.class);
        when(connectorDetails.getCommand()).thenReturn("COMMAND");
        when(connectorDetails.getExecutionType()).thenReturn("EXECUTION_TYPE");
        when(connectorDetails.getOutputFilenamesRegex()).thenReturn(".*\\.out");
        return connectorDetails;
    }

}
