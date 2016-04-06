/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.mango.mif.MIFHttpRestClient;
import com.mango.mif.domain.ClientAvailableConnectorDetails;
import com.mango.mif.domain.ExecutionRequest;

import eu.ddmore.fis.IntegrationTestParent;
import eu.ddmore.fis.configuration.Fileshare;
import eu.ddmore.fis.controllers.ClientError.JobNotFound;
import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.LocalJobStatus;
import eu.ddmore.fis.service.CommandRegistryImpl;
import eu.ddmore.fis.service.JobDispatcherImpl;
import eu.ddmore.fis.service.RemoteJobStatusPoller;


/**
 * Integration test for submission endpoints of the {@link JobsController}.
 */
@WebAppConfiguration
@IntegrationTest({"server.port=0", "management.port=0"}) //let the framework choose the port
public class JobsControllerSubmissionIT extends IntegrationTestParent {
    private static final Logger LOG = Logger.getLogger(JobsControllerSubmissionIT.class);
    private static final String URL = "http://localhost";
    
    @Autowired
    private EmbeddedWebApplicationContext server;

    @Autowired
    private RestTemplate restTemplate;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    // This is where the output from FIS and MIF can be found
    private File workingDir;

    @Autowired
    private Fileshare fileshare;
    
    @Autowired
    private JobsController jobsController;

    private String command = "mock-command";

    @Autowired
    private RemoteJobStatusPoller remoteJobStatusPoller;
    
    @Autowired
    private MIFHttpRestClient mockMifClient;

    @Before
    public void setUp() throws IOException {

        this.workingDir = new File(this.temporaryFolder.getRoot(), "SubmitControllerIT_Test_Working_Dir");
        this.workingDir.mkdir();
        
        final Map<String, ClientAvailableConnectorDetails> allClientAvailConnectorDetails = new HashMap<String, ClientAvailableConnectorDetails>();
        allClientAvailConnectorDetails.put(this.command, new ClientAvailableConnectorDetails());
        
        when(this.mockMifClient.getClientAvailableConnectorDetails()).thenReturn(allClientAvailConnectorDetails);
        when(this.mockMifClient.getClientAvailableConnectorDetails(this.command)).thenReturn(allClientAvailConnectorDetails.get(this.command));
        
        // Set the mock MIFHttpRestClient on all the controllers/services that will use it
        ((CommandRegistryImpl) ((JobDispatcherImpl) this.jobsController.getJobDispatcher()).getCommandRegistry()).setMifClient(this.mockMifClient);
        ((JobDispatcherImpl) this.jobsController.getJobDispatcher()).setMifClient(this.mockMifClient);
        this.remoteJobStatusPoller.setMifClient(this.mockMifClient);
        
        stub(this.mockMifClient.checkStatus(isA(String.class))).toReturn("FAILED");
    }

    @Test
    public void shouldSubmitRequest() throws IOException, InterruptedException, JobNotFound {

        // Copy the files out of the testdata JAR file

        final String MODEL_FILE_NAME = "Warfarin-ODE-latest.ctl";
        final String DATA_FILE_NAME = "warfarin_conc.csv";

        final String testDataDir = "/test-models/NM-TRAN/7.2.0/Warfarin_ODE/";

        final URL scriptFile = JobsControllerSubmissionIT.class.getResource(testDataDir + MODEL_FILE_NAME);
        FileUtils.copyURLToFile(scriptFile, new File(workingDir, MODEL_FILE_NAME));
        final URL dataFile = JobsControllerSubmissionIT.class.getResource(testDataDir + DATA_FILE_NAME);
        FileUtils.copyURLToFile(dataFile, new File(workingDir, DATA_FILE_NAME));
        
        // Put an extraInputFile into a subdirectory
        final String EXTRA_INPUT_FILE_RELPATH = "subdir/FileInSubdir.txt";
        FileUtils.write(new File(workingDir, EXTRA_INPUT_FILE_RELPATH), "blah blah blah\n");

        when(this.mockMifClient.executeJob(isA(ExecutionRequest.class))).thenReturn("Submitted");
        
        // Proceed with the test...

        final LocalJob job = new LocalJob();
        job.setExecutionType(this.command);
        job.setCommandParameters("echo Hello from mock NONMEM via command-line connector of MIF! >dummyoutput.lst\necho.");
        job.setExecutionFile(MODEL_FILE_NAME);
        job.setExtraInputFiles(Arrays.asList(EXTRA_INPUT_FILE_RELPATH));
        job.setWorkingDirectory(this.workingDir.getAbsolutePath());
        
        final LocalJob submittedJob = this.jobsController.submit(job);

        assertNotNull("Job instance should have been received", submittedJob);
        assertNotNull("submitted Job should have a Job ID", submittedJob.getId());

        final String jobId = submittedJob.getId();
        LOG.debug(String.format("Job ID = %s", jobId));
        final File mifWorkingDir = new File(this.fileshare.getFisHostPath(), jobId);
        
        verify(this.mockMifClient).getClientAvailableConnectorDetails();
    	final ArgumentCaptor<ExecutionRequest> execRequestArgCaptor = ArgumentCaptor.forClass(ExecutionRequest.class);
        verify(this.mockMifClient).executeJob(execRequestArgCaptor.capture());
        
        final ExecutionRequest executionRequest = execRequestArgCaptor.getValue();
        assertEquals("Checking the value of ExecutionRequest.type", this.command, executionRequest.getType());
        assertEquals("Checking the value of ExecutionRequest.executionFile", job.getExecutionFile(), executionRequest.getExecutionFile());
        assertEquals("Checking the value of ExecutionRequest.executionParameters", job.getCommandParameters(), executionRequest.getExecutionParameters());
        assertEquals("Checking the value of ExecutionRequest.requestAttributes['EXECUTION_HOST_FILESHARE']",
        	this.fileshare.getMifHostPath(), executionRequest.getRequestAttributes().get("EXECUTION_HOST_FILESHARE"));
        assertEquals("Checking the value of ExecutionRequest.requestAttributes['EXECUTION_HOST_FILESHARE_REMOTE']",
            this.fileshare.getExecutionHostPath(), executionRequest.getRequestAttributes().get("EXECUTION_HOST_FILESHARE_REMOTE"));
        assertEquals("Checking the value of ExecutionRequest.userName", "fis", executionRequest.getUserName());
        assertTrue("The value of the ExecutionRequest.userPassword should be empty", StringUtils.isEmpty(executionRequest.getUserPassword()));

        // Simulate the Job completing - the RemoteJobStatusPoller will pick this up imminently
        when(this.mockMifClient.checkStatus(jobId)).thenReturn("COMPLETED");
        
        while (isNotCompleted(this.jobsController.getJobStatus(jobId).getBody())) {
            LOG.debug(String.format("Waiting for %s job to complete. Current status: %s. Working directory: %s", job.getId(), this.jobsController.getJobStatus(jobId), this.workingDir));
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
        assertTrue("Extra input file, in a subdirectory, should have been copied into the MIF working directory",
            new File(mifWorkingDir, EXTRA_INPUT_FILE_RELPATH).exists());
        final File fisMetadataDir = new File(this.workingDir,".fis");
        assertTrue("FIS metadata directory should have been created", fisMetadataDir.exists());
        assertFalse("Archive's manifest.xml should NOT have been copied into the MIF working directory",
            new File(mifWorkingDir, "manifest.xml").exists());
        assertFalse("Archive's metadata.rdf should NOT have been copied into the MIF working directory",
            new File(mifWorkingDir, "metadata.rdf").exists());
        
    }
    
    @Test
    public void shouldRejectSubmittedJobIfIdSet() {
        final LocalJob job = new LocalJob();
        job.setExecutionType(this.command);
        job.setCommandParameters("MOCK PARAMETERS");
        job.setExecutionFile("MOCK");
        job.setWorkingDirectory(this.workingDir.getAbsolutePath());
        job.setId("MOCK_ID");
        try {
            restTemplate.postForEntity(generateEndpoint("/jobs"), job, LocalJob.class);
        } catch(HttpClientErrorException ex) {
            assertEquals(HttpStatus.BAD_REQUEST,ex.getStatusCode());
        }
    }

    @Test
    public void shouldRejectSubmittedJobIfSubmissionTimeSet() {
        final LocalJob job = new LocalJob();
        job.setExecutionType(this.command);
        job.setCommandParameters("MOCK PARAMETERS");
        job.setExecutionFile("MOCK");
        job.setWorkingDirectory(this.workingDir.getAbsolutePath());
        job.setSubmitTime("MOCK_TIME");
        try {
            restTemplate.postForEntity(generateEndpoint("/jobs"), job, LocalJob.class);
        } catch(HttpClientErrorException ex) {
            assertEquals(HttpStatus.BAD_REQUEST,ex.getStatusCode());
        }
    }
    
    @Test
    public void shouldRejectSubmittedJobIfVersionNonZero() {
        final LocalJob job = new LocalJob();
        job.setExecutionType(this.command);
        job.setCommandParameters("MOCK PARAMETERS");
        job.setExecutionFile("MOCK");
        job.setWorkingDirectory(this.workingDir.getAbsolutePath());
        job.setVersion(1);
        try {
            restTemplate.postForEntity(generateEndpoint("/jobs"), job, LocalJob.class);
        } catch(HttpClientErrorException ex) {
            assertEquals(HttpStatus.BAD_REQUEST,ex.getStatusCode());
        }
    }
    
    @Test
    public void shouldRejectSubmittedJobIfStatusNotNull() {
        final LocalJob job = new LocalJob();
        job.setExecutionType(this.command);
        job.setCommandParameters("MOCK PARAMETERS");
        job.setExecutionFile("MOCK");
        job.setWorkingDirectory(this.workingDir.getAbsolutePath());
        job.setStatus(LocalJobStatus.NEW);
        try {
            restTemplate.postForEntity(generateEndpoint("/jobs"), job, LocalJob.class);
        } catch(HttpClientErrorException ex) {
            assertEquals(HttpStatus.BAD_REQUEST,ex.getStatusCode());
        }
    }
    
    private boolean isNotCompleted(final LocalJobStatus jobStatus) {
        return LocalJobStatus.RUNNING.compareTo(jobStatus) >= 0;
    }

    private String generateEndpoint(String path) {
        return URL+":"+server.getEmbeddedServletContainer().getPort()+path;
    }
}
