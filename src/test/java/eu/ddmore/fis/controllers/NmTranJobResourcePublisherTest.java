package eu.ddmore.fis.controllers;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Preconditions;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.InvokerResult;

import eu.ddmore.fis.SystemPropertiesAware;
import eu.ddmore.fis.domain.LocalJob;

@RunWith(MockitoJUnitRunner.class)
public class NmTranJobResourcePublisherTest extends SystemPropertiesAware {

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();
    
    @InjectMocks
    private NmTranJobResourcePublisher publisher =  new NmTranJobResourcePublisher();
    
    @Mock
    private Invoker invoker;
    
    @Before
    public void setUp() throws ExecutionException {
        publisher.setConverterToolboxExecutable("mock-converter-toolbox");
        publisher.setPublishInputsScript(System.getProperty("fis.publishInputs"));
        InvokerResult invokerResult = mock(InvokerResult.class);
        when(invokerResult.getExitStatus()).thenReturn(0);
        when(invoker.execute((String)any())).thenReturn(invokerResult);
    }
    
    @Test
    public void shouldCopyAllFilesToMIFWorkingDirectory() throws IOException {
        File scriptFile = FileUtils.toFile(SubmitControllerIT.class.getResource("/eu/ddmore/testdata/warfarin_PK_PRED/warfarin_PK_PRED.ctl"));
        Preconditions.checkArgument(scriptFile.exists(), "Script file does not exist");
        FileUtils.copyDirectory(scriptFile.getParentFile(), folder.getRoot());
        LocalJob job = mock(LocalJob.class);
        when(job.getCommand()).thenReturn("COMMAND");
        when(job.getId()).thenReturn("1234");
        when(job.getControlFile()).thenReturn("CONTROL_FILE");
        when(job.getWorkingDirectory()).thenReturn(folder.getRoot().getAbsolutePath());
        publisher.process(job);
        assertTrue("MIF working directory must exist", new File(folder.getRoot(), "1234").exists());
        assertTrue("MIF working directory must exist", new File(new File(folder.getRoot(), "1234"),scriptFile.getName()).exists());
    }
    
    @Test
    public void shouldRenameMDLFileExtensionToPharmML() {
        LocalJob job = mock(LocalJob.class);
        when(job.getCommand()).thenReturn("COMMAND");
        when(job.getId()).thenReturn("1234");
        when(job.getControlFile()).thenReturn("CONTROL_FILE.mdl");
        when(job.getWorkingDirectory()).thenReturn(folder.getRoot().getAbsolutePath());
        publisher.process(job);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(job).setControlFile(captor.capture());
        assertTrue("MDL file extension must be replaced by 'xml'",captor.getValue().endsWith("xml"));
    }
    
    @Test
    public void shouldNotRenameCTLFileExtensionToPharmML() {
        LocalJob job = mock(LocalJob.class);
        when(job.getCommand()).thenReturn("COMMAND");
        when(job.getId()).thenReturn("1234");
        when(job.getControlFile()).thenReturn("CONTROL_FILE.ctl");
        when(job.getWorkingDirectory()).thenReturn(folder.getRoot().getAbsolutePath());
        publisher.process(job);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(job, times(0)).setControlFile(captor.capture());
    }
}
