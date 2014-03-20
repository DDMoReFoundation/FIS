/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.domain.ExecutionRequest;

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.service.JobDispatcherImpl;


@RunWith(MockitoJUnitRunner.class)
public class JobDispatcherImplTest {

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();
    
    @InjectMocks
	JobDispatcherImpl jobDispatcher = new JobDispatcherImpl();
    
	@Test
	public void shouldBuildExecutionRequest() {
	    CommandExecutionTarget executionTarget = mock(CommandExecutionTarget.class);
        when(executionTarget.getCommand()).thenReturn("COMMAND");
        when(executionTarget.getConverterToolboxPath()).thenReturn("CONVERTER_TOOLBOX_PATH");
        when(executionTarget.getEnvironmentSetupScript()).thenReturn("ENVIRONMENT_SETUP_SCRIPT");
        when(executionTarget.getExecutionType()).thenReturn("EXECUTION_TYPE");
        when(executionTarget.getToolExecutablePath()).thenReturn("EXECUTABLE_PATH");
	    
	    
	    LocalJob job = mock(LocalJob.class);
	    when(job.getCommand()).thenReturn("COMMAND");
	    when(job.getId()).thenReturn("1234");
	    when(job.getControlFile()).thenReturn("CONTROL_FILE");
	    when(job.getWorkingDirectory()).thenReturn("WORKING_DIR");
        when(job.getCommandParameters()).thenReturn("COMMAND_PARAMETERS");
	    
	    ExecutionRequest executionRequest = jobDispatcher.buildExecutionRequest(job,executionTarget);
	    
	    assertEquals("1234",executionRequest.getRequestId());
	    assertEquals("EXECUTABLE_PATH",executionRequest.getCommand());
	    assertEquals("CONTROL_FILE",executionRequest.getExecutionFile());
	    assertEquals("FIS Service Job",executionRequest.getName());
	    assertEquals("EXECUTION_TYPE",executionRequest.getType());
	    assertEquals("WORKING_DIR",executionRequest.getRequestAttributes().get(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName()));
        assertEquals("CONVERTER_TOOLBOX_PATH",executionRequest.getRequestAttributes().get("CONVERTER_TOOLBOX_PATH"));
        assertEquals("ENVIRONMENT_SETUP_SCRIPT",executionRequest.getSubmitHostPreamble());
        assertEquals("tel-user",executionRequest.getUserName());
        assertEquals("COMMAND_PARAMETERS",executionRequest.getExecutionParameters());
        assertFalse(executionRequest.getSubmitAsUserMode());
	}

}