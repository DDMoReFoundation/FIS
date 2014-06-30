package eu.ddmore.fis.controllers;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import eu.ddmore.fis.domain.LocalJob;


@RunWith(MockitoJUnitRunner.class)
public class RetrieveOutputsScriptTest {

    @Rule
    public TemporaryFolder testDirectory= new TemporaryFolder();
    
    @Before
    public void setUp() throws Exception {
        File testDataDir = FileUtils.toFile(SubmitControllerIT.class.getResource("/eu/ddmore/fis/controllers/testWorkingDir"));
        FileUtils.copyDirectory(testDataDir, testDirectory.getRoot());
    }

    @Test
    public void shouldRetrieveResultFilesFromMIFWorkingDirectory() {
        JobProcessor jobProcessor = new JobProcessor();
        jobProcessor.setScriptFile(FileUtils.toFile(RetrieveOutputsScriptTest.class.getResource("/scripts/retrieveOutputs.groovy")));
        File testWorkingDir = testDirectory.getRoot();
        
        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn("model.mdl");
        when(job.getId()).thenReturn("MIF_JOB_ID");
        jobProcessor.process(job);
        assertTrue("PharmML resource should be copied back", new File(testWorkingDir,"model.pharmml").exists());
        assertTrue("output.lst resource should be copied back", new File(testWorkingDir,"output.lst").exists());
        assertTrue("XML resource should be copied back", new File(testWorkingDir,"model.xml").exists());
        File fisHiddenDir = new File(testWorkingDir,".fis");
        assertTrue(String.format("%s directory should be created",fisHiddenDir), new File(testWorkingDir,".fis").exists());
        File stdOut = new File(fisHiddenDir,"stdout");
        File stdErr = new File(fisHiddenDir,"stderr");
        assertTrue(String.format("%s file should be created",stdOut), stdOut.exists());
        assertTrue(String.format("%s file should be created",stdErr), stdErr.exists());
    }

}
