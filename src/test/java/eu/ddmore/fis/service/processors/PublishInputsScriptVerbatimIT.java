/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.processors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Lists;

import eu.ddmore.archive.Archive;
import eu.ddmore.archive.ArchiveFactory;
import eu.ddmore.archive.Entry;
import eu.ddmore.archive.exception.ArchiveException;
import eu.ddmore.convertertoolbox.domain.LanguageVersion;
import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.service.cts.ConverterToolboxServiceException;
import eu.ddmore.fis.service.processors.internal.JobArchiveProvisioner;
import groovy.lang.Binding;

/**
 * Integration Test for publishing script that doesn't perform conversion
 */
@RunWith(MockitoJUnitRunner.class)
public class PublishInputsScriptVerbatimIT {
    private static final Logger LOG = Logger.getLogger(PublishInputsScriptVerbatimIT.class);
    private static final String PUBLISH_VERBATIM_INPUTS_SCRIPT="/scripts/publishInputsVerbatim.groovy";
    private static final String PHEX_ARCHIVE = "archive.phex";
    @Rule
    public TemporaryFolder testDirectory = new TemporaryFolder();

    @Mock
    private LanguageVersion mdlLanguage;
    
    @Mock
    private LanguageVersion pharmmlLanguage;
    
    @Mock
    private ArchiveFactory archiveFactory;
    
    @Mock
    private JobArchiveProvisioner jobArchiveProvisioner;
    
    private File testWorkingDir;
    private File testExecutionHostFileshareLocal;
    private File mifJobWorkingDir;

    private Binding binding;

    private JobProcessor jobProcessor;

    @Before
    public void setUp() {
        this.testWorkingDir = this.testDirectory.getRoot();
        LOG.debug(String.format("Test working dir %s", this.testWorkingDir));
        this.testExecutionHostFileshareLocal = this.testWorkingDir;
        this.mifJobWorkingDir = new File(this.testExecutionHostFileshareLocal, "MIF_JOB_ID");

        this.binding = new Binding();
        this.binding.setVariable("execution.host.fileshare.local", this.testExecutionHostFileshareLocal);
        this.binding.setVariable("archiveFactory",archiveFactory);
        this.binding.setVariable("fis.cts.output.archive", PHEX_ARCHIVE);
        this.binding.setVariable("fis.metadata.dir", ".fis");
        this.binding.setVariable("jobArchiveProvisioner", jobArchiveProvisioner);
        
        this.jobProcessor = new JobProcessor(this.binding);
    }

    @Test
    public void shouldPublishCTLInputs() throws IOException, ArchiveException {
        this.jobProcessor.setScriptFile(FileUtils.toFile(PublishInputsScriptVerbatimIT.class.getResource(PUBLISH_VERBATIM_INPUTS_SCRIPT)));
        // Copy the files out of the testdata JAR file
        final String testDataDir = "/test-models/NM-TRAN/7.2.0/Warfarin_ODE/";
        final String ctlFileName = "Warfarin-ODE-latest.ctl";
        final String dataFileName = "warfarin_conc.csv";


        final File scriptFile = new File(testWorkingDir, ctlFileName);
        final File dataFile = new File(testWorkingDir, dataFileName);
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(testDataDir + ctlFileName), scriptFile);
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(testDataDir + dataFileName), dataFile);

        // Proceed with the test...

        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn(ctlFileName);
        when(job.getId()).thenReturn("MIF_JOB_ID");
        
        Archive archive = mockArchiveCreation(testWorkingDir,ctlFileName);
        
        jobProcessor.process(job);

        assertTrue("Archive is created in FIS metadata directory.", new File(testWorkingDir, ".fis/archive.phex").exists());
        verify(archive).addFile(eq(scriptFile), eq(""));
        verify(archive).addFile(eq(dataFile), eq(""));
        verify(jobArchiveProvisioner).provision(same(job), same(archive), eq(mifJobWorkingDir));
    }

    @Test
    public void shouldPublishPharmMLInputs() throws IOException, ArchiveException {

        this.jobProcessor.setScriptFile(FileUtils.toFile(PublishInputsScriptVerbatimIT.class.getResource(PUBLISH_VERBATIM_INPUTS_SCRIPT)));
        // Copy the files out of the testdata JAR file
        final String testDataDir = "/test-models/PharmML/0.3.0/example3/";
        final String modelFileName = "example3.xml";
        final String dataFileName = "example3_data.csv";

        final File scriptFile = new File(testWorkingDir, modelFileName);
        final File dataFile = new File(testWorkingDir, dataFileName);
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(testDataDir + modelFileName), scriptFile);
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(testDataDir + dataFileName), dataFile);

        // Proceed with the test...

        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn(modelFileName);
        when(job.getId()).thenReturn("MIF_JOB_ID");
        
        Archive archive = mockArchiveCreation(testWorkingDir,modelFileName);
        
        jobProcessor.process(job);

        assertTrue("Archive is created in FIS metadata directory.", new File(testWorkingDir, ".fis/archive.phex").exists());
        verify(archive).addFile(eq(scriptFile), eq(""));
        verify(archive).addFile(eq(dataFile), eq(""));
        verify(jobArchiveProvisioner).provision(same(job), same(archive), eq(mifJobWorkingDir));
    }
    

    @Test
    public void shouldPublishCTLInputsWhenModelFileWithinSubdirectory() throws IOException, ArchiveException {
        this.jobProcessor.setScriptFile(FileUtils.toFile(PublishInputsScriptVerbatimIT.class.getResource(PUBLISH_VERBATIM_INPUTS_SCRIPT)));

        // Copy the files out of the testdata JAR file
        final String testDataDir = "/test-models/NM-TRAN/7.2.0/Warfarin_ODE/";
        final String modelFileName = "Warfarin-ODE-latest.ctl";
        final String dataFileName = "warfarin_conc.csv";
        final String modelFileInSubDir = "warfarin"+File.separator+modelFileName;
        final String dataFileInSubDir = "warfarin"+File.separator+dataFileName;
        
        final File ctlFile = new File(testWorkingDir, modelFileInSubDir);
        final File dataFile = new File(testWorkingDir, dataFileInSubDir);
        
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(testDataDir + modelFileName), ctlFile);
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(testDataDir + dataFileName), dataFile);

        // Proceed with the test...

        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn(modelFileInSubDir);
        when(job.getId()).thenReturn("MIF_JOB_ID");

        Archive archive = mockArchiveCreation(testWorkingDir,modelFileInSubDir);
        
        jobProcessor.process(job);

        assertTrue("Archive is created in FIS metadata directory.", new File(testWorkingDir, ".fis/archive.phex").exists());
        verify(archive).addFile(eq(ctlFile), eq(File.separator+"warfarin"));
        verify(archive).addFile(eq(dataFile), eq(File.separator+"warfarin"));
        verify(jobArchiveProvisioner).provision(same(job), same(archive), eq(mifJobWorkingDir));
    }

    @Test
    public void shouldPublishPharmMLInputsWhenModelFileWithinSubdirectory() throws IOException, ConverterToolboxServiceException, ArchiveException {
        this.jobProcessor.setScriptFile(FileUtils.toFile(PublishInputsScriptVerbatimIT.class.getResource(PUBLISH_VERBATIM_INPUTS_SCRIPT)));
        // Copy the files out of the testdata JAR file
        final String testDataDir = "/test-models/PharmML/0.3.0/example3/";
        
        final String modelFileName = "example3.xml";
        final String modelFileInSubDir = "example3" + File.separator + modelFileName;
        final String dataFileName = "example3_data.csv";
        final String dataFileInSubDir = "example3" + File.separator + dataFileName;

        final File scriptFile = new File(testWorkingDir, modelFileInSubDir);
        final File dataFile = new File(testWorkingDir, dataFileInSubDir);
        
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(testDataDir + modelFileName), scriptFile);
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(testDataDir + dataFileName), dataFile);

        // Proceed with the test...

        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn(modelFileInSubDir);
        when(job.getId()).thenReturn("MIF_JOB_ID");
        
        Archive archive = mockArchiveCreation(testWorkingDir, modelFileInSubDir);
        
        jobProcessor.process(job);

        assertTrue("Archive is created in FIS metadata directory.", new File(testWorkingDir, ".fis/archive.phex").exists());
        verify(archive).addFile(eq(scriptFile), eq(File.separator+"example3"));
        verify(archive).addFile(eq(dataFile), eq(File.separator+"example3"));
        verify(jobArchiveProvisioner).provision(same(job), same(archive), eq(mifJobWorkingDir));
    }

    private Archive mockArchiveCreation(final File fisJobDir, String ctlFileName) throws IOException, ArchiveException {
        final Archive archive = mock(Archive.class);
        Entry resultEntry = mock(Entry.class);
        when(resultEntry.getFilePath()).thenReturn(ctlFileName);
        List<Entry> mainEntries = Lists.newArrayList(resultEntry);
        when(archive.getMainEntries()).thenReturn(mainEntries);
        when(archiveFactory.createArchive(any(File.class))).then(new Answer<Archive>() {
            @Override
            public Archive answer(InvocationOnMock invocation) throws Throwable {
                File fisMetadataDir = new File(fisJobDir, ".fis");
                File phexFile = new File(fisMetadataDir, PHEX_ARCHIVE);
                FileUtils.writeStringToFile(phexFile, "This is mock Phex file contents");
                when(archive.getArchiveFile()).thenReturn(phexFile);
                return archive;
            }
        });
        return archive;
    }

}
