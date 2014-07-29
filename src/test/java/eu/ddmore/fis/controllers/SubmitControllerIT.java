/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.ddmore.fis.CommonIntegrationTestContextConfiguration;
import eu.ddmore.fis.SystemPropertiesAware;
import eu.ddmore.fis.domain.LocalJobStatus;
import eu.ddmore.fis.domain.SubmissionRequest;
import eu.ddmore.fis.domain.SubmissionResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CommonIntegrationTestContextConfiguration.class })
public class SubmitControllerIT extends SystemPropertiesAware {

    private static final Logger LOG = Logger.getLogger(SubmitControllerIT.class);

    // This is where the output from FIS and MIF can be found
    private File workingDir = new File("target", "SubmitControllerIT_Test_Working_Dir");

    @Autowired
    SubmitController submitController;

    @Autowired
    JobsController jobsController;

    @Value("${commandline.execute.command}")
    private String command;

    @Before
    public void setUp() throws IOException {
        FileUtils.deleteDirectory(this.workingDir);
        this.workingDir.mkdir();
    }

    @Test
    public void shouldSubmitRequest() throws IOException, InterruptedException {

        // Copy the files out of the testdata JAR file

        final String SCRIPT_FILE_NAME = "warfarin_PK_PRED.ctl";
//        final String DATA_FILE_NAME = "warfarin_conc_pca.csv";
//
//        final String testDataDir = "/eu/ddmore/testdata/models/ctl/warfarin_PK_PRED/";
//
//        final URL scriptFile = PublishInputsScriptTest.class.getResource(testDataDir + SCRIPT_FILE_NAME);
//        FileUtils.copyURLToFile(scriptFile, new File(workingDir, SCRIPT_FILE_NAME));
//        final URL dataFile = PublishInputsScriptTest.class.getResource(testDataDir + DATA_FILE_NAME);
//        FileUtils.copyURLToFile(dataFile, new File(workingDir, DATA_FILE_NAME));

        // Proceed with the test...

        SubmissionRequest submissionRequest = new SubmissionRequest();
        submissionRequest.setCommand(this.command);
        submissionRequest.setCommandParameters("echo Hello from mock NONMEM via command-line connector of MIF! >dummyoutput.lst\necho.");
        submissionRequest.setExecutionFile(SCRIPT_FILE_NAME);
        submissionRequest.setWorkingDirectory(workingDir.getAbsolutePath());

        SubmissionResponse response = submitController.submit(submissionRequest);

        assertNotNull(response);
        assertNotNull(response.getRequestID());

        String jobId = response.getRequestID();

        LOG.debug(String.format("Request ID %s", response.getRequestID()));

        while (isNotCompleted(jobsController.getJobStatus(jobId))) {
            LOG.debug(String.format("Waiting for %s job to complete. Working directory: %s", response.getRequestID(), workingDir));
            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        }
        LOG.debug(String.format("Files in working directory: %s", Arrays.toString(workingDir.list())));
        File outputFile = new File(workingDir, "dummyoutput.lst");
        assertTrue(String.format("File %s did not exist", outputFile), outputFile.exists());
        assertTrue("Checking the content of the dummy output file",
            FileUtils.readFileToString(outputFile).startsWith("Hello from "));
        assertEquals(LocalJobStatus.COMPLETED, jobsController.getJobStatus(jobId));
    }

    private boolean isNotCompleted(final LocalJobStatus jobStatus) {
        return LocalJobStatus.RUNNING.compareTo(jobStatus) >= 0;
    }

}
