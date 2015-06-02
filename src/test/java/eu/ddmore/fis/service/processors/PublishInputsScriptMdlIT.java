/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.processors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
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
import eu.ddmore.fis.controllers.utils.MdlUtils;
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
    
    @Rule
    public TemporaryFolder otherTestDirectory = new TemporaryFolder();

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
    
    private MdlUtils mockMdlUtils;

    private JobProcessor jobProcessor;

    private final static String MODEL_FILE_NAME = "UseCase1.mdl";
    private final static String DATA_FILE_NAME = "warfarin_conc.csv";
    private final static String TEST_DATA_DIR = "/test-models/MDL/Product4/";

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
        
        this.mockMdlUtils = mock(MdlUtils.class);
        List<File> emptyResult = Lists.newArrayList();
        when(this.mockMdlUtils.getDataFileFromMDL(any(File.class))).thenReturn(emptyResult);
        this.binding.setVariable("mdlUtils", mockMdlUtils);
        
        this.jobProcessor = new JobProcessor(this.binding);
    }
    
    @Test
    public void shouldPublishExtractedArchiveIfMIFDoesntSupportIt() throws IOException, ConverterToolboxServiceException, ArchiveException {
        this.binding.setVariable("fis.mif.archive.support", false);
        
        this.jobProcessor.setScriptFile(FileUtils.toFile(PublishInputsScriptMdlIT.class.getResource(PUBLISH_MDL_INPUTS_SCRIPT)));
        
        // Prepare FIS Job working directory
        final String modelFileInSubDir = "warfarin" + File.separator + MODEL_FILE_NAME;
        final String dataFileInSubDir = "warfarin" + File.separator + DATA_FILE_NAME;

        final File modelFile = new File(this.testWorkingDir, modelFileInSubDir);
        final File dataFile = new File(this.testWorkingDir, dataFileInSubDir);
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(TEST_DATA_DIR + MODEL_FILE_NAME), modelFile);
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(TEST_DATA_DIR + DATA_FILE_NAME), dataFile);
        
        // Simulate the data file being associated with the model file
        reset(this.mockMdlUtils);
        when(this.mockMdlUtils.getDataFileFromMDL(modelFile)).thenReturn(Arrays.asList(dataFile));
        
        // Prepare FIS Job
        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(this.testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn(modelFileInSubDir);
        when(job.getId()).thenReturn("MIF_JOB_ID");
        
        final Archive archive = mockConversionAndArchive(this.testWorkingDir, modelFileInSubDir);
        
        // When
        jobProcessor.process(job);

        // Then
        verifyArchive(this.testWorkingDir, job, modelFile, dataFile, "/", "/", archive);
        verify(this.mockMdlUtils).getDataFileFromMDL(modelFile);
    }

    /**
     * Passing in a control file with a relative path, the Job Working Directory is used both
     * to resolve the control file against, and as the location in which to create the Archive.
     */
    @Test
    public void shouldPublishMDLInputs_WorkingDirectoryAbsPathAndControlFileRelPath()
            throws IOException, ConverterToolboxServiceException, ArchiveException {
    
        this.jobProcessor.setScriptFile(FileUtils.toFile(PublishInputsScriptMdlIT.class.getResource(PUBLISH_MDL_INPUTS_SCRIPT)));
        
        // Prepare FIS Job working directory
        final String modelFileInSubDir = "warfarin" + File.separator + MODEL_FILE_NAME;
        final String dataFileInSubDir = "warfarin" + File.separator + DATA_FILE_NAME;
        
        final File modelFile = new File(this.testWorkingDir, modelFileInSubDir);
        final File dataFile = new File(this.testWorkingDir, dataFileInSubDir);
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(TEST_DATA_DIR + MODEL_FILE_NAME), modelFile);
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(TEST_DATA_DIR + DATA_FILE_NAME), dataFile);
        
        // Simulate the data file being associated with the model file
        reset(this.mockMdlUtils);
        when(this.mockMdlUtils.getDataFileFromMDL(modelFile)).thenReturn(Arrays.asList(dataFile));
        
        // Prepare FIS Job
        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(this.testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn(modelFileInSubDir);
        when(job.getId()).thenReturn("MIF_JOB_ID");
        
        final Archive archive = mockConversionAndArchive(this.testWorkingDir, modelFileInSubDir);
        
        // When
        jobProcessor.process(job);

        // Then
        verifyArchive(this.testWorkingDir, job, modelFile, dataFile, "/", "/", archive);
        verify(this.mockMdlUtils).getDataFileFromMDL(modelFile);
    }
    
    /**
     * Passing in a control file with a relative path, the Job Working Directory is used both
     * to resolve the control file against, and as the location in which to create the Archive.
     */
    @Test
    public void shouldPublishMDLInputs_WorkingDirectoryAbsPathAndControlFileRelPath_DataFileNotInSameDirAsControlFile()
            throws IOException, ConverterToolboxServiceException, ArchiveException {
            
        this.jobProcessor.setScriptFile(FileUtils.toFile(PublishInputsScriptMdlIT.class.getResource(PUBLISH_MDL_INPUTS_SCRIPT)));
        
        // Prepare FIS Job working directory
        final String modelFileInSubDir = "models" + File.separator + MODEL_FILE_NAME;
        final String dataFileInSubDir = "data" + File.separator + DATA_FILE_NAME;
        
        final File modelFile = new File(this.testWorkingDir, modelFileInSubDir);
        final File dataFile = new File(this.testWorkingDir, dataFileInSubDir);
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(TEST_DATA_DIR + MODEL_FILE_NAME), modelFile);
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(TEST_DATA_DIR + DATA_FILE_NAME), dataFile);
        
        // Simulate the data file being associated with the model file
        reset(this.mockMdlUtils);
        when(this.mockMdlUtils.getDataFileFromMDL(modelFile)).thenReturn(Arrays.asList(dataFile));
        
        // Prepare FIS Job
        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(this.testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn(modelFileInSubDir);
        when(job.getId()).thenReturn("MIF_JOB_ID");
        
        final Archive archive = mockConversionAndArchive(this.testWorkingDir, modelFileInSubDir);
        
        // When
        jobProcessor.process(job);

        // Then
        verifyArchive(this.testWorkingDir, job, modelFile, dataFile, "/models", "/data", archive);
        verify(this.mockMdlUtils).getDataFileFromMDL(modelFile);
    }
    
    /**
     * Passing in a control file with an absolute path, the Job Working Directory becomes 'divorced'
     * from the control file and can thus point to any location. In this case, the working directory
     * is just used as the location in which to create the Archive.
     */
    @Test
    public void shouldPublishMDLInputs_ControlFileAbsPath()
            throws IOException, ConverterToolboxServiceException, ArchiveException {
    
        this.jobProcessor.setScriptFile(FileUtils.toFile(PublishInputsScriptMdlIT.class.getResource(PUBLISH_MDL_INPUTS_SCRIPT)));
        
        // Prepare FIS Job working directory
        final File modelFile = new File(new File(this.testWorkingDir, "warfarin"), MODEL_FILE_NAME);
        final File dataFile = new File(new File(this.testWorkingDir, "warfarin"), DATA_FILE_NAME);
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(TEST_DATA_DIR + MODEL_FILE_NAME), modelFile);
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(TEST_DATA_DIR + DATA_FILE_NAME), dataFile);
        
        // Control file with absolute path -> job working directory isn't used for
        // resolving the control file against so can point to some other directory
        final File fisWorkingDir = this.otherTestDirectory.getRoot();
        
        // Simulate the data file being associated with the model file
        reset(this.mockMdlUtils);
        when(this.mockMdlUtils.getDataFileFromMDL(modelFile)).thenReturn(Arrays.asList(dataFile));
        
        // Prepare FIS Job
        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(fisWorkingDir.getPath());
        when(job.getControlFile()).thenReturn(modelFile.getAbsolutePath());
        when(job.getId()).thenReturn("MIF_JOB_ID");
        
        final Archive archive = mockConversionAndArchive(fisWorkingDir, MODEL_FILE_NAME);
        
        // When
        jobProcessor.process(job);

        // Then
        verifyArchive(fisWorkingDir, job, modelFile, dataFile, "/", "/", archive);
        verify(this.mockMdlUtils).getDataFileFromMDL(modelFile);
    }
    
    /**
     * Passing in a control file with an absolute path, the Job Working Directory becomes 'divorced'
     * from the control file and can thus point to any location. In this case, the working directory
     * is just used as the location in which to create the Archive.
     */
    @Test
    public void shouldPublishMDLInputs_ControlFileAbsPath_DataFileNotInSameDirAsControlFile()
            throws IOException, ConverterToolboxServiceException, ArchiveException {
            
        this.jobProcessor.setScriptFile(FileUtils.toFile(PublishInputsScriptMdlIT.class.getResource(PUBLISH_MDL_INPUTS_SCRIPT)));
        
        // Prepare FIS Job working directory
        final File modelFile = new File(new File(this.testWorkingDir, "models"), MODEL_FILE_NAME);
        final File dataFile = new File(new File(this.testWorkingDir, "data"), DATA_FILE_NAME);
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(TEST_DATA_DIR + MODEL_FILE_NAME), modelFile);
        FileUtils.copyURLToFile(PublishInputsScriptVerbatimIT.class.getResource(TEST_DATA_DIR + DATA_FILE_NAME), dataFile);
        
        // Control file with absolute path -> job working directory isn't used for
        // resolving the control file against so can point to some other directory
        final File fisWorkingDir = this.otherTestDirectory.getRoot();
        
        // Simulate the data file being associated with the model file
        reset(this.mockMdlUtils);
        when(this.mockMdlUtils.getDataFileFromMDL(modelFile)).thenReturn(Arrays.asList(dataFile));
        
        // Prepare FIS Job
        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(fisWorkingDir.getPath());
        when(job.getControlFile()).thenReturn(modelFile.getAbsolutePath());
        when(job.getId()).thenReturn("MIF_JOB_ID");
        
        final Archive archive = mockConversionAndArchive(fisWorkingDir, MODEL_FILE_NAME);
        
        // When
        jobProcessor.process(job);

        // Then
        verifyArchive(fisWorkingDir, job, modelFile, dataFile, "/models", "/data", archive);
        verify(this.mockMdlUtils).getDataFileFromMDL(modelFile);
    }
    
    @Test
    public void shouldMockConversionIfConversionResultsAvailable() throws IOException, ConverterToolboxServiceException, ArchiveException {
    
        this.jobProcessor.setScriptFile(FileUtils.toFile(PublishInputsScriptMdlIT.class.getResource(PUBLISH_MDL_INPUTS_SCRIPT)));
        
        // Prepare FIS Job working directory
        final String modelFileName = "MockGeneratedPharmML.mdl";
        final String testDataDir = "/test-models/MDL_with_mock_PharmML/";
        final URL scriptFile = PublishInputsScriptMdlIT.class.getResource(testDataDir + modelFileName);
        FileUtils.copyURLToFile(scriptFile, new File(this.testWorkingDir, modelFileName));

        // Prepare FIS Job
        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(this.testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn(modelFileName);
        when(job.getId()).thenReturn("MIF_JOB_ID");
        
        final Archive archive = mockConversionAndArchive(this.testWorkingDir, modelFileName);
        
        // When
        jobProcessor.process(job);

        // Then
        verify(this.converterToolboxService, times(0)).convert(any(Archive.class), same(mdlLanguage), same(pharmmlLanguage));
        assertTrue("Conversion Report file exists", new File(this.testWorkingDir, ".fis/conversionReport.log").exists());
        assertTrue("Archive is created in FIS metadata directory.", new File(this.testWorkingDir, ".fis/archive.phex").exists());
        
        // We expect 3 times, because there are two files in mock conversion results
        verify(archive, times(3)).addFile(any(File.class), any(String.class));
        verify(jobArchiveProvisioner).provision(same(job), same(archive), eq(mifJobWorkingDir));
    }
    
    private Archive mockConversionAndArchive(final File workingDir, final String modelFileRelativePath)
            throws IOException, ArchiveException, ConverterToolboxServiceException {
        
        // Mock a successful conversion
        final Archive archive = mock(Archive.class);
        Entry resultEntry = mock(Entry.class);
        when(resultEntry.getFilePath()).thenReturn(modelFileRelativePath).thenReturn("file.xml");
        List<Entry> mainEntries = Lists.newArrayList(resultEntry);
        when(archive.getMainEntries()).thenReturn(mainEntries);
        when(archiveFactory.createArchive(any(File.class))).then(new Answer<Archive>() {
            @Override
            public Archive answer(InvocationOnMock invocation) throws Throwable {
                File fisMetadataDir = new File(workingDir, ".fis");
                File phexFile = new File(fisMetadataDir, PHEX_ARCHIVE);
                FileUtils.writeStringToFile(phexFile, "This is mock Phex file contents");
                when(archive.getArchiveFile()).thenReturn(phexFile);
                return archive;
            }
        });
        
        final ConversionReport conversionReport = new ConversionReport();
        conversionReport.setReturnCode(ConversionReportOutcomeCode.SUCCESS);
        when(this.converterToolboxService.isConversionSupported(same(mdlLanguage), same(pharmmlLanguage))).thenReturn(true);
        when(this.converterToolboxService.convert(same(archive), same(mdlLanguage), same(pharmmlLanguage))).thenReturn(conversionReport);
        return archive;
    }
    
    private void verifyArchive(final File workingDir, final LocalJob job,
            final File modelFile, final File dataFile, final String modelFileRelPathInArchive, final String dataFileRelPathInArchive,
            final Archive archive) throws ConverterToolboxServiceException, ArchiveException, IOException {
        verify(this.converterToolboxService).convert(same(archive), same(mdlLanguage), same(pharmmlLanguage));
        assertTrue("Conversion Report file exists", new File(workingDir, ".fis/conversionReport.log").exists());
        assertTrue("Archive is created in FIS metadata directory.", new File(workingDir, ".fis/archive.phex").exists());
        verify(archive).addFile(modelFile, modelFileRelPathInArchive);
        verify(archive).addFile(dataFile, dataFileRelPathInArchive);
        verify(this.jobArchiveProvisioner).provision(same(job), same(archive), eq(mifJobWorkingDir));
    }

}
