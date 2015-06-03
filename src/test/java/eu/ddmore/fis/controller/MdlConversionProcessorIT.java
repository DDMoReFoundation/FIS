/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

import eu.ddmore.archive.Archive;
import eu.ddmore.archive.ArchiveFactory;
import eu.ddmore.archive.Entry;
import eu.ddmore.archive.exception.ArchiveException;
import eu.ddmore.convertertoolbox.domain.ConversionReport;
import eu.ddmore.convertertoolbox.domain.ConversionReportOutcomeCode;
import eu.ddmore.convertertoolbox.domain.LanguageVersion;
import eu.ddmore.fis.controllers.MdlConversionProcessor;
import eu.ddmore.fis.controllers.utils.MdlUtils;
import eu.ddmore.fis.service.cts.ConverterToolboxService;
import eu.ddmore.fis.service.cts.ConverterToolboxServiceException;
import groovy.lang.Binding;


/**
 * Tests {@link MdlConversionProcessor}
 */
@RunWith(MockitoJUnitRunner.class)
public class MdlConversionProcessorIT {
    private static final Logger LOG = Logger.getLogger(MdlConversionProcessorIT.class);
    
    private static final String MDL_CONVERTER_SCRIPT = "/scripts/mdlConverter.groovy";
    
    private static final String PHEX_ARCHIVE = "archive.phex";
    
    @Rule
    public TemporaryFolder testDirectory = new TemporaryFolder();
    
    private File outputDir;
    private File expectedResultFile; 

    @Mock
    private ConverterToolboxService converterToolboxService;
    
    @Mock
    private LanguageVersion mdlLanguage;
    
    @Mock
    private LanguageVersion pharmmlLanguage;
    
    @Mock
    private ArchiveFactory archiveFactory;
    
    private MdlConversionProcessor conversionProcessor;
    
    private File testWorkingDir;
    private Binding binding;
    
    @Before
    public void setUp() throws IOException {
        this.testWorkingDir = this.testDirectory.getRoot();
        LOG.debug(String.format("Test working dir %s", this.testWorkingDir));
        
        this.binding = new Binding();
        this.binding.setVariable("converterToolboxService",converterToolboxService);
        this.binding.setVariable("mdlLanguage",mdlLanguage);
        this.binding.setVariable("pharmmlLanguage",pharmmlLanguage);
        this.binding.setVariable("archiveFactory",archiveFactory);
        this.binding.setVariable("fis.cts.output.conversionReport", "conversionReport.log");
        this.binding.setVariable("fis.cts.output.archive", "archive.phex");
        this.binding.setVariable("fis.metadata.dir", ".fis");
        
        MdlUtils mdlUtils = mock(MdlUtils.class);
        List<File> emptyResult = Lists.newArrayList();
        when(mdlUtils.getDataFileFromMDL(any(File.class))).thenReturn(emptyResult);
        this.binding.setVariable("mdlUtils", mdlUtils);
        
        this.outputDir = this.testDirectory.newFolder();
        this.expectedResultFile = new File(this.outputDir,"file.ext");
        
        this.conversionProcessor = new MdlConversionProcessor(this.binding);
        conversionProcessor.setScriptFile(FileUtils.toFile(MdlConversionProcessorIT.class.getResource(MDL_CONVERTER_SCRIPT)));
    }
    
    @Test
    public void shouldPerformConversionExtractTheResultFileAndDumpConversionReportToAFile() throws ConverterToolboxServiceException, IOException, ArchiveException {
        //Given successful conversion
        File fisMetadataDir = new File(this.outputDir, ".fis");
        
        File mdlFile = new File(outputDir, "test.mdl");
        FileUtils.writeStringToFile(mdlFile, "This is mock MDL file contents");
        
        final Archive archive = mockArchiveCreationAndConversion(fisMetadataDir, mdlFile, "/");
        
        //when
        String resultFile = conversionProcessor.process(mdlFile.getAbsolutePath(), this.outputDir.getAbsolutePath());
        
        //then
        verifyArchiveCreationAndConversion(this.outputDir, mdlFile, "/", archive);
        assertTrue("Conversion Result file path is not blank.", StringUtils.isNotBlank(resultFile));
        assertEquals("The result file name is as expected", this.expectedResultFile.getAbsolutePath(), resultFile);
    }
    
    @Test
    public void shouldReturnEmptyResultForFailedConversion() throws IOException, ConverterToolboxServiceException, ArchiveException {
        //Given failed conversion
        File outputDir = this.testDirectory.newFolder();
        File fisMetadataDir = new File(outputDir, ".fis");
        
        File mdlFile = new File(outputDir, "test.mdl");
        FileUtils.writeStringToFile(mdlFile, "This is mock MDL file contents");
        
        // Mock the archive creation behaviour
        final Archive archive = mock(Archive.class);
        final Entry mainEntry = mock(Entry.class);
        when(mainEntry.getFilePath()).thenReturn("/");
        when(archive.addFile(mdlFile, "/")).thenReturn(mainEntry);
        when(this.archiveFactory.createArchive(any(File.class))).thenReturn(archive);
        
        ConversionReport conversionReport = new ConversionReport();
        conversionReport.setReturnCode(ConversionReportOutcomeCode.FAILURE);
        when(converterToolboxService.isConversionSupported(same(mdlLanguage), same(pharmmlLanguage))).thenReturn(true);
        when(converterToolboxService.convert(any(Archive.class), same(mdlLanguage), same(pharmmlLanguage))).thenReturn(conversionReport);
        
        //when
        String resultFile = conversionProcessor.process(mdlFile.getAbsolutePath(), outputDir.getAbsolutePath());
        
        //then
        verify(this.converterToolboxService).convert(same(archive), same(mdlLanguage), same(pharmmlLanguage));
        assertTrue("Conversion Report file exists", new File(fisMetadataDir, "conversionReport.log").exists());
        //assertTrue("Archive is created in FIS metadata directory.", new File(fisMetadataDir, ".fis/archive.phex").exists()); // No archive is physically created by these tests
        verify(archive, times(2)).open(); // once for archive creation, once for conversion execution
        final ArgumentCaptor<Entry> entryArgCaptor = ArgumentCaptor.forClass(Entry.class);
        verify(archive).addMainEntry(entryArgCaptor.capture());
        assertEquals("Checking that the mainEntry that was added to the Archive has the correct file path",
            "/", entryArgCaptor.getValue().getFilePath());
        verify(archive).addFile(mdlFile, "/");
        verify(archive, times(2)).close(); // once for archive creation, once for conversion execution
        // The crucial assertion
        assertTrue("Conversion Result file path is blank", StringUtils.isBlank(resultFile));
    }
    
    @Test(expected=RuntimeException.class)
    public void shouldReThrowExceptionIfRuntimeExceptionOccurs() throws IOException, ConverterToolboxServiceException, ArchiveException {
        //Given conversion in error
        File outputDir = this.testDirectory.newFolder();
        
        File mdlFile = new File(outputDir, "test.mdl");
        FileUtils.writeStringToFile(mdlFile, "This is mock MDL file contents");
        
        // Mock the archive creation behaviour
        final Archive archive = mock(Archive.class);
        final Entry mainEntry = mock(Entry.class);
        when(mainEntry.getFilePath()).thenReturn("/");
        when(archive.addFile(mdlFile, "/")).thenReturn(mainEntry);
        when(this.archiveFactory.createArchive(any(File.class))).thenReturn(archive);
        
        ConversionReport conversionReport = new ConversionReport();
        conversionReport.setReturnCode(ConversionReportOutcomeCode.FAILURE);
        when(converterToolboxService.isConversionSupported(same(mdlLanguage), same(pharmmlLanguage))).thenReturn(true);
        doThrow(ConverterToolboxServiceException.class).when(converterToolboxService).convert(any(Archive.class), same(mdlLanguage), same(pharmmlLanguage));
        
        //when
        conversionProcessor.process(mdlFile.getAbsolutePath(), outputDir.getAbsolutePath());
    }
    
    @Test(expected=RuntimeException.class)
    public void shouldThrowRuntimeExceptionIfConversionIsNotSupported() throws IOException, ConverterToolboxServiceException, ArchiveException {
        //Given conversion in error
        File outputDir = this.testDirectory.newFolder();
        
        File mdlFile = new File(outputDir, "test.mdl");
        FileUtils.writeStringToFile(mdlFile, "This is mock MDL file contents");
        
        // Mock the archive creation behaviour
        final Archive archive = mock(Archive.class);
        final Entry mainEntry = mock(Entry.class);
        when(mainEntry.getFilePath()).thenReturn("/");
        when(archive.addFile(mdlFile, "/")).thenReturn(mainEntry);
        when(this.archiveFactory.createArchive(any(File.class))).thenReturn(archive);
        
        ConversionReport conversionReport = new ConversionReport();
        conversionReport.setReturnCode(ConversionReportOutcomeCode.FAILURE);
        when(converterToolboxService.isConversionSupported(same(mdlLanguage), same(pharmmlLanguage))).thenReturn(false);
        
        //when
        conversionProcessor.process(mdlFile.getAbsolutePath(), outputDir.getAbsolutePath());
    }
    
    private Archive mockArchiveCreationAndConversion(final File workingDir, final File modelFile, final String modelFileDirPathInArchive)
            throws IOException, ArchiveException, ConverterToolboxServiceException {
        
        final Archive archive = mock(Archive.class);
        
        // Mock the archive creation behaviour
        final Entry mainEntry = mock(Entry.class);
        when(mainEntry.getFilePath()).thenReturn(modelFileDirPathInArchive);
        when(archive.addFile(modelFile, modelFileDirPathInArchive)).thenReturn(mainEntry);
        when(this.archiveFactory.createArchive(any(File.class))).thenReturn(archive);
        
        // Mock a successful conversion
        final ConversionReport conversionReport = new ConversionReport();
        conversionReport.setReturnCode(ConversionReportOutcomeCode.SUCCESS);
        when(this.converterToolboxService.isConversionSupported(same(mdlLanguage), same(pharmmlLanguage))).thenReturn(true);
        when(this.converterToolboxService.convert(same(archive), same(mdlLanguage), same(pharmmlLanguage))).thenReturn(conversionReport);
        
        final Entry resultEntry = mock(Entry.class);
        final File resultEntryFile = new File("/mock/path/to/file.ext");
        when(resultEntry.extractFile(eq(this.expectedResultFile))).thenReturn(this.expectedResultFile);
        when(resultEntry.getFileName()).thenReturn(resultEntryFile.getName());
        final List<Entry> mainEntries = Lists.newArrayList(resultEntry);
        when(archive.getMainEntries()).thenReturn(mainEntries);
        
        return archive;
    }
    
    private void verifyArchiveCreationAndConversion(final File workingDir,
            final File modelFile, final String modelFileDirPathInArchive, final Archive archive)
                    throws ConverterToolboxServiceException, ArchiveException, IOException {
                    
        verify(this.converterToolboxService).convert(same(archive), same(mdlLanguage), same(pharmmlLanguage));
        assertTrue("Conversion Report file exists", new File(workingDir, ".fis/conversionReport.log").exists());
        //assertTrue("Archive is created in FIS metadata directory.", new File(workingDir, ".fis/archive.phex").exists()); // No archive is physically created by these tests
        verify(archive, times(2)).open(); // once for archive creation, once for conversion execution
        final ArgumentCaptor<Entry> entryArgCaptor = ArgumentCaptor.forClass(Entry.class);
        verify(archive).addMainEntry(entryArgCaptor.capture());
        assertEquals("Checking that the mainEntry that was added to the Archive has the correct file path",
            modelFileDirPathInArchive, entryArgCaptor.getValue().getFilePath());
        verify(archive).addFile(modelFile, modelFileDirPathInArchive);
        verify(archive, times(2)).close(); // once for archive creation, once for conversion execution
        verify(archive, times(2)).getMainEntries();
    }
    
}
