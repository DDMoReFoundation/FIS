package eu.ddmore.fis.controllers;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.LocalJobStatus;


@RunWith(MockitoJUnitRunner.class)
public class JobProcessorTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldExecuteScript() {
        JobProcessor jobProcessor = new JobProcessor();
        jobProcessor.setScriptFile(FileUtils.toFile(JobProcessorTest.class.getResource("/eu/ddmore/fis/controllers/updateJobStatus.groovy")));
        LocalJob job = mock(LocalJob.class);
        
        jobProcessor.process(job);
        
        verify(job).setStatus(eq(LocalJobStatus.COMPLETED));
    }
}