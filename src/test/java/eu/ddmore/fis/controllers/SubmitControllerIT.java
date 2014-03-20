/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Preconditions;

import eu.ddmore.fis.CommonIntegrationTestContextConfiguration;
import eu.ddmore.fis.SystemPropertiesAware;
import eu.ddmore.fis.domain.LocalJobStatus;
import eu.ddmore.fis.domain.SubmissionRequest;
import eu.ddmore.fis.domain.SubmissionResponse;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {CommonIntegrationTestContextConfiguration.class})
public class SubmitControllerIT  extends SystemPropertiesAware {

    private static final Logger LOG = Logger.getLogger(SubmitControllerIT.class);
    
    @Rule
    public TemporaryFolder folder= new TemporaryFolder();
    
	@Autowired
	SubmitController submitController;
	
	@Autowired
	JobsController jobsController;

	private String nonmemCommand;
	@Before
	public void setUp() {
        nonmemCommand = System.getProperty("nonmem.command");
	}
	
	@Test
	public void shouldSubmitRequest() throws IOException, InterruptedException {
        File scriptFile = FileUtils.toFile(SubmitControllerIT.class.getResource("/eu/ddmore/testdata/warfarin_PK_PRED/warfarin_PK_PRED.ctl"));
        Preconditions.checkArgument(scriptFile.exists(), "Script file does not exist");
        FileUtils.copyDirectory(scriptFile.getParentFile(), folder.getRoot());
        SubmissionRequest submissionRequest = new SubmissionRequest();
        submissionRequest.setCommand(nonmemCommand);
        submissionRequest.setExecutionFile(scriptFile.getName());
        submissionRequest.setWorkingDirectory(folder.getRoot().getAbsolutePath());
        
        SubmissionResponse response = submitController.submit(submissionRequest);

        assertNotNull(response);
        assertNotNull(response.getRequestID());
        
        String jobId = response.getRequestID();
        
        LOG.debug(String.format("Request ID %s", response.getRequestID()));
        
        while(isNotCompleted(jobsController.getJobStatus(jobId))) {
            LOG.debug(String.format("Waiting for %s job to complete. Working directory: %s", response.getRequestID(),folder.getRoot()));
            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        }
        LOG.debug(String.format("Files in working directory: %s",Arrays.toString(folder.getRoot().list())));
        File outputFile = new File(folder.getRoot(),"output.lst");
        assertTrue(String.format("File %s did not exist", outputFile), outputFile.exists());
        assertEquals(LocalJobStatus.COMPLETED,jobsController.getJobStatus(jobId));
	}


    private boolean isNotCompleted(LocalJobStatus jobStatus) {
        return LocalJobStatus.RUNNING.compareTo(jobStatus)>=0;
    }
	
}
