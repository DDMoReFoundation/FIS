package eu.ddmore.fis.controllers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import eu.ddmore.fis.domain.LocalJob;
import groovy.lang.Binding;

@RunWith(MockitoJUnitRunner.class)
public class RetrieveOutputsScriptTest {

	private static final Logger LOG = Logger.getLogger(RetrieveOutputsScriptTest.class);

    @Rule
    public TemporaryFolder testDirectory = new TemporaryFolder();
    
    private File testWorkingDir;
    private File testExecutionHostFileshare;
    private File mifWorkingDir;

    @Before
    public void setUp() throws Exception {
        File testDataDir = FileUtils.toFile(RetrieveOutputsScriptTest.class.getResource("/eu/ddmore/fis/controllers/testWorkingDir"));
        
        this.testWorkingDir = this.testDirectory.getRoot();
        LOG.debug(String.format("Test working dir %s", this.testWorkingDir));
        this.testExecutionHostFileshare = this.testWorkingDir;
        this.mifWorkingDir = new File(this.testExecutionHostFileshare, "MIF_JOB_ID");
        
        FileUtils.copyDirectory(testDataDir, this.testDirectory.getRoot());
    }

    @Test
    public void shouldRetrieveResultFilesFromMIFWorkingDirectory() {
    
        Binding binding = new Binding();
        binding.setVariable("execution.host.fileshare", this.testExecutionHostFileshare);
        
        JobProcessor jobProcessor = new JobProcessor(binding);
        jobProcessor.setScriptFile(FileUtils.toFile(RetrieveOutputsScriptTest.class.getResource("/scripts/retrieveOutputs.groovy")));
        File testWorkingDir = testDirectory.getRoot();

        final String FILE_THAT_SHOULD_NOT_BE_COPIED_BACK = "should_not_be_copied_back.blah";
        assertTrue("Double-checking that the file that shouldn't be copied back does actually exist in the MIF working dir", new File(
                this.mifWorkingDir, FILE_THAT_SHOULD_NOT_BE_COPIED_BACK).exists());

        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn("model.mdl");
        when(job.getId()).thenReturn("MIF_JOB_ID");
        when(job.getOutputFilenamesRegex()).thenReturn(".*\\.(csv|ctl|xml|lst|pharmml|fit)$");

        // Invoke the retrieveOutputs script being tested
        jobProcessor.process(job);

        assertTrue("Output LST resource should be copied back", new File(testWorkingDir, "model.lst").exists());
        assertTrue("PharmML resource should be copied back", new File(testWorkingDir, "model.xml").exists());
        assertFalse("File that doesn't match the list of extensions should not be copied back", new File(testWorkingDir,
                FILE_THAT_SHOULD_NOT_BE_COPIED_BACK).exists());
        File fisHiddenDir = new File(testWorkingDir, ".fis");
        assertTrue(String.format("%s directory should be created", fisHiddenDir), new File(testWorkingDir, ".fis").exists());
        File stdOut = new File(fisHiddenDir, "stdout");
        File stdErr = new File(fisHiddenDir, "stderr");
        assertTrue(String.format("%s file should be created", stdOut), stdOut.exists());
        assertTrue(String.format("%s file should be created", stdErr), stdErr.exists());
    }

}
