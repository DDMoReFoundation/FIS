package eu.ddmore.fis.controllers;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import eu.ddmore.fis.domain.LocalJob;
import groovy.lang.Binding;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.mango.mif.core.exec.Invoker;


@RunWith(MockitoJUnitRunner.class)
public class PublishInputsScriptTest {

    private static final Logger LOG = Logger.getLogger(PublishInputsScriptTest.class);
    
    @Rule
    public TemporaryFolder testDirectory= new TemporaryFolder();
    
    private File testWorkingDir;
    
    private GroovyScriptJobProcessor jobProcessor;
    @Before
    public void setUp() throws Exception {
        testWorkingDir = testDirectory.getRoot();
        LOG.debug(String.format("Test working dir %s",testWorkingDir));
        
        Binding binding = new Binding();
        binding.setVariable("converter.toolbox.executable","Mock Location");
        binding.setVariable("fis.mdl.ext","mdl");
        binding.setVariable("fis.pharmml.ext","xml");
        binding.setVariable("invoker",mock(Invoker.class));
        
        jobProcessor = new GroovyScriptJobProcessor(binding);
        jobProcessor.setScriptFile(FileUtils.toFile(PublishInputsScriptTest.class.getResource("/scripts/publishInputs.groovy")));
    }

    @Test
    public void shouldPublishPharmMLInputs() throws IOException {
        File testDataDir = FileUtils.toFile(PublishInputsScriptTest.class.getResource("/eu/ddmore/testdata/example3"));
        FileUtils.copyDirectory(testDataDir, testWorkingDir);
        
        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn("example3.xml");
        when(job.getId()).thenReturn("MIF_JOB_ID");
        jobProcessor.process(job);
        
        File mifWorkingDir = new File(testWorkingDir,"MIF_JOB_ID");
        

        assertTrue("MIF working directory should be created", mifWorkingDir.exists());
        assertTrue("PharmML resource should be created", new File(mifWorkingDir,"example3.pharmml").exists());
        assertTrue("Data file should be copied from the source", new File(mifWorkingDir,"Example3_full_data_MDV.csv").exists());
        assertTrue("XML resource should be copied back", new File(mifWorkingDir,"example3.xml").exists());
    }

    @Test
    public void shouldPublishMDLInputs() throws IOException {
        
        File testDataDir = FileUtils.toFile(PublishInputsScriptTest.class.getResource("/eu/ddmore/testdata/MDL_with_mock_PharmML"));
        FileUtils.copyDirectory(testDataDir, testWorkingDir);
        
        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn("MockGeneratedPharmML.mdl");
        when(job.getId()).thenReturn("MIF_JOB_ID");
        jobProcessor.process(job);
        
        File mifWorkingDir = new File(testWorkingDir,"MIF_JOB_ID");
        

        assertTrue("MIF working directory should be created", mifWorkingDir.exists());
        assertTrue("PharmML resource should be created", new File(mifWorkingDir,"MockGeneratedPharmML.pharmml").exists());
        assertTrue("Data file should be created", new File(mifWorkingDir,"MockGeneratedPharmML_data.csv").exists());
        assertTrue("XML resource should be copied back", new File(mifWorkingDir,"MockGeneratedPharmML.xml").exists());
    }
    
    @Test
    public void shouldPublishCTLInputs() throws IOException {
        
        File testDataDir = FileUtils.toFile(PublishInputsScriptTest.class.getResource("/eu/ddmore/testdata/warfarin_PK_PRED"));
        FileUtils.copyDirectory(testDataDir, testWorkingDir);
        
        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn("warfarin_PK_PRED.ctl");
        when(job.getId()).thenReturn("MIF_JOB_ID");
        jobProcessor.process(job);
        
        File mifWorkingDir = new File(testWorkingDir,"MIF_JOB_ID");
        

        assertTrue("MIF working directory should be created", mifWorkingDir.exists());
        assertTrue("Data file should be created", new File(mifWorkingDir,"warfarin_conc_pca.csv").exists());
        assertTrue("ctl file should be created", new File(mifWorkingDir,"warfarin_PK_PRED.ctl").exists());
    }
    
}
