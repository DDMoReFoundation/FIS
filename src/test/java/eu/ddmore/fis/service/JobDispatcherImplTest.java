/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.mango.mif.MIFHttpRestClient;
import com.mango.mif.domain.ExecutionRequest;

import eu.ddmore.fis.controllers.JobResourceProcessor;
import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.LocalJobStatus;


@RunWith(MockitoJUnitRunner.class)
public class JobDispatcherImplTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    private JobResourceProcessor jobResourcePublisher;
    @Mock
    private CommandRegistry commandRegistry;
    @Mock
    private MIFHttpRestClient mifClient;

    @InjectMocks
    private JobDispatcherImpl jobDispatcher = new JobDispatcherImpl();

    @Test
    public void shouldDispatch() {

        final LocalJob localJob = new LocalJob();
        localJob.setCommand("COMMAND");
        localJob.setId("1234");
        localJob.setControlFile("CONTROL_FILE");
        localJob.setWorkingDirectory("WORKING_DIR");
        localJob.setCommandParameters("COMMAND_PARAMETERS");
        localJob.setStatus(LocalJobStatus.NEW);

        final CommandExecutionTarget executionTarget = new CommandExecutionTarget();
        executionTarget.setCommand("COMMAND");
        executionTarget.setExecutionType("CMDLINE");
        executionTarget.setToolExecutablePath("cmd /c");
        executionTarget.setOutputFilenamesRegex(".*\\.(csv|ctl|xml|lst|pharmml|fit)$");
        executionTarget.setEnvironmentSetupScript("ENV-SETUP-SCRIPT");
        executionTarget.setConverterToolboxPath("CONVERTER-TOOLBOX-PATH");

        when(this.jobResourcePublisher.process(localJob)).thenReturn(localJob);
        when(this.commandRegistry.resolveExecutionTargetFor("COMMAND")).thenReturn(executionTarget);

        // Call the method under test
        final LocalJob publishedLocalJob = this.jobDispatcher.dispatch(localJob);

        // Check updates to the published Local Job
        assertEquals("Output filenames regex should be populated on the LocalJob",
            ".*\\.(csv|ctl|xml|lst|pharmml|fit)$", publishedLocalJob.getOutputFilenamesRegex());

        // Check calls to dependencies
        verify(this.jobResourcePublisher).process(localJob);
        verify(this.commandRegistry).resolveExecutionTargetFor("COMMAND");
        final ArgumentCaptor<ExecutionRequest> mifClientExecArgCaptor = ArgumentCaptor.forClass(ExecutionRequest.class);
        verify(this.mifClient).executeJob(mifClientExecArgCaptor.capture());
        assertNotNull("ExecutionRequest should be created and passed to MIF Client Execute Job call", mifClientExecArgCaptor.getValue());

        // Check parameters on the MIF Client Execution Request
        assertEquals("Checking the request ID on the MIF Client Execution Request",
            "1234", mifClientExecArgCaptor.getValue().getRequestId());
        assertEquals("Checking the name on the MIF Client Execution Request",
            "FIS Service Job", mifClientExecArgCaptor.getValue().getName());
        assertEquals("Checking the execution type on the MIF Client Execution Request",
            "CMDLINE", mifClientExecArgCaptor.getValue().getType());
        assertEquals("Checking the execution file on the MIF Client Execution Request",
            "CONTROL_FILE", mifClientExecArgCaptor.getValue().getExecutionFile());
        assertEquals("Checking the command parameters on the MIF Client Execution Request",
            "COMMAND_PARAMETERS", mifClientExecArgCaptor.getValue().getExecutionParameters());
        assertEquals("Checking the user name on the MIF Client Execution Request",
            "tel-user", mifClientExecArgCaptor.getValue().getUserName());
        assertFalse("Checking the submit-as-user mode on the MIF Client Execution Request",
            mifClientExecArgCaptor.getValue().getSubmitAsUserMode());
        assertNull("Passing the submit host preamble on the MIF Client Execution Request is deprecated so should NOT be set",
            mifClientExecArgCaptor.getValue().getSubmitHostPreamble());
        assertNull("The grid host preamble on the MIF Client Execution Request should NOT be set",
            mifClientExecArgCaptor.getValue().getGridHostPreamble());
        final Map<String, String> execRequestAttrs = mifClientExecArgCaptor.getValue().getRequestAttributes();
        assertEquals("Checking the \"execution host fileshare\" Execution Request Parameter on the MIF Client Execution Request",
            "WORKING_DIR", execRequestAttrs.get("EXECUTION_HOST_FILESHARE"));
        assertEquals("Checking the \"execution host fileshare remote\" Execution Request Parameter on the MIF Client Execution Request",
            "WORKING_DIR", execRequestAttrs.get("EXECUTION_HOST_FILESHARE_REMOTE"));

        // Unused? :
        // mifClientExecArgCaptor.getValue().getExecutionMode()
        // mifClientExecArgCaptor.getValue().getUserPassword()

    }

}
