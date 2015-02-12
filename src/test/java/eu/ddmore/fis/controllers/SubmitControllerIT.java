/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mango.mif.MIFHttpRestClient;
import com.mango.mif.domain.ClientAvailableConnectorDetails;
import com.mango.mif.domain.ExecutionRequest;

import eu.ddmore.fis.CommonIntegrationTestContextConfiguration;
import eu.ddmore.fis.SystemPropertiesAware;
import eu.ddmore.fis.domain.LocalJobStatus;
import eu.ddmore.fis.domain.SubmissionRequest;
import eu.ddmore.fis.domain.SubmissionResponse;
import eu.ddmore.fis.service.CommandRegistryImpl;
import eu.ddmore.fis.service.JobDispatcherImpl;
import eu.ddmore.fis.service.RemoteJobStatusPoller;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CommonIntegrationTestContextConfiguration.class })
public class SubmitControllerIT extends SystemPropertiesAware {

    private static final Logger LOG = Logger.getLogger(SubmitControllerIT.class);

    // This is where the output from FIS and MIF can be found
    private File workingDir = new File("target", "SubmitControllerIT_Test_Working_Dir");

    @Value("${execution.host.fileshare}") // Set to "target/Test_Execution_Host_Fileshare" in tests.properties
    private String executionHostFileshare;
    @Value("${execution.host.fileshare.remote}")
    private String executionHostFileshareRemote;
    
    @Autowired
    private SubmitController submitController;

    @Autowired
    private JobsController jobsController;

    @Value("${commandline.execute.command}")
    private String command;

    @Autowired
    private RemoteJobStatusPoller remoteJobStatusPoller;
    
    private MIFHttpRestClient mockMifClient;

    @Before
    public void setUp() throws IOException {
        FileUtils.deleteDirectory(this.workingDir);
        this.workingDir.mkdir();
        
        final Map<String, ClientAvailableConnectorDetails> allClientAvailConnectorDetails = new HashMap<String, ClientAvailableConnectorDetails>();
        allClientAvailConnectorDetails.put(this.command, new ClientAvailableConnectorDetails());
        
        this.mockMifClient = mock(MIFHttpRestClient.class);
        when(this.mockMifClient.getClientAvailableConnectorDetails()).thenReturn(allClientAvailConnectorDetails);
        when(this.mockMifClient.getClientAvailableConnectorDetails(this.command)).thenReturn(allClientAvailConnectorDetails.get(this.command));
        
        // Set the mock MIFHttpRestClient on all the controllers/services that will use it
        ((CommandRegistryImpl) ((JobDispatcherImpl) this.submitController.getJobDispatcher()).getCommandRegistry()).setMifClient(this.mockMifClient);
        ((JobDispatcherImpl) this.submitController.getJobDispatcher()).setMifClient(this.mockMifClient);
        this.remoteJobStatusPoller.setMifClient(this.mockMifClient);
        
        stub(this.mockMifClient.checkStatus(isA(String.class))).toReturn("FAILED");
    }

    @Test
    public void shouldSubmitRequest() throws IOException, InterruptedException {

        // Copy the files out of the testdata JAR file

        final String MODEL_FILE_NAME = "Warfarin-ODE-latest.ctl";
        final String DATA_FILE_NAME = "warfarin_conc.csv";

        final String testDataDir = "/eu/ddmore/testdata/models/NM-TRAN/7.2.0/Warfarin_ODE/";

        final URL scriptFile = SubmitControllerIT.class.getResource(testDataDir + MODEL_FILE_NAME);
        FileUtils.copyURLToFile(scriptFile, new File(workingDir, MODEL_FILE_NAME));
        final URL dataFile = SubmitControllerIT.class.getResource(testDataDir + DATA_FILE_NAME);
        FileUtils.copyURLToFile(dataFile, new File(workingDir, DATA_FILE_NAME));

        when(this.mockMifClient.executeJob(isA(ExecutionRequest.class))).thenReturn("Submitted");
        
        // Proceed with the test...

        final SubmissionRequest submissionRequest = new SubmissionRequest();
        submissionRequest.setCommand(this.command);
        submissionRequest.setCommandParameters("echo Hello from mock NONMEM via command-line connector of MIF! >dummyoutput.lst\necho.");
        submissionRequest.setExecutionFile(MODEL_FILE_NAME);
        submissionRequest.setWorkingDirectory(this.workingDir.getAbsolutePath());
        
        final SubmissionResponse response = this.submitController.submit(submissionRequest);

        assertNotNull("SubmissionResponse should have been received", response);
        assertNotNull("SubmissionResponse should have a Request ID", response.getRequestID());

        final String jobId = response.getRequestID();
        LOG.debug(String.format("Request ID = Job ID = %s", jobId));
        final File mifWorkingDir = new File(this.executionHostFileshare, jobId);
        
        verify(this.mockMifClient).getClientAvailableConnectorDetails();
    	final ArgumentCaptor<ExecutionRequest> execRequestArgCaptor = ArgumentCaptor.forClass(ExecutionRequest.class);
        verify(this.mockMifClient).executeJob(execRequestArgCaptor.capture());
        
        final ExecutionRequest executionRequest = execRequestArgCaptor.getValue();
        assertEquals("Checking the value of ExecutionRequest.type", this.command, executionRequest.getType());
        assertEquals("Checking the value of ExecutionRequest.executionFile", submissionRequest.getExecutionFile(), executionRequest.getExecutionFile());
        assertEquals("Checking the value of ExecutionRequest.executionParameters", submissionRequest.getCommandParameters(), executionRequest.getExecutionParameters());
        assertEquals("Checking the value of ExecutionRequest.requestAttributes['EXECUTION_HOST_FILESHARE']",
        	this.executionHostFileshare, executionRequest.getRequestAttributes().get("EXECUTION_HOST_FILESHARE"));
        assertEquals("Checking the value of ExecutionRequest.requestAttributes['EXECUTION_HOST_FILESHARE_REMOTE']",
        	this.executionHostFileshareRemote, executionRequest.getRequestAttributes().get("EXECUTION_HOST_FILESHARE_REMOTE"));
        assertEquals("Checking the value of ExecutionRequest.userName", "tel-user", executionRequest.getUserName());
        assertNull("Checking the value of ExecutionRequest.userPassword", executionRequest.getUserPassword());

        // Simulate the Job completing - the RemoteJobStatusPoller will pick this up imminently
        when(this.mockMifClient.checkStatus(jobId)).thenReturn("COMPLETED");
        
        while (isNotCompleted(this.jobsController.getJobStatus(jobId))) {
            LOG.debug(String.format("Waiting for %s job to complete. Current status: %s. Working directory: %s", response.getRequestID(), this.jobsController.getJobStatus(jobId), this.workingDir));
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        }
        
        verify(this.mockMifClient).checkStatus(jobId);
        
        // Check that the publishInputs and retrieveOutputs Groovy scripts were called
        LOG.debug(String.format("Files in working directory: %s", Arrays.toString(this.workingDir.list())));
        assertTrue("Model file should still exist in the original working directory",
        	new File(this.workingDir, MODEL_FILE_NAME).exists());
        assertTrue("Data file should still exist in the original working directory",
        	new File(this.workingDir, DATA_FILE_NAME).exists());
        assertTrue("MIF working directory should have been created", mifWorkingDir.exists());
        LOG.debug(String.format("Files in MIF working directory: %s", Arrays.toString(mifWorkingDir.list())));
        assertTrue("Model file should have been copied into the MIF working directory",
        	new File(mifWorkingDir, MODEL_FILE_NAME).exists());
        assertTrue("Data file should have been copied into the MIF working directory",
        	new File(mifWorkingDir, DATA_FILE_NAME).exists());
        final File fisMetadataDir = new File(this.workingDir,".fis");
        assertTrue("FIS metadata directory should have been created", fisMetadataDir.exists());
        
    }

    private boolean isNotCompleted(final LocalJobStatus jobStatus) {
        return LocalJobStatus.RUNNING.compareTo(jobStatus) >= 0;
    }

}
