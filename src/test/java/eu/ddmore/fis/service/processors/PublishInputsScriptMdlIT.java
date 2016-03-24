/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.processors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.ddmore.archive.Archive;
import eu.ddmore.archive.exception.ArchiveException;
import eu.ddmore.convertertoolbox.domain.ConversionReport;
import eu.ddmore.convertertoolbox.domain.ConversionReportOutcomeCode;
import eu.ddmore.convertertoolbox.domain.LanguageVersion;
import eu.ddmore.fis.configuration.Fileshare;
import eu.ddmore.fis.controllers.utils.ArchiveCreator;
import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.service.cts.ConverterToolboxService;
import eu.ddmore.fis.service.cts.ConverterToolboxServiceException;
import eu.ddmore.fis.service.processors.internal.JobArchiveProvisioner;
import groovy.lang.Binding;

/**
 * Integration Test for Publish MDL Inputs groovy script.
 */
@RunWith(MockitoJUnitRunner.class)
public class PublishInputsScriptMdlIT {
    private static final Logger LOG = Logger.getLogger(PublishInputsScriptMdlIT.class);
    
    private static final String PUBLISH_MDL_INPUTS_SCRIPT = "/scripts/publishInputsMdl.groovy";
    
    private final static String MODEL_FILE_NAME = "model.mdl";
    
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
    private ArchiveCreator mockArchiveCreator;
    
    @Mock
    private JobArchiveProvisioner jobArchiveProvisioner;
    
    private File testWorkingDir;
    private File mifJobWorkingDir;

    private Binding binding;

    private JobProcessor jobProcessor;

    @Before
    public void setUp() {
        assertFalse("Ensuring that the two test directories created for the test, are different",
            this.testDirectory.getRoot().equals(this.otherTestDirectory.getRoot()));
        
        this.testWorkingDir = this.testDirectory.getRoot();
        LOG.debug(String.format("Test working dir %s", this.testWorkingDir));
        final File testExecutionHostFileshareLocal = this.testWorkingDir;
        this.mifJobWorkingDir = new File(testExecutionHostFileshareLocal, "MIF_JOB_ID");
        final Fileshare fileshare = new Fileshare();
        fileshare.setFisHostPath(testExecutionHostFileshareLocal.getAbsolutePath());
        this.binding = new Binding();
        this.binding.setVariable("fis.mdl.ext", "mdl");
        this.binding.setVariable("fis.pharmml.ext", "xml");
        this.binding.setVariable("fileshare", fileshare);
        this.binding.setVariable("converterToolboxService",converterToolboxService);
        this.binding.setVariable("mdlLanguage",mdlLanguage);
        this.binding.setVariable("pharmmlLanguage",pharmmlLanguage);
        this.binding.setVariable("fis.cts.output.conversionReport", "conversionReport.log");
        this.binding.setVariable("fis.cts.output.archive", PHEX_ARCHIVE);
        this.binding.setVariable("fis.metadata.dir", ".fis");
        this.binding.setVariable("jobArchiveProvisioner", jobArchiveProvisioner);
        
        this.binding.setVariable("archiveCreator", this.mockArchiveCreator);
        
        this.jobProcessor = new JobProcessor(this.binding);
        
        this.jobProcessor.setScriptFile(FileUtils.toFile(PublishInputsScriptMdlIT.class.getResource(PUBLISH_MDL_INPUTS_SCRIPT)));
    }

    /**
     * Passing in a model file with a relative path, the Job Working Directory is used both
     * to resolve the model file against, and as the location in which to create the Archive.
     */
    @Test
    public void shouldPublishMDLInputs_WorkingDirectoryAbsPathAndModelFileRelPath()
            throws IOException, ConverterToolboxServiceException, ArchiveException {
            
        final String modelFileInSubDir = "mydir" + File.separator + MODEL_FILE_NAME;
        // Don't actually need to physically create the model & data files for these tests
        final File modelFile = new File(this.testWorkingDir, modelFileInSubDir);
        
        // Prepare FIS Job
        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(this.testWorkingDir.getAbsolutePath());
        when(job.getExecutionFile()).thenReturn(modelFileInSubDir);
        when(job.getId()).thenReturn("MIF_JOB_ID");
        
        final Archive archive = mockArchiveCreationAndConversion(this.testWorkingDir, modelFile);
        
        // When
        jobProcessor.process(job);

        // Then
        verifyArchiveCreationAndConversionAndProvisioningToMIF(this.testWorkingDir, modelFile, job, archive);
    }
    
    /**
     * Passing in a model file with an absolute path, the Job Working Directory becomes 'divorced'
     * from the model file and can thus point to any location. In this case, the working directory
     * is just used as the location in which to create the Archive.
     */
    @Test
    public void shouldPublishMDLInputs_ModelFileAbsPath()
            throws IOException, ConverterToolboxServiceException, ArchiveException {
    
        // Don't actually need to physically create the model & data files for these tests
        final File modelFile = new File(new File(this.testWorkingDir, "mydir"), MODEL_FILE_NAME);
        
        // Model file with absolute path -> job working directory isn't used for
        // resolving the model file against so can point to some other directory
        final File fisWorkingDir = this.otherTestDirectory.getRoot();
        
        // Prepare FIS Job
        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(fisWorkingDir.getPath());
        when(job.getExecutionFile()).thenReturn(modelFile.getAbsolutePath());
        when(job.getId()).thenReturn("MIF_JOB_ID");
        
        final Archive archive = mockArchiveCreationAndConversion(fisWorkingDir, modelFile);
        
        // When
        jobProcessor.process(job);

        // Then
        verifyArchiveCreationAndConversionAndProvisioningToMIF(fisWorkingDir, modelFile, job, archive);
    }
    
    /**
     * If a list of extraInputFiles paths is provided on the job then these are to be included in the created Archive.
     * The paths can either be absolute, or relative in which case they will be resolved against the model file,
     * but this takes place in the Archive Creator and thus isn't relevant to this unit test.
     */
    @Test
    public void shouldPublishMDLInputs_ExtraInputFilesProvidedHavingBothRelativeAndAbsolutePaths()
            throws IOException, ConverterToolboxServiceException, ArchiveException {
    
        // Don't actually need to physically create the model & data files for these tests
        final File modelFile = new File(new File(this.testWorkingDir, "mydir"), MODEL_FILE_NAME);
        
        // Model file with absolute path -> job working directory isn't used for
        // resolving the model file against so can point to some other directory
        final File fisWorkingDir = this.otherTestDirectory.getRoot();
        
        // Prepare FIS Job
        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(fisWorkingDir.getPath());
        when(job.getExecutionFile()).thenReturn(modelFile.getAbsolutePath());
        when(job.getExtraInputFiles()).thenReturn(Arrays.asList("/path/to/other/file/1", "../other/file/2"));
        when(job.getId()).thenReturn("MIF_JOB_ID");
        
        final Archive archive = mockArchiveCreationAndConversion(fisWorkingDir, modelFile, new File("/path/to/other/file/1"), new File("../other/file/2"));
        
        // When
        jobProcessor.process(job);

        // Then
        verifyArchiveCreationAndConversionAndProvisioningToMIF(fisWorkingDir, modelFile, job, archive);
    }
    
    @Test
    public void shouldMockConversionIfConversionResultsAvailable() throws IOException, ConverterToolboxServiceException, ArchiveException {
        
        // Prepare FIS Job working directory
        final String modelFileName = "MockGeneratedPharmML.mdl";
        final String testDataDir = "/test-models/MDL_with_mock_PharmML/";
        final URL scriptFile = PublishInputsScriptMdlIT.class.getResource(testDataDir + modelFileName);
        final File modelFile = new File(this.testWorkingDir, modelFileName);
        FileUtils.copyURLToFile(scriptFile, modelFile);

        // Prepare FIS Job
        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(this.testWorkingDir.getAbsolutePath());
        when(job.getExecutionFile()).thenReturn(modelFileName);
        when(job.getId()).thenReturn("MIF_JOB_ID");
        
        final Archive archive = mockArchiveCreationAndConversion(this.testWorkingDir, modelFile);
        
        // When
        jobProcessor.process(job);

        // Then
        
        verify(this.converterToolboxService).isConversionSupported(mdlLanguage, pharmmlLanguage);
        verify(this.converterToolboxService, times(0)).convert(same(archive), same(mdlLanguage), same(pharmmlLanguage));
        verifyNoMoreInteractions(this.converterToolboxService);
        
        assertTrue("Conversion Report file exists", new File(this.testWorkingDir, ".fis/conversionReport.log").exists());
        
        final File phexFile = new File(new File(this.testWorkingDir, ".fis"), PHEX_ARCHIVE);
        assertTrue("Archive is created in FIS metadata directory.", phexFile.exists());
        verify(this.mockArchiveCreator).buildArchive(phexFile, modelFile, new ArrayList<File>());
        verifyNoMoreInteractions(this.mockArchiveCreator);
        
        // We expect 2 times, because there are two files - model and data - in mock conversion results
        verify(archive, times(2)).addFile(any(File.class), eq("/"));
        verify(jobArchiveProvisioner).provision(same(job), same(archive), eq(mifJobWorkingDir));
    }
    
    private Archive mockArchiveCreationAndConversion(final File fisWorkingDir, final File modelFile, final File ... extraInputFiles)
            throws IOException, ArchiveException, ConverterToolboxServiceException {
        
        final Archive archive = mock(Archive.class);
        
        final File phexFile = new File(new File(fisWorkingDir, ".fis"), PHEX_ARCHIVE);
        FileUtils.writeStringToFile(phexFile, "This is mock Phex file contents");
        
        when(this.mockArchiveCreator.buildArchive(phexFile, modelFile, Arrays.asList(extraInputFiles))).thenReturn(archive);

        // Mock a successful conversion
        final ConversionReport conversionReport = new ConversionReport();
        conversionReport.setReturnCode(ConversionReportOutcomeCode.SUCCESS);
        when(this.converterToolboxService.isConversionSupported(same(mdlLanguage), same(pharmmlLanguage))).thenReturn(true);
        when(this.converterToolboxService.convert(same(archive), same(mdlLanguage), same(pharmmlLanguage))).thenReturn(conversionReport);
        
        return archive;
    }
    
    private void verifyArchiveCreationAndConversionAndProvisioningToMIF(final File fisWorkingDir, final File modelFile,
            final LocalJob job, final Archive archive) throws ConverterToolboxServiceException, ArchiveException, IOException {
            
        final File phexFile = new File(new File(fisWorkingDir, ".fis"), PHEX_ARCHIVE);
        
        assertTrue("Archive is created in FIS metadata directory.", phexFile.exists());
        
        verify(this.mockArchiveCreator).buildArchive(phexFile, modelFile, job.getExtraInputFiles() == null ? null :
            CollectionUtils.collect(job.getExtraInputFiles(), new Transformer<String, File>() {

                @Override
                public File transform(final String filePath) {
                    return new File(filePath);
                }
            }));
        verifyNoMoreInteractions(this.mockArchiveCreator);
        
        verify(this.converterToolboxService).isConversionSupported(mdlLanguage, pharmmlLanguage);
        verify(this.converterToolboxService).convert(same(archive), same(mdlLanguage), same(pharmmlLanguage));
        verifyNoMoreInteractions(this.converterToolboxService);
        
        assertTrue("Conversion Report file exists", new File(fisWorkingDir, ".fis/conversionReport.log").exists());
        
        verify(this.jobArchiveProvisioner).provision(same(job), same(archive), eq(mifJobWorkingDir));
    }

}
