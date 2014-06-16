/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis;

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
import org.junit.BeforeClass;
import org.junit.Test;

import eu.ddmore.fis.controllers.PublishInputsScriptTest;
import eu.ddmore.fis.domain.LocalJobStatus;
import eu.ddmore.fis.domain.SubmissionRequest;
import eu.ddmore.fis.domain.SubmissionResponse;

/**
 * Verifies that standalone service fulfils functional requirements.
 */
public class ServiceAT extends SystemPropertiesAware {

    private static final Logger LOG = Logger.getLogger(ServiceAT.class);
    private String nonmemCommand;
    private FISHttpRestClient teisClient;

    // This is where the output from FIS and MIF can be found
    private static File parentWorkingDir = new File("target", "ServiceAT_Test_Working_Dir");

    @BeforeClass
    public static void globalSetUp() throws IOException {
        FileUtils.deleteDirectory(parentWorkingDir);
        parentWorkingDir.mkdir();
    }

    @Before
    public void setUp() throws Exception {
        nonmemCommand = System.getProperty("nonmem.command");
        teisClient = new FISHttpRestClient(System.getProperty("fis.url"));
    }

    @Test
    public void shouldExecuteControlFile() throws IOException, InterruptedException {
        final File workingDir = new File(parentWorkingDir, "ControlFileExecution");
        workingDir.mkdir();

        // Copy the files out of the testdata JAR file

        final String SCRIPT_FILE_NAME = "warfarin_PK_PRED.ctl";
        final String DATA_FILE_NAME = "warfarin_conc_pca.csv";

        final String testDataDir = "/eu/ddmore/testdata/models/ctl/warfarin_PK_PRED/";

        final URL scriptFile = PublishInputsScriptTest.class.getResource(testDataDir + SCRIPT_FILE_NAME);
        FileUtils.copyURLToFile(scriptFile, new File(workingDir, SCRIPT_FILE_NAME));
        final URL dataFile = PublishInputsScriptTest.class.getResource(testDataDir + DATA_FILE_NAME);
        FileUtils.copyURLToFile(dataFile, new File(workingDir, DATA_FILE_NAME));

        // Proceed with the test...

        SubmissionRequest submissionRequest = new SubmissionRequest();
        submissionRequest.setCommand(nonmemCommand);
        submissionRequest.setExecutionFile(SCRIPT_FILE_NAME);
        submissionRequest.setWorkingDirectory(workingDir.getAbsolutePath());
        SubmissionResponse response = teisClient.submitRequest(submissionRequest);

        assertNotNull(response);
        assertNotNull(response.getRequestID());

        String jobId = response.getRequestID();

        LOG.debug(String.format("Request ID %s", response.getRequestID()));

        while (isNotCompleted(teisClient.checkStatus(jobId))) {
            LOG.debug(String.format("Waiting for %s job to complete. Working directory: %s", response.getRequestID(), workingDir));
            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        }

        LOG.debug(String.format("Files in working directory: %s", Arrays.toString(workingDir.list())));

        File outputFile = new File(workingDir, "output.lst");
        assertTrue(String.format("File %s did not exist", outputFile), outputFile.exists());

        verifyThatFisMetadataFilesExist(workingDir);

        assertEquals(LocalJobStatus.COMPLETED, teisClient.checkStatus(jobId));
    }

    @Test
    public void shouldExecutePharmMLFile() throws IOException, InterruptedException {
        final File workingDir = new File(parentWorkingDir, "PharmMLFileExecution");
        workingDir.mkdir();

        // Copy the files out of the testdata JAR file

        final String SCRIPT_FILE_NAME = "example3.xml";
        final String DATA_FILE_NAME = "example3_data.csv"; // or _full_data_MDV.csv ?

        final String testDataDir = "/eu/ddmore/testdata/models/pharmml/example3/";

        final URL scriptFile = PublishInputsScriptTest.class.getResource(testDataDir + SCRIPT_FILE_NAME);
        FileUtils.copyURLToFile(scriptFile, new File(workingDir, SCRIPT_FILE_NAME));
        final URL dataFile = PublishInputsScriptTest.class.getResource(testDataDir + DATA_FILE_NAME);
        FileUtils.copyURLToFile(dataFile, new File(workingDir, DATA_FILE_NAME));

        // Proceed with the test...

        SubmissionRequest submissionRequest = new SubmissionRequest();
        submissionRequest.setCommand(nonmemCommand);
        submissionRequest.setExecutionFile(SCRIPT_FILE_NAME);
        submissionRequest.setWorkingDirectory(workingDir.getAbsolutePath());
        SubmissionResponse response = teisClient.submitRequest(submissionRequest);

        assertNotNull(response);
        assertNotNull(response.getRequestID());

        String jobId = response.getRequestID();

        LOG.debug(String.format("Request ID %s", response.getRequestID()));

        while (isNotCompleted(teisClient.checkStatus(jobId))) {
            LOG.debug(String.format("Waiting for %s job to complete. Working directory: %s", response.getRequestID(), workingDir));
            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        }
        LOG.debug(String.format("Files in working directory: %s", Arrays.toString(workingDir.list())));
        File pharmmlFile = new File(workingDir, "example3.pharmml");
        assertTrue(String.format("File %s did not exist", pharmmlFile), pharmmlFile.exists());
        File outputFile = new File(workingDir, "output.lst");
        assertTrue(String.format("File %s did not exist", outputFile), outputFile.exists());
        assertEquals(LocalJobStatus.COMPLETED, teisClient.checkStatus(jobId));

        verifyThatFisMetadataFilesExist(workingDir);
    }

    @Test
    public void shouldExecuteMDLFile() throws IOException, InterruptedException {
        final File workingDir = new File(parentWorkingDir, "MDLFileExecution");
        workingDir.mkdir();

        // Copy the files out of the testdata JAR file

        final String SCRIPT_FILE_NAME = "warfarin_PK_PRED.mdl";

        final String testDataDir = "/eu/ddmore/testdata/models/mdl/warfarin_PK_PRED/";

        final URL scriptFile = PublishInputsScriptTest.class.getResource(testDataDir + SCRIPT_FILE_NAME);
        FileUtils.copyURLToFile(scriptFile, new File(workingDir, SCRIPT_FILE_NAME));

        // Proceed with the test...

        SubmissionRequest submissionRequest = new SubmissionRequest();
        submissionRequest.setCommand(nonmemCommand);
        submissionRequest.setExecutionFile(SCRIPT_FILE_NAME);
        submissionRequest.setWorkingDirectory(workingDir.getAbsolutePath());
        SubmissionResponse response = teisClient.submitRequest(submissionRequest);

        assertNotNull(response);
        assertNotNull(response.getRequestID());

        String jobId = response.getRequestID();

        LOG.debug(String.format("Request ID %s", response.getRequestID()));

        while (isNotCompleted(teisClient.checkStatus(jobId))) {
            LOG.debug(String.format("Waiting for %s job to complete. Working directory: %s", response.getRequestID(), workingDir));
            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        }
        LOG.debug(String.format("Files in working directory: %s", Arrays.toString(workingDir.list())));
        assertEquals(LocalJobStatus.COMPLETED, teisClient.checkStatus(jobId));
        File outputFile = new File(workingDir, "output.lst");
        assertTrue(String.format("File %s did not exist", outputFile), outputFile.exists());
        File pharmmlFile = new File(workingDir, "warfarin_PK_PRED.pharmml");
        assertTrue(String.format("File %s did not exist", pharmmlFile), pharmmlFile.exists());

        verifyThatFisMetadataFilesExist(workingDir);
    }

    private void verifyThatFisMetadataFilesExist(final File root) {
        File fisHiddenDir = new File(root, ".fis");
        assertTrue(String.format("%s directory should be created", fisHiddenDir), new File(root, ".fis").exists());
        File stdOut = new File(fisHiddenDir, "stdout");
        File stdErr = new File(fisHiddenDir, "stderr");
        assertTrue(String.format("%s file should be created", stdOut), stdOut.exists());
        assertTrue(String.format("%s file should be created", stdErr), stdErr.exists());
    }

    private boolean isNotCompleted(final LocalJobStatus jobStatus) {
        return LocalJobStatus.RUNNING.compareTo(jobStatus) >= 0;
    }
}
