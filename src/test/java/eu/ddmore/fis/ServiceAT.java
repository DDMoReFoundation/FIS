/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Preconditions;

import eu.ddmore.fis.controllers.SubmitControllerIT;
import eu.ddmore.fis.domain.LocalJobStatus;
import eu.ddmore.fis.domain.SubmissionRequest;
import eu.ddmore.fis.domain.SubmissionResponse;

/**
 * Verifies that standalone service fulfils functional requirements
 */
public class ServiceAT extends SystemPropertiesAware {
    private static final Logger LOG = Logger.getLogger(ServiceAT.class);
    private String nonmemCommand;
    private FISHttpRestClient teisClient;
    
    @Rule
    public TemporaryFolder folder= new TemporaryFolder();
    @Before
    public void setUp() throws Exception {
        nonmemCommand = System.getProperty("nonmem.command");
        teisClient = new FISHttpRestClient(System.getProperty("fis.url"));
    }

    @Test
    public void shouldExecuteControlFile() throws IOException, InterruptedException {
        File scriptFile = FileUtils.toFile(SubmitControllerIT.class.getResource("/eu/ddmore/testdata/warfarin_PK_PRED/warfarin_PK_PRED.ctl"));
        Preconditions.checkArgument(scriptFile.exists(), "Script file does not exist");
        FileUtils.copyDirectory(scriptFile.getParentFile(), folder.getRoot());
        SubmissionRequest submissionRequest = new SubmissionRequest();
        submissionRequest.setCommand(nonmemCommand);
        submissionRequest.setExecutionFile(scriptFile.getName());
        submissionRequest.setWorkingDirectory(folder.getRoot().getAbsolutePath());
        SubmissionResponse response = teisClient.submitRequest(submissionRequest);
        
        assertNotNull(response);
        assertNotNull(response.getRequestID());
        
        String jobId = response.getRequestID();
        
        LOG.debug(String.format("Request ID %s", response.getRequestID()));
        
        while(isNotCompleted(teisClient.checkStatus(jobId))) {
            LOG.debug(String.format("Waiting for %s job to complete. Working directory: %s", response.getRequestID(),folder.getRoot()));
            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        }

        LOG.debug(String.format("Files in working directory: %s",Arrays.toString(folder.getRoot().list())));
        
        File outputFile = new File(folder.getRoot(),"output.lst");
        assertTrue(String.format("File %s did not exist", outputFile), outputFile.exists());
        assertEquals(LocalJobStatus.COMPLETED,teisClient.checkStatus(jobId));

        verifyThatFisMetadataFilesExist(folder.getRoot());
    }

    @Test
    public void shouldExecutePharmMLFile() throws IOException, InterruptedException {
        File scriptFile = FileUtils.toFile(SubmitControllerIT.class.getResource("/eu/ddmore/testdata/example3/example3.xml"));
        Preconditions.checkArgument(scriptFile.exists(), "Script file does not exist");
        FileUtils.copyDirectory(scriptFile.getParentFile(), folder.getRoot());
        SubmissionRequest submissionRequest = new SubmissionRequest();
        submissionRequest.setCommand(nonmemCommand);
        submissionRequest.setExecutionFile(scriptFile.getName());
        submissionRequest.setWorkingDirectory(folder.getRoot().getAbsolutePath());
        SubmissionResponse response = teisClient.submitRequest(submissionRequest);
        
        assertNotNull(response);
        assertNotNull(response.getRequestID());
        
        String jobId = response.getRequestID();
        
        LOG.debug(String.format("Request ID %s", response.getRequestID()));
        
        while(isNotCompleted(teisClient.checkStatus(jobId))) {
            LOG.debug(String.format("Waiting for %s job to complete. Working directory: %s", response.getRequestID(),folder.getRoot()));
            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        }
        LOG.debug(String.format("Files in working directory: %s",Arrays.toString(folder.getRoot().list())));
        File pharmmlFile = new File(folder.getRoot(),"example3.pharmml");
        assertTrue(String.format("File %s did not exist", pharmmlFile), pharmmlFile.exists());
        File outputFile = new File(folder.getRoot(),"output.lst");
        assertTrue(String.format("File %s did not exist", outputFile), outputFile.exists());
        assertEquals(LocalJobStatus.COMPLETED,teisClient.checkStatus(jobId));
        
        verifyThatFisMetadataFilesExist(folder.getRoot());
    }

    private void verifyThatFisMetadataFilesExist(File root) {
        File fisHiddenDir = new File(root,".fis");
        assertTrue(String.format("%s directory should be created",fisHiddenDir), new File(root,".fis").exists());
        File stdOut = new File(fisHiddenDir,"stdout");
        File stdErr = new File(fisHiddenDir,"stderr");
        assertTrue(String.format("%s file should be created",stdOut), stdOut.exists());
        assertTrue(String.format("%s file should be created",stdErr), stdErr.exists());
    }

    @Test
    public void shouldExecuteMDLFile() throws IOException, InterruptedException {
        File scriptFile = FileUtils.toFile(SubmitControllerIT.class.getResource("/eu/ddmore/testdata/warfarin_PK_PRED_MDL/warfarin_PK_PRED.mdl"));
        Preconditions.checkArgument(scriptFile.exists(), "Script file does not exist");
        FileUtils.copyDirectory(scriptFile.getParentFile(), folder.getRoot());
        SubmissionRequest submissionRequest = new SubmissionRequest();
        submissionRequest.setCommand(nonmemCommand);
        submissionRequest.setExecutionFile(scriptFile.getName());
        submissionRequest.setWorkingDirectory(folder.getRoot().getAbsolutePath());
        SubmissionResponse response = teisClient.submitRequest(submissionRequest);
        
        assertNotNull(response);
        assertNotNull(response.getRequestID());
        
        String jobId = response.getRequestID();
        
        LOG.debug(String.format("Request ID %s", response.getRequestID()));
        
        while(isNotCompleted(teisClient.checkStatus(jobId))) {
            LOG.debug(String.format("Waiting for %s job to complete. Working directory: %s", response.getRequestID(),folder.getRoot()));
            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        }
        LOG.debug(String.format("Files in working directory: %s",Arrays.toString(folder.getRoot().list())));
        File outputFile = new File(folder.getRoot(),"output.lst");
        assertTrue(String.format("File %s did not exist", outputFile), outputFile.exists());
        File pharmmlFile = new File(folder.getRoot(),"warfarin_PK_PRED.pharmml");
        assertTrue(String.format("File %s did not exist", pharmmlFile), pharmmlFile.exists());
        assertEquals(LocalJobStatus.COMPLETED,teisClient.checkStatus(jobId));
        
        verifyThatFisMetadataFilesExist(folder.getRoot());
    }
    
    private boolean isNotCompleted(LocalJobStatus jobStatus) {
        return LocalJobStatus.RUNNING.compareTo(jobStatus)>=0;
    }
}
