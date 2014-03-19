/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.miflocal.controllers;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.domain.ExecutionRequest;

import eu.ddmore.miflocal.domain.LocalJob;


@RunWith(MockitoJUnitRunner.class)
public class SubmitControllerTest {

    private static final Logger LOG = Logger.getLogger(SubmitControllerTest.class);
    
    @Rule
    public TemporaryFolder folder= new TemporaryFolder();
    
    @InjectMocks
	SubmitController submitController = new SubmitController();
    
	@Test
	public void shouldBuildExecutionRequest() {
	    
	    submitController.setConverterToolboxPath("CONVERTER_TOOLBOX_PATH");
	    submitController.setEnvironmentSetupScript("ENVIRONMENT_SETUP_SCRIPT");
	    
	    LocalJob job = mock(LocalJob.class);
	    when(job.getCommand()).thenReturn("COMMAND");
	    when(job.getId()).thenReturn("1234");
	    when(job.getControlFile()).thenReturn("CONTROL_FILE");
	    when(job.getWorkingDirectory()).thenReturn("WORKING_DIR");
	    
	    ExecutionRequest executionRequest = submitController.buildExecutionRequest(job,"DESCRIPTION","EXECUTION_TYPE");
	    
	    assertEquals("1234",executionRequest.getRequestId());
	    assertEquals("COMMAND",executionRequest.getCommand());
	    assertEquals("CONTROL_FILE",executionRequest.getExecutionFile());
	    assertEquals("DESCRIPTION",executionRequest.getName());
	    assertEquals("EXECUTION_TYPE",executionRequest.getType());
	    assertEquals("WORKING_DIR",executionRequest.getRequestAttributes().get(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName()));
        assertEquals("CONVERTER_TOOLBOX_PATH",executionRequest.getRequestAttributes().get("CONVERTER_TOOLBOX_PATH"));
        assertEquals("ENVIRONMENT_SETUP_SCRIPT",executionRequest.getSubmitHostPreamble());
        assertEquals("tel-user",executionRequest.getUserName());
        assertFalse(executionRequest.getSubmitAsUserMode());
	}

}