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
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

import eu.ddmore.archive.Archive;
import eu.ddmore.archive.ArchiveFactory;
import eu.ddmore.archive.Entry;
import eu.ddmore.convertertoolbox.domain.ConversionReport;
import eu.ddmore.convertertoolbox.domain.ConversionReportOutcomeCode;
import eu.ddmore.convertertoolbox.domain.LanguageVersion;
import eu.ddmore.fis.controllers.FileProcessor;
import eu.ddmore.fis.service.cts.ConverterToolboxService;
import eu.ddmore.fis.service.cts.ConverterToolboxServiceException;
import groovy.lang.Binding;


/**
 * Tests {@link FileProcessor}
 */
@RunWith(MockitoJUnitRunner.class)
public class ReadMdlResourceFileProcessorIT {
    private static final Logger LOG = Logger.getLogger(ReadMdlResourceFileProcessorIT.class);
    
    private static final String CONVERTER_SCRIPT = "/scripts/readMdlResource.groovy";
    
    @Rule
    public TemporaryFolder testDirectory = new TemporaryFolder();

    @Mock
    private ConverterToolboxService converterToolboxService;
    
    @Mock
    private LanguageVersion mdlLanguage;
    
    @Mock
    private LanguageVersion jsonLanguage;
    
    @Mock
    private ArchiveFactory archiveFactory;
    
    private FileProcessor fileProcessor;
    
    private File testWorkingDir;
    private Binding binding;
    
    @Before
    public void setUp() {
        this.testWorkingDir = this.testDirectory.getRoot();
        LOG.debug(String.format("Test working dir %s", this.testWorkingDir));
        this.binding = new Binding();
        this.binding.setVariable("converterToolboxService",converterToolboxService);
        this.binding.setVariable("mdlLanguage",mdlLanguage);
        this.binding.setVariable("jsonLanguage",jsonLanguage);
        this.binding.setVariable("archiveFactory",archiveFactory);
        this.binding.setVariable("fis.cts.output.conversionReport", "conversionReport.log");
        this.binding.setVariable("fis.cts.output.archive", "archive.phex");
        
        this.fileProcessor = new FileProcessor(this.binding);
        fileProcessor.setScriptFile(FileUtils.toFile(ReadMdlResourceFileProcessorIT.class.getResource(CONVERTER_SCRIPT)));
    }
    
    
    @Test
    public void shouldPerformConversionExtractTheResultFileAndDumpConversionReportToAFile() throws ConverterToolboxServiceException, IOException {
        //Given successful conversion
        File outputDir = this.testDirectory.newFolder();
        File fisMetadataDir = new File(outputDir, ".fis");
        Archive archive = mock(Archive.class);
        Entry resultEntry = mock(Entry.class);
        File resultEntryFile = new File("/mock/path/to/file.ext");
        File expectedResultFile = new File(fisMetadataDir,"file.ext");
        when(resultEntry.extractFile(eq(expectedResultFile))).thenReturn(expectedResultFile);
        when(resultEntry.getFileName()).thenReturn(resultEntryFile.getName());
        List<Entry> mainEntries = Lists.newArrayList(resultEntry);
        when(archive.getMainEntries()).thenReturn(mainEntries);
        when(archiveFactory.createArchive(any(File.class))).thenReturn(archive);
        
        File mdlFile = new File(outputDir, "test.mdl");
        FileUtils.writeStringToFile(mdlFile, "This is mock MDL file contents");
        File jsonFile = new File(fisMetadataDir, "file.ext");
        FileUtils.writeStringToFile(jsonFile, "This is mock JSON file contents");
        
        ConversionReport conversionReport = new ConversionReport();
        conversionReport.setReturnCode(ConversionReportOutcomeCode.SUCCESS);
        when(converterToolboxService.convert(any(Archive.class), same(mdlLanguage), same(jsonLanguage))).thenReturn(conversionReport);
        
        //when
        String resultFileContent = fileProcessor.process(mdlFile.getAbsolutePath());
        
        //then
        assertTrue("Conversion Report file exists", new File(fisMetadataDir, "conversionReport.log").exists());
        assertTrue("Conversion Result file path is not blank.", StringUtils.isNotBlank(resultFileContent));
        assertEquals("The result file name is as expected.", "This is mock JSON file contents",resultFileContent);
   }
    
    @Test
    public void shouldReturnEmptyResultForFailedConversion() throws IOException, ConverterToolboxServiceException {
        //Given failed conversion
        File outputDir = this.testDirectory.newFolder();
        File fisMetadataDir = new File(outputDir, ".fis");
        Archive archive = mock(Archive.class);
        Entry resultEntry = mock(Entry.class);
        File resultEntryFile = new File("/mock/path/to/file.ext");
        File expectedResultFile = new File(fisMetadataDir,"file.ext");
        when(resultEntry.extractFile(eq(expectedResultFile))).thenReturn(expectedResultFile);
        when(resultEntry.getFileName()).thenReturn(resultEntryFile.getName());
        List<Entry> mainEntries = Lists.newArrayList(resultEntry);
        when(archive.getMainEntries()).thenReturn(mainEntries);
        when(archiveFactory.createArchive(any(File.class))).thenReturn(archive);
        
        File mdlFile = new File(outputDir, "test.mdl");
        FileUtils.writeStringToFile(mdlFile, "This is mock MDL file contents");
        
        ConversionReport conversionReport = new ConversionReport();
        conversionReport.setReturnCode(ConversionReportOutcomeCode.FAILURE);
        when(converterToolboxService.convert(any(Archive.class), same(mdlLanguage), same(jsonLanguage))).thenReturn(conversionReport);
        
        //when
        String resultFileContent = fileProcessor.process(mdlFile.getAbsolutePath());
        
        //then
        assertTrue("Conversion Result is blank.", StringUtils.isBlank(resultFileContent));
        assertTrue("Conversion Report file exists", new File(fisMetadataDir, "conversionReport.log").exists());
    }
    
    @Test(expected=RuntimeException.class)
    public void shouldReThrowExceptionIfErrorHappensAsRuntimeException() throws IOException, ConverterToolboxServiceException {
        //Given conversion in error
        File outputDir = this.testDirectory.newFolder();
        Archive archive = mock(Archive.class);
        Entry resultEntry = mock(Entry.class);
        File resultEntryFile = new File("/mock/path/to/file.ext");
        File expectedResultFile = new File(new File(outputDir, ".fis"),"file.ext");
        when(resultEntry.extractFile(eq(expectedResultFile))).thenReturn(expectedResultFile);
        when(resultEntry.getFileName()).thenReturn(resultEntryFile.getName());
        List<Entry> mainEntries = Lists.newArrayList(resultEntry);
        when(archive.getMainEntries()).thenReturn(mainEntries);
        when(archiveFactory.createArchive(any(File.class))).thenReturn(archive);
        
        File mdlFile = new File(outputDir, "test.mdl");
        FileUtils.writeStringToFile(mdlFile, "This is mock MDL file contents");
        
        ConversionReport conversionReport = new ConversionReport();
        conversionReport.setReturnCode(ConversionReportOutcomeCode.FAILURE);
        doThrow(ConverterToolboxServiceException.class).when(converterToolboxService).convert(any(Archive.class), same(mdlLanguage), same(jsonLanguage));
        
        //when
        fileProcessor.process(mdlFile.getAbsolutePath());
    }
}
