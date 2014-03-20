package eu.ddmore.fis.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import org.junit.Test;

import com.google.common.collect.Sets;


public class CommandRegistryImplTest {


    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForEmptyCommand() {
        CommandExecutionTarget executionTarget = createMockExecutionTarget();

        CommandRegistryImpl registry = new CommandRegistryImpl(Sets.newHashSet(executionTarget));
        registry.resolveExecutionTargetFor("");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNullCommand() {
        CommandExecutionTarget executionTarget = createMockExecutionTarget();

        CommandRegistryImpl registry = new CommandRegistryImpl(Sets.newHashSet(executionTarget));
        registry.resolveExecutionTargetFor(null);
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowExceptionIfNoExecutionTargetWasFound() {
        CommandExecutionTarget executionTarget = createMockExecutionTarget();

        CommandRegistryImpl registry = new CommandRegistryImpl(Sets.newHashSet(executionTarget));
        registry.resolveExecutionTargetFor("COMMAND2");
    }

    @Test
    public void shouldReturnExecutionTargetForGivenCommand() {
        CommandExecutionTarget executionTarget = createMockExecutionTarget();

        CommandRegistryImpl registry = new CommandRegistryImpl(Sets.newHashSet(executionTarget));
        
        assertTrue(registry.resolveExecutionTargetFor("COMMAND")==executionTarget);
    }
    
    @Test(expected=IllegalStateException.class)
    public void shouldThrowExceptionIfMoreThanOneExecutionTargetWasFoundForGivenCommand() {
        CommandExecutionTarget executionTarget = createMockExecutionTarget();

        CommandExecutionTarget executionTarget2 = mock(CommandExecutionTarget.class);
        when(executionTarget2.getCommand()).thenReturn("COMMAND");
        when(executionTarget2.getConverterToolboxPath()).thenReturn("CONVERTER_TOOLBOX_PATH2");
        when(executionTarget2.getEnvironmentSetupScript()).thenReturn("ENVIRONMENT_SETUP_SCRIPT2");
        when(executionTarget2.getExecutionType()).thenReturn("EXECUTION_TYPE");
        when(executionTarget2.getToolExecutablePath()).thenReturn("EXECUTABLE_PATH");
        
        CommandRegistryImpl registry = new CommandRegistryImpl(Sets.newHashSet(executionTarget, executionTarget2));
        
        registry.resolveExecutionTargetFor("COMMAND");
    }


    private CommandExecutionTarget createMockExecutionTarget() {
        CommandExecutionTarget executionTarget = mock(CommandExecutionTarget.class);
        when(executionTarget.getCommand()).thenReturn("COMMAND");
        when(executionTarget.getConverterToolboxPath()).thenReturn("CONVERTER_TOOLBOX_PATH");
        when(executionTarget.getEnvironmentSetupScript()).thenReturn("ENVIRONMENT_SETUP_SCRIPT");
        when(executionTarget.getExecutionType()).thenReturn("EXECUTION_TYPE");
        when(executionTarget.getToolExecutablePath()).thenReturn("EXECUTABLE_PATH");
        return executionTarget;
    }

}
