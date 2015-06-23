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
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void globalSetUp() throws Exception {
        nonmemCommand = System.getProperty("nonmem.command");
    }

    @Before
    public void setUp() throws Exception {
        teisClient = new FISHttpRestClient(System.getProperty("fis.url"), System.getProperty("fis.management.url"));
    }

    @Test
    public void shouldExecuteControlFile() throws IOException, InterruptedException {
        final File userProjectDir = new File(this.temporaryFolder.getRoot(), "ControlFileExecution-UserProjectDir");
        userProjectDir.mkdir();

        // Copy the files out of the testdata JAR file

        final String MODEL_FILE_NAME = "Warfarin-ODE-latest.ctl";
        final String DATA_FILE_NAME = "warfarin_conc.csv";

        final String testDataDir = "/test-models/NM-TRAN/7.2.0/Warfarin_ODE/";

        FileUtils.copyURLToFile( ServiceAT.class.getResource(testDataDir + MODEL_FILE_NAME), new File(userProjectDir, MODEL_FILE_NAME) );
        FileUtils.copyURLToFile( ServiceAT.class.getResource(testDataDir + DATA_FILE_NAME), new File(userProjectDir, DATA_FILE_NAME) );

        // Proceed with the test...
        submitJobAndWaitForCompletionThenVerifyOutput(userProjectDir, MODEL_FILE_NAME);

    }

    @Test
    public void shouldExecutePharmMLFile() throws IOException, InterruptedException {
        final File userProjectDir = new File(this.temporaryFolder.getRoot(), "PharmMLFileExecution-UserProjectDir");
        userProjectDir.mkdir();

        // Copy the files out of the testdata JAR file

        final String MODEL_FILE_NAME = "UseCase1.xml";
        final String DATA_FILE_NAME = "warfarin_conc.csv";
        
        final String testDataDir = "/test-models/PharmML/0.6.0/";

        FileUtils.copyURLToFile( ServiceAT.class.getResource(testDataDir + MODEL_FILE_NAME), new File(userProjectDir, MODEL_FILE_NAME) );
        FileUtils.copyURLToFile( ServiceAT.class.getResource(testDataDir + DATA_FILE_NAME), new File(userProjectDir, DATA_FILE_NAME) );

        // Proceed with the test...
        submitJobAndWaitForCompletionThenVerifyOutput(userProjectDir, MODEL_FILE_NAME);

    }

    @Test
    public void shouldExecuteMDLFile() throws IOException, InterruptedException {
        final File userProjectDir = new File(this.temporaryFolder.getRoot(), "MDLFileExecution-UserProjectDir");
        userProjectDir.mkdir();

        // Copy the files out of the testdata JAR file

        final String MODEL_FILE_NAME = "UseCase1.mdl";
        final String DATA_FILE_NAME = "warfarin_conc.csv";

        final String testDataDir = "/test-models/MDL/Product4/";

        FileUtils.copyURLToFile( ServiceAT.class.getResource(testDataDir + MODEL_FILE_NAME), new File(userProjectDir, MODEL_FILE_NAME) );
        FileUtils.copyURLToFile( ServiceAT.class.getResource(testDataDir + DATA_FILE_NAME), new File(userProjectDir, DATA_FILE_NAME) );

        // Proceed with the test...
        submitJobAndWaitForCompletionThenVerifyOutput(userProjectDir, MODEL_FILE_NAME);
        
    }
    
    @Test
    public void shouldExecuteMDLFileHavingRelativePathToDataFile() throws IOException, InterruptedException {
        final File userProjectDir = new File(this.temporaryFolder.getRoot(), "MDLFileExecutionDataFileRelPath-UserProjectDir");
        userProjectDir.mkdir();

        // Copy the files out of the testdata JAR file

        final String MODEL_FILE = "models/UseCase1.mdl";
        final String DATA_FILE = "data/warfarin_conc.csv";

        final String testDataDir = "/test-models/MDL/Product4/";

        final File modelFile = new File(userProjectDir, MODEL_FILE);
        final File dataFile = new File(userProjectDir, DATA_FILE);
        
        final String modelFileContent = IOUtils.toString(ServiceAT.class.getResource(testDataDir + MODEL_FILE.split("/")[1]));
        FileUtils.write(modelFile, modelFileContent.replace(DATA_FILE.split("/")[1], "../" + DATA_FILE));
        FileUtils.copyURLToFile(ServiceAT.class.getResource(testDataDir + DATA_FILE.split("/")[1]), dataFile);

        // Proceed with the test...
        submitJobAndWaitForCompletionThenVerifyOutput(userProjectDir, MODEL_FILE);
    }
    
    /**
     * @param userProjectDir - the directory within which the model file and data files live
     * @param modelFile - the model file, possibly with a relative path prefix if appropriate for the calling test
     * @throws InterruptedException could be thrown from Thread.sleep()
     */
    private void submitJobAndWaitForCompletionThenVerifyOutput(final File userProjectDir, final String modelFile) throws InterruptedException {
        final File workingDir = new File(userProjectDir.toString().replace("-UserProjectDir", "-WorkingDir"));
        workingDir.mkdir();

        SubmissionRequest submissionRequest = new SubmissionRequest();
        submissionRequest.setCommand(nonmemCommand);
        submissionRequest.setExecutionFile(new File(userProjectDir, modelFile).getAbsolutePath());
        submissionRequest.setWorkingDirectory(workingDir.getAbsolutePath());
        SubmissionResponse response = teisClient.submitRequest(submissionRequest);

        assertNotNull("SubmissionResponse should not be null", response);
        assertNotNull("Request ID should be set on the SubmissionResponse", response.getRequestID());

        String jobId = response.getRequestID();

        LOG.debug(String.format("Request ID %s", response.getRequestID()));

        while (isNotCompleted(teisClient.checkStatus(jobId))) {
            LOG.debug(String.format("Waiting for %s job to complete. Working directory: %s", response.getRequestID(), workingDir));
            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        }

        LOG.debug(String.format("Files in working directory: %s", Arrays.toString(workingDir.list())));
        
        assertEquals("Job should complete with status COMPLETED", LocalJobStatus.COMPLETED, teisClient.checkStatus(jobId));

        assertTrue("NONMEM Control File does not exist in the working directory", new File(workingDir, modelFile.replaceFirst("\\..*", ".ctl")).exists());
        assertTrue("NONMEM Output LST file does not exist in the working directory", new File(workingDir, modelFile.replaceFirst("\\..*", ".lst")).exists());
        assertTrue("Standard Output Object XML file should have been created in the working directory", new File(workingDir, modelFile.toString().replaceFirst("\\..*", ".SO.xml")).exists());

        verifyThatFisMetadataFilesExist(workingDir);

        assertEquals("Status of the Job should be COMPLETED", LocalJobStatus.COMPLETED, teisClient.checkStatus(jobId));
    }

    /**
     * @param workingDir - the FIS working directory, in which the .fis metadata directory will have been created
     */
    private void verifyThatFisMetadataFilesExist(final File workingDir) {
        File fisHiddenDir = new File(workingDir, ".fis");
        assertTrue(String.format("%s directory should be created", fisHiddenDir), new File(workingDir, ".fis").exists());
        File stdOut = new File(fisHiddenDir, "stdout");
        File stdErr = new File(fisHiddenDir, "stderr");
        assertTrue(String.format("%s file should be created", stdOut), stdOut.exists());
        assertTrue(String.format("%s file should be created", stdErr), stdErr.exists());
    }

    private boolean isNotCompleted(final LocalJobStatus jobStatus) {
        return LocalJobStatus.RUNNING.compareTo(jobStatus) >= 0;
    }

}
