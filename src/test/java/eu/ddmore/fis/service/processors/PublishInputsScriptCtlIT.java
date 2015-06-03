/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import eu.ddmore.archive.Archive;
import eu.ddmore.archive.ArchiveFactory;
import eu.ddmore.archive.Entry;
import eu.ddmore.archive.exception.ArchiveException;
import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.service.cts.ConverterToolboxServiceException;
import eu.ddmore.fis.service.processors.internal.JobArchiveProvisioner;
import groovy.lang.Binding;

/**
 * Integration Test for Publish CTL Inputs groovy script, that doesn't perform conversion.
 */
@RunWith(MockitoJUnitRunner.class)
public class PublishInputsScriptCtlIT {
    private static final Logger LOG = Logger.getLogger(PublishInputsScriptCtlIT.class);
    
    private static final String PUBLISH_CTL_INPUTS_SCRIPT = "/scripts/publishInputsCtl.groovy";
    
    private static final String PHEX_ARCHIVE = "archive.phex";
    
    @Rule
    public TemporaryFolder testDirectory = new TemporaryFolder();
    
    @Rule
    public TemporaryFolder otherTestDirectory = new TemporaryFolder();
    
    @Mock
    private ArchiveFactory archiveFactory;
    
    @Mock
    private JobArchiveProvisioner jobArchiveProvisioner;
    
    private File testWorkingDir;
    private File testExecutionHostFileshareLocal;
    private File mifJobWorkingDir;

    private Binding binding;

    private JobProcessor jobProcessor;
    
    private final static String TEST_DATA_DIR = "/test-models/NM-TRAN/7.2.0/Warfarin_ODE/";
    private final static String CONTROL_FILE_NAME = "Warfarin-ODE-latest.ctl";
    private final static String DATA_FILE_NAME = "warfarin_conc.csv";

    @Before
    public void setUp() {
        assertFalse("Ensuring that the two test directories created for the test, are different",
            this.testDirectory.getRoot().equals(this.otherTestDirectory.getRoot()));
            
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

    /**
     * Passing in a control file with a relative path, the Job Working Directory is used both
     * to resolve the control file against, and as the location in which to create the Archive.
     */
    @Test
    public void shouldPublishCTLInputs_WorkingDirectoryAbsPathAndControlFileRelPath()
            throws IOException, ConverterToolboxServiceException, ArchiveException {
    
        this.jobProcessor.setScriptFile(FileUtils.toFile(PublishInputsScriptCtlIT.class.getResource(PUBLISH_CTL_INPUTS_SCRIPT)));
        
        // Prepare FIS Job working directory
        final String ctlFileInSubDir = "warfarin" + File.separator + CONTROL_FILE_NAME;
        final String dataFileInSubDir = "warfarin" + File.separator + DATA_FILE_NAME;

        final File ctlFile = new File(this.testWorkingDir, ctlFileInSubDir);
        final File dataFile = new File(this.testWorkingDir, dataFileInSubDir);
        FileUtils.copyURLToFile(PublishInputsScriptCtlIT.class.getResource(TEST_DATA_DIR + CONTROL_FILE_NAME), ctlFile);
        FileUtils.copyURLToFile(PublishInputsScriptCtlIT.class.getResource(TEST_DATA_DIR + DATA_FILE_NAME), dataFile);
        
        // Prepare FIS Job
        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(this.testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn(ctlFileInSubDir);
        when(job.getId()).thenReturn("MIF_JOB_ID");
        
        final Archive archive = mockArchiveCreation(this.testWorkingDir, ctlFile, "/");
        
        // When
        jobProcessor.process(job);

        // Then
        verifyArchiveCreation(this.testWorkingDir, ctlFile, dataFile, "/", "/", archive);
        verify(this.jobArchiveProvisioner).provision(same(job), same(archive), eq(this.mifJobWorkingDir));
    }
    
    /**
     * Passing in a control file with a relative path, the Job Working Directory is used both
     * to resolve the control file against, and as the location in which to create the Archive.
     */
    @Test
    public void shouldPublishCTLInputs_WorkingDirectoryAbsPathAndControlFileRelPath_DataFileNotInSameDirAsControlFile()
            throws IOException, ConverterToolboxServiceException, ArchiveException {
    
        this.jobProcessor.setScriptFile(FileUtils.toFile(PublishInputsScriptCtlIT.class.getResource(PUBLISH_CTL_INPUTS_SCRIPT)));
        
        // Prepare FIS Job working directory
        final String ctlFileInSubDir = "models" + File.separator + CONTROL_FILE_NAME;
        final String dataFileInSubDir = "data" + File.separator + DATA_FILE_NAME;
        
        final File ctlFile = new File(this.testWorkingDir, ctlFileInSubDir);
        final File dataFile = new File(this.testWorkingDir, dataFileInSubDir);
        FileUtils.copyURLToFile(PublishInputsScriptCtlIT.class.getResource(TEST_DATA_DIR + CONTROL_FILE_NAME), ctlFile);
        FileUtils.copyURLToFile(PublishInputsScriptCtlIT.class.getResource(TEST_DATA_DIR + DATA_FILE_NAME), dataFile);
        
        // Manipulate the path to the data file within the control file
        FileUtils.write(ctlFile, FileUtils.readFileToString(ctlFile).replace(DATA_FILE_NAME, "../data/" + DATA_FILE_NAME));
        
        // Prepare FIS Job
        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(this.testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn(ctlFileInSubDir);
        when(job.getId()).thenReturn("MIF_JOB_ID");
        
        final Archive archive = mockArchiveCreation(this.testWorkingDir, ctlFile, "/models");
        
        // When
        jobProcessor.process(job);

        // Then
        verifyArchiveCreation(this.testWorkingDir, ctlFile, dataFile, "/models", "/data", archive);
        verify(this.jobArchiveProvisioner).provision(same(job), same(archive), eq(this.mifJobWorkingDir));
    }
    
    /**
     * Passing in a control file with an absolute path, the Job Working Directory becomes 'divorced'
     * from the control file and can thus point to any location. In this case, the working directory
     * is just used as the location in which to create the Archive.
     */
    @Test
    public void shouldPublishCTLInputs_ControlFileAbsPath()
            throws IOException, ConverterToolboxServiceException, ArchiveException {
    
        this.jobProcessor.setScriptFile(FileUtils.toFile(PublishInputsScriptCtlIT.class.getResource(PUBLISH_CTL_INPUTS_SCRIPT)));
        
        // Prepare FIS Job working directory
        final File ctlFile = new File(new File(this.testWorkingDir, "warfarin"), CONTROL_FILE_NAME);
        final File dataFile = new File(new File(this.testWorkingDir, "warfarin"), DATA_FILE_NAME);
        FileUtils.copyURLToFile(PublishInputsScriptCtlIT.class.getResource(TEST_DATA_DIR + CONTROL_FILE_NAME), ctlFile);
        FileUtils.copyURLToFile(PublishInputsScriptCtlIT.class.getResource(TEST_DATA_DIR + DATA_FILE_NAME), dataFile);
        
        // Control file with absolute path -> job working directory isn't used for
        // resolving the control file against so can point to some other directory
        final File fisWorkingDir = this.otherTestDirectory.getRoot();
        
        // Prepare FIS Job
        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(fisWorkingDir.getPath());
        when(job.getControlFile()).thenReturn(ctlFile.getAbsolutePath());
        when(job.getId()).thenReturn("MIF_JOB_ID");
        
        final Archive archive = mockArchiveCreation(this.testWorkingDir, ctlFile, "/");
        
        // When
        jobProcessor.process(job);

        // Then
        verifyArchiveCreation(this.testWorkingDir, ctlFile, dataFile, "/", "/", archive);
        verify(this.jobArchiveProvisioner).provision(same(job), same(archive), eq(this.mifJobWorkingDir));
    }
    
    /**
     * Passing in a control file with an absolute path, the Job Working Directory becomes 'divorced'
     * from the control file and can thus point to any location. In this case, the working directory
     * is just used as the location in which to create the Archive.
     */
    @Test
    public void shouldPublishCTLInputs_ControlFileAbsPath_DataFileNotInSameDirAsControlFile()
            throws IOException, ConverterToolboxServiceException, ArchiveException {
            
        this.jobProcessor.setScriptFile(FileUtils.toFile(PublishInputsScriptCtlIT.class.getResource(PUBLISH_CTL_INPUTS_SCRIPT)));
        
        // Prepare FIS Job working directory
        final File ctlFile = new File(new File(this.testWorkingDir, "models"), CONTROL_FILE_NAME);
        final File dataFile = new File(new File(this.testWorkingDir, "data"), DATA_FILE_NAME);
        FileUtils.copyURLToFile(PublishInputsScriptCtlIT.class.getResource(TEST_DATA_DIR + CONTROL_FILE_NAME), ctlFile);
        FileUtils.copyURLToFile(PublishInputsScriptCtlIT.class.getResource(TEST_DATA_DIR + DATA_FILE_NAME), dataFile);
        
        // Manipulate the path to the data file within the control file
        FileUtils.write(ctlFile, FileUtils.readFileToString(ctlFile).replace(DATA_FILE_NAME, "../data/" + DATA_FILE_NAME));
        
        // Model file with absolute path -> job working directory isn't used for
        // resolving the model file against so can point to some other directory
        final File fisWorkingDir = this.otherTestDirectory.getRoot();
        
        // Prepare FIS Job
        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(fisWorkingDir.getPath());
        when(job.getControlFile()).thenReturn(ctlFile.getAbsolutePath());
        when(job.getId()).thenReturn("MIF_JOB_ID");
        
        final Archive archive = mockArchiveCreation(this.testWorkingDir, ctlFile, "/models");
        
        // When
        jobProcessor.process(job);

        // Then
        verifyArchiveCreation(this.testWorkingDir, ctlFile, dataFile, "/models", "/data", archive);
        verify(this.jobArchiveProvisioner).provision(same(job), same(archive), eq(this.mifJobWorkingDir));
    }

    private Archive mockArchiveCreation(final File workingDir, final File controlFile, final String controlFileDirPathInArchive) throws IOException, ArchiveException {
    
        final Archive archive = mock(Archive.class);
        
        // Mock the archive creation behaviour
        final Entry mainEntry = mock(Entry.class);
        when(mainEntry.getFilePath()).thenReturn(controlFileDirPathInArchive);
        when(archive.addFile(controlFile, controlFileDirPathInArchive)).thenReturn(mainEntry);
        when(this.archiveFactory.createArchive(any(File.class))).then(new Answer<Archive>() {
            @Override
            public Archive answer(InvocationOnMock invocation) throws Throwable {
                File fisMetadataDir = new File(workingDir, ".fis");
                File phexFile = new File(fisMetadataDir, PHEX_ARCHIVE);
                FileUtils.writeStringToFile(phexFile, "This is mock Phex file contents");
                when(archive.getArchiveFile()).thenReturn(phexFile);
                return archive;
            }
        });

        return archive;
    }
    
    private void verifyArchiveCreation(final File workingDir,
            final File modelFile, final File dataFile, final String modelFileDirPathInArchive, final String dataFileDirPathInArchive,
            final Archive archive) throws ConverterToolboxServiceException, ArchiveException, IOException {
            
        assertTrue("Archive is created in FIS metadata directory.", new File(workingDir, ".fis/archive.phex").exists());
        verify(archive).open();
        final ArgumentCaptor<Entry> entryArgCaptor = ArgumentCaptor.forClass(Entry.class);
        verify(archive).addMainEntry(entryArgCaptor.capture());
        assertEquals("Checking that the mainEntry that was added to the Archive has the correct file path",
            modelFileDirPathInArchive, entryArgCaptor.getValue().getFilePath());
        verify(archive).addFile(modelFile, modelFileDirPathInArchive);
        verify(archive).addFile(dataFile, dataFileDirPathInArchive);
        verify(archive).close();
    }

}
