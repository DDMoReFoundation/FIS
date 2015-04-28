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
import eu.ddmore.fis.controllers.MdlConversionProcessor;
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
    
    private MdlConversionProcessor conversionProcessor;
    
    private File testWorkingDir;
    private Binding binding;
    
    @Before
    public void setUp() {
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
        
        this.conversionProcessor = new MdlConversionProcessor(this.binding);
        conversionProcessor.setScriptFile(FileUtils.toFile(MdlConversionProcessorIT.class.getResource(MDL_CONVERTER_SCRIPT)));
    }
    
    
    @Test
    public void shouldPerformConversionExtractTheResultFileAndDumpConversionReportToAFile() throws ConverterToolboxServiceException, IOException {
        //Given successful conversion
        File outputDir = this.testDirectory.newFolder();
        File fisMetadataDir = new File(outputDir, ".fis");
        
        Archive archive = mock(Archive.class);
        Entry resultEntry = mock(Entry.class);
        File resultEntryFile = new File("/mock/path/to/file.ext");
        File expectedResultFile = new File(outputDir,"file.ext");
        when(resultEntry.extractFile(eq(expectedResultFile))).thenReturn(expectedResultFile);
        when(resultEntry.getFileName()).thenReturn(resultEntryFile.getName());
        List<Entry> mainEntries = Lists.newArrayList(resultEntry);
        when(archive.getMainEntries()).thenReturn(mainEntries);
        when(archiveFactory.createArchive(any(File.class))).thenReturn(archive);
        
        File mdlFile = new File(outputDir, "test.mdl");
        FileUtils.writeStringToFile(mdlFile, "This is mock MDL file contents");
        
        ConversionReport conversionReport = new ConversionReport();
        conversionReport.setReturnCode(ConversionReportOutcomeCode.SUCCESS);
        when(converterToolboxService.convert(any(Archive.class), same(mdlLanguage), same(pharmmlLanguage))).thenReturn(conversionReport);
        
        //when
        String resultFile = conversionProcessor.process(mdlFile.getAbsolutePath(), outputDir.getAbsolutePath());
        
        //then
        assertTrue("Conversion Report file exists", new File(fisMetadataDir, "conversionReport.log").exists());
        assertTrue("Conversion Result file path is not blank.", StringUtils.isNotBlank(resultFile));
        assertEquals("The result file name is as expected.", expectedResultFile.getAbsolutePath(),resultFile);
   }
    
    @Test
    public void shouldReturnEmptyResultForFailedConversion() throws IOException, ConverterToolboxServiceException {
        //Given failed conversion
        File outputDir = this.testDirectory.newFolder();
        File fisMetadataDir = new File(outputDir, ".fis");
        
        Archive archive = mock(Archive.class);
        Entry resultEntry = mock(Entry.class);
        File resultEntryFile = new File("/mock/path/to/file.ext");
        File expectedResultFile = new File(outputDir,"file.ext");
        when(resultEntry.extractFile(eq(expectedResultFile))).thenReturn(expectedResultFile);
        when(resultEntry.getFileName()).thenReturn(resultEntryFile.getName());
        List<Entry> mainEntries = Lists.newArrayList(resultEntry);
        when(archive.getMainEntries()).thenReturn(mainEntries);
        when(archiveFactory.createArchive(any(File.class))).thenReturn(archive);
        
        File mdlFile = new File(outputDir, "test.mdl");
        FileUtils.writeStringToFile(mdlFile, "This is mock MDL file contents");
        
        ConversionReport conversionReport = new ConversionReport();
        conversionReport.setReturnCode(ConversionReportOutcomeCode.FAILURE);
        when(converterToolboxService.convert(any(Archive.class), same(mdlLanguage), same(pharmmlLanguage))).thenReturn(conversionReport);
        
        //when
        String resultFile = conversionProcessor.process(mdlFile.getAbsolutePath(), outputDir.getAbsolutePath());
        
        //then
        assertTrue("Conversion Report file exists", new File(fisMetadataDir, "conversionReport.log").exists());
        assertTrue("Conversion Result file path is blank.", StringUtils.isBlank(resultFile));
    }
    
    @Test(expected=RuntimeException.class)
    public void shouldReThrowExceptionIfErrorHappensAsRuntimeException() throws IOException, ConverterToolboxServiceException {
        //Given conversion in error
        File outputDir = this.testDirectory.newFolder();
        
        Archive archive = mock(Archive.class);
        Entry resultEntry = mock(Entry.class);
        File resultEntryFile = new File("/mock/path/to/file.ext");
        File expectedResultFile = new File(outputDir,"file.ext");
        when(resultEntry.extractFile(eq(expectedResultFile))).thenReturn(expectedResultFile);
        when(resultEntry.getFileName()).thenReturn(resultEntryFile.getName());
        List<Entry> mainEntries = Lists.newArrayList(resultEntry);
        when(archive.getMainEntries()).thenReturn(mainEntries);
        when(archiveFactory.createArchive(any(File.class))).thenReturn(archive);
        
        File mdlFile = new File(outputDir, "test.mdl");
        FileUtils.writeStringToFile(mdlFile, "This is mock MDL file contents");
        
        ConversionReport conversionReport = new ConversionReport();
        conversionReport.setReturnCode(ConversionReportOutcomeCode.FAILURE);
        doThrow(ConverterToolboxServiceException.class).when(converterToolboxService).convert(any(Archive.class), same(mdlLanguage), same(pharmmlLanguage));
        
        //when
        conversionProcessor.process(mdlFile.getAbsolutePath(), outputDir.getAbsolutePath());
    }
}
