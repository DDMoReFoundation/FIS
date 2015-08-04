/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.mango.mif.MIFHttpRestClient;
import com.mango.mif.domain.ClientAvailableConnectorDetails;
import com.mango.mif.domain.ExecutionRequest;

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.LocalJobStatus;


@RunWith(MockitoJUnitRunner.class)
public class JobDispatcherImplTest {

    @Rule
    public TemporaryFolder testDirectory = new TemporaryFolder();

    @Mock
    private JobResourceProcessor jobResourcePublisher;
    @Mock
    private CommandRegistry commandRegistry;
    @Mock
    private MIFHttpRestClient mifClient;
    
    private File testWorkingDir;
    private String testExecutionHostFileshare;
    private String dummyExecutionHostFileshareRemote = "V:\\mifshare";

    @InjectMocks
    private JobDispatcherImpl jobDispatcher = new JobDispatcherImpl();
    
    @Before
    public void setUp() {
    	this.testWorkingDir = this.testDirectory.getRoot();
    	this.testExecutionHostFileshare = this.testWorkingDir.toString();
    	this.jobDispatcher.setExecutionHostFileshare(this.testExecutionHostFileshare);
    	this.jobDispatcher.setExecutionHostFileshareRemote(this.dummyExecutionHostFileshareRemote);
    }
    
    @Test
    public void shouldDispatch_SubmitAsUserFalse() {
    
        final ExecutionRequest capturedExecRequest = setupJobAndDispatchItToMockMIF();
        
        // Check parameters on the MIF Client Execution Request
        assertEquals("Checking the request ID on the MIF Client Execution Request",
            "1234", capturedExecRequest.getRequestId());
        assertEquals("Checking the name on the MIF Client Execution Request",
            "FIS Service Job", capturedExecRequest.getName());
        assertEquals("Checking the execution type on the MIF Client Execution Request",
            "CMDLINE", capturedExecRequest.getType());
        assertEquals("Checking the execution file on the MIF Client Execution Request",
            "CONTROL_FILE", capturedExecRequest.getExecutionFile());
        assertEquals("Checking the command parameters on the MIF Client Execution Request",
            "COMMAND_PARAMETERS", capturedExecRequest.getExecutionParameters());
        assertFalse("Checking the submit-as-user mode on the MIF Client Execution Request",
            capturedExecRequest.getSubmitAsUserMode());
        assertEquals("Checking the user name on the MIF Client Execution Request",
            JobDispatcherImpl.DEFAULT_MIF_USERNAME, capturedExecRequest.getUserName());
        assertNull("Passing the submit host preamble on the MIF Client Execution Request is deprecated so should NOT be set",
            capturedExecRequest.getSubmitHostPreamble());
        assertNull("The grid host preamble on the MIF Client Execution Request should NOT be set",
            capturedExecRequest.getGridHostPreamble());
        final Map<String, String> execRequestAttrs = capturedExecRequest.getRequestAttributes();
        assertEquals("Checking the \"execution host fileshare\" Execution Request Parameter on the MIF Client Execution Request",
            this.testExecutionHostFileshare, execRequestAttrs.get("EXECUTION_HOST_FILESHARE"));
        assertEquals("Checking the \"execution host fileshare remote\" Execution Request Parameter on the MIF Client Execution Request",
            this.dummyExecutionHostFileshareRemote, execRequestAttrs.get("EXECUTION_HOST_FILESHARE_REMOTE"));
        
    }
    
    @Test
    public void shouldDispatch_SubmitAsUserTrue() {
    
        final String mifUserName = "anotherUser";
        final String mifUserPassword = "encryptedPassword";
        this.jobDispatcher.setMifUserName(mifUserName);
        this.jobDispatcher.setMifUserPassword(mifUserPassword);
    
        final ExecutionRequest capturedExecRequest = setupJobAndDispatchItToMockMIF();
        
        // Check parameters on the MIF Client Execution Request
        assertEquals("Checking the request ID on the MIF Client Execution Request",
            "1234", capturedExecRequest.getRequestId());
        assertEquals("Checking the name on the MIF Client Execution Request",
            "FIS Service Job", capturedExecRequest.getName());
        assertEquals("Checking the execution type on the MIF Client Execution Request",
            "CMDLINE", capturedExecRequest.getType());
        assertEquals("Checking the execution file on the MIF Client Execution Request",
            "CONTROL_FILE", capturedExecRequest.getExecutionFile());
        assertEquals("Checking the command parameters on the MIF Client Execution Request",
            "COMMAND_PARAMETERS", capturedExecRequest.getExecutionParameters());
        assertTrue("Checking the submit-as-user mode on the MIF Client Execution Request",
            capturedExecRequest.getSubmitAsUserMode());
        assertEquals("Checking the user name on the MIF Client Execution Request",
            mifUserName, capturedExecRequest.getUserName());
        assertEquals("Checking the user password on the MIF Client Execution Request",
            mifUserPassword, capturedExecRequest.getUserPassword());
        assertNull("Passing the submit host preamble on the MIF Client Execution Request is deprecated so should NOT be set",
            capturedExecRequest.getSubmitHostPreamble());
        assertNull("The grid host preamble on the MIF Client Execution Request should NOT be set",
            capturedExecRequest.getGridHostPreamble());
        final Map<String, String> execRequestAttrs = capturedExecRequest.getRequestAttributes();
        assertEquals("Checking the \"execution host fileshare\" Execution Request Parameter on the MIF Client Execution Request",
            this.testExecutionHostFileshare, execRequestAttrs.get("EXECUTION_HOST_FILESHARE"));
        assertEquals("Checking the \"execution host fileshare remote\" Execution Request Parameter on the MIF Client Execution Request",
            this.dummyExecutionHostFileshareRemote, execRequestAttrs.get("EXECUTION_HOST_FILESHARE_REMOTE"));
        
    }
    
    /**
     * Create and populate a {@link LocalJob} and dispatch it to the mocked-out MIF.
     * <p>
     * @return {@link ExecutionRequest} captured from the call to {@link MIFHttpRestClient.executeJob()}.
     */
    private ExecutionRequest setupJobAndDispatchItToMockMIF() {
    
        final LocalJob localJob = new LocalJob();
        localJob.setExecutionType("CMDLINE");
        localJob.setId("1234");
        localJob.setExecutionFile("CONTROL_FILE");
        localJob.setWorkingDirectory("WORKING_DIR");
        localJob.setCommandParameters("COMMAND_PARAMETERS");
        localJob.setExtraInputFiles(Arrays.asList("EXTRA_INPUT_FILE_1", "EXTRA_INPUT_FILE_2"));
        localJob.setStatus(LocalJobStatus.NEW);

        final ClientAvailableConnectorDetails connectorDetails = new ClientAvailableConnectorDetails();
        connectorDetails.setResultsIncludeRegex(".*\\.(csv|ctl|xml|lst|pharmml|fit)$");
        connectorDetails.setResultsExcludeRegex(".*\\.(exe)$");

        when(this.jobResourcePublisher.process(localJob)).thenReturn(localJob);
        when(this.commandRegistry.resolveClientAvailableConnectorDetailsFor("CMDLINE")).thenReturn(connectorDetails);

        // Call the method under test
        final LocalJob publishedLocalJob = this.jobDispatcher.dispatch(localJob);

        // Check updates to the published Local Job
        assertEquals("Output filenames regex should be populated on the LocalJob",
            ".*\\.(csv|ctl|xml|lst|pharmml|fit)$", publishedLocalJob.getResultsIncludeRegex());
        assertEquals("Output filenames regex should be populated on the LocalJob",
            ".*\\.(exe)$", publishedLocalJob.getResultsExcludeRegex());

        // Check calls to dependencies
        verify(this.jobResourcePublisher).process(localJob);
        verify(this.commandRegistry).resolveClientAvailableConnectorDetailsFor("CMDLINE");
        final ArgumentCaptor<ExecutionRequest> mifClientExecArgCaptor = ArgumentCaptor.forClass(ExecutionRequest.class);
        verify(this.mifClient).executeJob(mifClientExecArgCaptor.capture());
        assertNotNull("ExecutionRequest should be created and passed to MIF Client Execute Job call", mifClientExecArgCaptor.getValue());
        
        return mifClientExecArgCaptor.getValue();
    }

}
