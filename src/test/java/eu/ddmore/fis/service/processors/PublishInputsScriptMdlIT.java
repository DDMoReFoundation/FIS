/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.processors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
import eu.ddmore.convertertoolbox.domain.ConversionReport;
import eu.ddmore.convertertoolbox.domain.ConversionReportOutcomeCode;
import eu.ddmore.convertertoolbox.domain.LanguageVersion;
import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.service.cts.ConverterToolboxService;
import eu.ddmore.fis.service.cts.ConverterToolboxServiceException;
import eu.ddmore.fis.service.processors.internal.JobArchiveProvisioner;
import groovy.lang.Binding;

/**
 * Integration Test of publish inputs groovy scripts
 */
@RunWith(MockitoJUnitRunner.class)
public class PublishInputsScriptMdlIT {
    private static final Logger LOG = Logger.getLogger(PublishInputsScriptMdlIT.class);
    private static final String PUBLISH_MDL_INPUTS_SCRIPT="/scripts/publishInputsMdl.groovy";
    private static final String PHEX_ARCHIVE = "archive.phex";
    @Rule
    public TemporaryFolder testDirectory = new TemporaryFolder();

    @Mock
    private ConverterToolboxService converterToolboxService;
    
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
        this.binding.setVariable("fis.mdl.ext", "mdl");
        this.binding.setVariable("fis.pharmml.ext", "xml");
        this.binding.setVariable("execution.host.fileshare.local", this.testExecutionHostFileshareLocal);
        this.binding.setVariable("converterToolboxService",converterToolboxService);
        this.binding.setVariable("mdlLanguage",mdlLanguage);
        this.binding.setVariable("pharmmlLanguage",pharmmlLanguage);
        this.binding.setVariable("archiveFactory",archiveFactory);
        this.binding.setVariable("fis.cts.output.conversionReport", "conversionReport.log");
        this.binding.setVariable("fis.cts.output.archive", PHEX_ARCHIVE);
        this.binding.setVariable("fis.metadata.dir", ".fis");
        this.binding.setVariable("jobArchiveProvisioner", jobArchiveProvisioner);
        
        this.jobProcessor = new JobProcessor(this.binding);
    }
    
    @Test
    public void shouldPublishExtractedArchiveIfMIFDoesntSupportIt() throws IOException, ConverterToolboxServiceException, ArchiveException {
        this.binding.setVariable("fis.mif.archive.support", false);
        
        this.jobProcessor.setScriptFile(FileUtils.toFile(PublishInputsScriptMdlIT.class.getResource(PUBLISH_MDL_INPUTS_SCRIPT)));
        // Prepare FIS Job working directory

        final String modelFileName = "UseCase1.mdl";
        final String dataFileName = "warfarin_conc.csv";
        final String testDataDir = "/test-models/MDL/Product4/";
        final String modelFileInSubDir = "warfarin" + File.separator + modelFileName;
        final String dataFileInSubDir = "warfarin" + File.separator + dataFileName;

        final File scriptFile = new File(testWorkingDir, modelFileInSubDir);
        final File dataFile = new File(testWorkingDir, dataFileInSubDir);
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(testDataDir + modelFileName), scriptFile);
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(testDataDir + dataFileName), dataFile);
        
        // Prepare FIS Job
        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn(modelFileInSubDir);
        when(job.getId()).thenReturn("MIF_JOB_ID");
        
        // mock conversion and Archive
        Archive archive = mockSuccessfulConversionArchive(testWorkingDir, modelFileInSubDir);
        ConversionReport conversionReport = new ConversionReport();
        conversionReport.setReturnCode(ConversionReportOutcomeCode.SUCCESS);
        when(converterToolboxService.isConversionSupported(same(mdlLanguage), same(pharmmlLanguage))).thenReturn(true);
        when(converterToolboxService.convert(same(archive), same(mdlLanguage), same(pharmmlLanguage))).thenReturn(conversionReport);
        
        //When
        jobProcessor.process(job);

        // Then
        verify(this.converterToolboxService).convert(any(Archive.class), same(mdlLanguage), same(pharmmlLanguage));
        assertTrue("Conversion Report file exists", new File(testWorkingDir, ".fis/conversionReport.log").exists());
        assertTrue("Archive is created in FIS metadata directory.", new File(testWorkingDir, ".fis/archive.phex").exists());
        verify(archive).addFile(eq(scriptFile), any(String.class));
        verify(archive).addFile(eq(dataFile), any(String.class));
        verify(jobArchiveProvisioner).provision(same(job), same(archive), eq(mifJobWorkingDir));
    }

    @Test
    public void shouldPublishMDLInputsWhenModelFileWithinSubdirectory() throws IOException, ConverterToolboxServiceException, ArchiveException {
        this.jobProcessor.setScriptFile(FileUtils.toFile(PublishInputsScriptMdlIT.class.getResource(PUBLISH_MDL_INPUTS_SCRIPT)));
        // Prepare FIS Job working directory
        final String modelFileName = "UseCase1.mdl";
        final String dataFileName = "warfarin_conc.csv";
        final String testDataDir = "/test-models/MDL/Product4/";
        final String modelFileInSubDir = "warfarin" + File.separator + modelFileName;
        final String dataFileInSubDir = "warfarin" + File.separator + dataFileName;

        final File scriptFile = new File(testWorkingDir, modelFileInSubDir);
        final File dataFile = new File(testWorkingDir, dataFileInSubDir);
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(testDataDir + modelFileName), scriptFile);
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(testDataDir + dataFileName), dataFile);
        // Prepare FIS Job
        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn(modelFileInSubDir);
        when(job.getId()).thenReturn("MIF_JOB_ID");
        
        // mock conversion and Archive
        Archive archive = mockSuccessfulConversionArchive(testWorkingDir, modelFileInSubDir);
        ConversionReport conversionReport = new ConversionReport();
        conversionReport.setReturnCode(ConversionReportOutcomeCode.SUCCESS);
        when(converterToolboxService.isConversionSupported(same(mdlLanguage), same(pharmmlLanguage))).thenReturn(true);
        when(converterToolboxService.convert(same(archive), same(mdlLanguage), same(pharmmlLanguage))).thenReturn(conversionReport);
        
        //When
        jobProcessor.process(job);

        // Then
        verify(this.converterToolboxService).convert(any(Archive.class), same(mdlLanguage), same(pharmmlLanguage));
        assertTrue("Conversion Report file exists", new File(testWorkingDir, ".fis/conversionReport.log").exists());
        assertTrue("Archive is created in FIS metadata directory.", new File(testWorkingDir, ".fis/archive.phex").exists());
        verify(archive).addFile(eq(scriptFile), any(String.class));
        verify(archive).addFile(eq(dataFile), any(String.class));
        verify(jobArchiveProvisioner).provision(same(job), same(archive), eq(mifJobWorkingDir));
    }

    @Test
    public void shouldMockConversionIfConversionResultsAvailable() throws IOException, ConverterToolboxServiceException, ArchiveException {
        this.jobProcessor.setScriptFile(FileUtils.toFile(PublishInputsScriptMdlIT.class.getResource(PUBLISH_MDL_INPUTS_SCRIPT)));
        // Prepare FIS Job working directory
        final String SCRIPT_FILE_NAME = "MockGeneratedPharmML.mdl";
        final String testDataDir = "/test-models/MDL_with_mock_PharmML/";
        final URL scriptFile = PublishInputsScriptMdlIT.class.getResource(testDataDir + SCRIPT_FILE_NAME);
        FileUtils.copyURLToFile(scriptFile, new File(testWorkingDir, SCRIPT_FILE_NAME));

        // Prepare FIS Job
        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn(SCRIPT_FILE_NAME);
        when(job.getId()).thenReturn("MIF_JOB_ID");
        
        // mock conversion and Archive
        Archive archive = mockSuccessfulConversionArchive(testWorkingDir, SCRIPT_FILE_NAME);
        ConversionReport conversionReport = new ConversionReport();
        conversionReport.setReturnCode(ConversionReportOutcomeCode.SUCCESS);
        when(converterToolboxService.isConversionSupported(same(mdlLanguage), same(pharmmlLanguage))).thenReturn(true);
        when(converterToolboxService.convert(same(archive), same(mdlLanguage), same(pharmmlLanguage))).thenReturn(conversionReport);
        
        //When
        jobProcessor.process(job);

        // Then
        verify(this.converterToolboxService, times(0)).convert(any(Archive.class), same(mdlLanguage), same(pharmmlLanguage));
        assertTrue("Conversion Report file exists", new File(testWorkingDir, ".fis/conversionReport.log").exists());
        assertTrue("Archive is created in FIS metadata directory.", new File(testWorkingDir, ".fis/archive.phex").exists());
        
        // We expect 3 times, because there are two files in mock conversion results
        verify(archive, times(3)).addFile(any(File.class), any(String.class));
        verify(jobArchiveProvisioner).provision(same(job), same(archive), eq(mifJobWorkingDir));
    }
    
    private Archive mockSuccessfulConversionArchive(final File fisJobDir, final String ctlFileName) throws IOException, ArchiveException {
        final Archive archive = mock(Archive.class);
        Entry resultEntry = mock(Entry.class);
        when(resultEntry.getFilePath()).thenReturn(ctlFileName).thenReturn("file.xml");
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
