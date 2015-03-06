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

import eu.ddmore.fis.domain.LocalJobStatus;
import eu.ddmore.fis.domain.SubmissionRequest;
import eu.ddmore.fis.domain.SubmissionResponse;

/**
 * Verifies that standalone service fulfils functional requirements.
 */
public class ServiceAT extends SystemPropertiesAware {

    private static final Logger LOG = Logger.getLogger(ServiceAT.class);
    
    private FISHttpRestClient teisClient;

    private static String nonmemCommand;

    // This is where the output from FIS and MIF can be found
    private static File parentWorkingDir = new File("target", "ServiceAT_Test_Working_Dir");

    @BeforeClass
    public static void globalSetUp() throws Exception {
        FileUtils.deleteDirectory(parentWorkingDir);
        parentWorkingDir.mkdir();
        nonmemCommand = System.getProperty("nonmem.command");
    }

    @Before
    public void setUp() throws Exception {
        teisClient = new FISHttpRestClient(System.getProperty("fis.url"));
    }

    @Test
    public void shouldExecuteControlFile() throws IOException, InterruptedException {
        final File workingDir = new File(parentWorkingDir, "ControlFileExecution");
        workingDir.mkdir();

        // Copy the files out of the testdata JAR file

        final String SCRIPT_FILE_NAME = "Warfarin-ODE-latest.ctl";
        final String DATA_FILE_NAME = "warfarin_conc.csv";

        final String testDataDir = "/eu/ddmore/testdata/models/NM-TRAN/7.2.0/Warfarin_ODE/";

        final URL scriptFile = ServiceAT.class.getResource(testDataDir + SCRIPT_FILE_NAME);
        FileUtils.copyURLToFile(scriptFile, new File(workingDir, SCRIPT_FILE_NAME));
        final URL dataFile = ServiceAT.class.getResource(testDataDir + DATA_FILE_NAME);
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

        assertTrue("NONMEM Output LST file does not exist in the working directory", new File(workingDir, "Warfarin-ODE-latest.lst").exists());
        assertTrue("Standard Output Object XML file should have been created in the working directory", new File(workingDir, "Warfarin-ODE-latest.SO.xml").exists());

        verifyThatFisMetadataFilesExist(workingDir);

        assertEquals(LocalJobStatus.COMPLETED, teisClient.checkStatus(jobId));
    }

    @Test
    public void shouldExecutePharmMLFile() throws IOException, InterruptedException {
        final File workingDir = new File(parentWorkingDir, "PharmMLFileExecution");
        workingDir.mkdir();

        // Copy the files out of the testdata JAR file

        final String SCRIPT_FILE_NAME = "Warfarin-ODE-latest.xml";
        final String DATA_FILE_NAME = "warfarin_conc.csv";

        final String testDataDir = "/eu/ddmore/testdata/models/PharmML/0.3.1/Warfarin_ODE/";

        final URL scriptFile = ServiceAT.class.getResource(testDataDir + SCRIPT_FILE_NAME);
        FileUtils.copyURLToFile(scriptFile, new File(workingDir, SCRIPT_FILE_NAME));
        final URL dataFile = ServiceAT.class.getResource(testDataDir + DATA_FILE_NAME);
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

        assertTrue("NONMEM Control File does not exist in the working directory", new File(workingDir, "Warfarin-ODE-latest.ctl").exists());
        assertTrue("NONMEM Output LST file does not exist in the working directory", new File(workingDir, "Warfarin-ODE-latest.lst").exists());
        assertTrue("Standard Output Object XML file should have been created in the working directory", new File(workingDir, "Warfarin-ODE-latest.SO.xml").exists());

        verifyThatFisMetadataFilesExist(workingDir);

        assertEquals(LocalJobStatus.COMPLETED, teisClient.checkStatus(jobId));
    }

    @Test
    public void shouldExecuteMDLFile() throws IOException, InterruptedException {
        final File workingDir = new File(parentWorkingDir, "MDLFileExecution");
        workingDir.mkdir();

        // Copy the files out of the testdata JAR file

        final String SCRIPT_FILE_NAME = "Warfarin-ODE-latest.mdl";
        final String DATA_FILE_NAME = "warfarin_conc.csv";

        final String testDataDir = "/eu/ddmore/testdata/models/mdl/6.0.7/Warfarin_ODE/";

        final URL scriptFile = ServiceAT.class.getResource(testDataDir + SCRIPT_FILE_NAME);
        FileUtils.copyURLToFile(scriptFile, new File(workingDir, SCRIPT_FILE_NAME));
        final URL dataFile = ServiceAT.class.getResource(testDataDir + DATA_FILE_NAME);
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

        assertTrue("NONMEM Control File does not exist in the working directory", new File(workingDir, "Warfarin-ODE-latest.ctl").exists());
        assertTrue("NONMEM Output LST file does not exist in the working directory", new File(workingDir, "Warfarin-ODE-latest.lst").exists());
        assertTrue("Standard Output Object XML file should have been created in the working directory", new File(workingDir, "Warfarin-ODE-latest.SO.xml").exists());

        verifyThatFisMetadataFilesExist(workingDir);

        assertEquals(LocalJobStatus.COMPLETED, teisClient.checkStatus(jobId));
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
