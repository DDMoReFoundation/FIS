package eu.ddmore.fis.controllers;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.LocalJobStatus;


@RunWith(MockitoJUnitRunner.class)
public class GroovyScriptJobProcessorTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldExecuteScript() {
        GroovyScriptJobProcessor jobProcessor = new GroovyScriptJobProcessor();
        jobProcessor.setScriptFile(FileUtils.toFile(GroovyScriptJobProcessorTest.class.getResource("/eu/ddmore/fis/controllers/updateJobStatus.groovy")));
        LocalJob job = mock(LocalJob.class);
        
        jobProcessor.process(job);
        
        verify(job).setStatus(eq(LocalJobStatus.COMPLETED));
    }

}
