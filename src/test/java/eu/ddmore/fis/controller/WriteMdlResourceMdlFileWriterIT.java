/*******************************************************************************
 * Copyright (C) 2016 Mango Business Solutions Ltd, http://www.mango-solutions.com
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
 *******************************************************************************/
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
import eu.ddmore.fis.controllers.MdlFileWriter;
import eu.ddmore.fis.domain.WriteMdlRequest;
import eu.ddmore.fis.domain.WriteMdlResponse;
import eu.ddmore.fis.service.cts.ConverterToolboxService;
import eu.ddmore.fis.service.cts.ConverterToolboxServiceException;
import groovy.lang.Binding;


/**
 * Tests {@link FileProcessor}
 */
@RunWith(MockitoJUnitRunner.class)
public class WriteMdlResourceMdlFileWriterIT {
    private static final Logger LOG = Logger.getLogger(WriteMdlResourceMdlFileWriterIT.class);
    
    private static final String CONVERTER_SCRIPT = "/scripts/writeMdlResource.groovy";
    
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
    
    private MdlFileWriter fileWriter;
    
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
        this.binding.setVariable("fis.metadata.dir", ".fis");
        
        this.fileWriter = new MdlFileWriter(this.binding);
        fileWriter.setScriptFile(FileUtils.toFile(WriteMdlResourceMdlFileWriterIT.class.getResource(CONVERTER_SCRIPT)));
    }
    
    
    @Test
    public void shouldPerformConversionExtractTheResultFileAndDumpConversionReportToAFile() throws ConverterToolboxServiceException, IOException {
        //Given successful conversion
        File outputDir = this.testDirectory.newFolder();
        File fisMetadataDir = new File(outputDir, ".fis");
        Archive archive = mock(Archive.class);
        Entry resultEntry = mock(Entry.class);
        File resultEntryFile = new File("/mock/path/to/file.ext");
        File expectedResultFile = new File(outputDir,"test.mdl");
        when(resultEntry.extractFile(any(File.class))).thenReturn(expectedResultFile);
        when(resultEntry.getFileName()).thenReturn(resultEntryFile.getName());
        List<Entry> mainEntries = Lists.newArrayList(resultEntry);
        when(archive.getMainEntries()).thenReturn(mainEntries);
        when(archiveFactory.createArchive(any(File.class))).thenReturn(archive);
        
        ConversionReport conversionReport = new ConversionReport();
        conversionReport.setReturnCode(ConversionReportOutcomeCode.SUCCESS);
        when(converterToolboxService.isConversionSupported(same(jsonLanguage), same(mdlLanguage))).thenReturn(true);
        when(converterToolboxService.convert(any(Archive.class), same(jsonLanguage), same(mdlLanguage))).thenReturn(conversionReport);
        
        WriteMdlRequest request = new WriteMdlRequest();
        request.setFileContent("Dummy JSON file content");
        request.setFileName(expectedResultFile.getAbsolutePath());
        
        //when
        WriteMdlResponse response = fileWriter.process(request);
        
        //then
        assertEquals("Wrte Report contains 'successful' status.", "Successful", response.getStatus());
        assertTrue("Conversion Report file exists", new File(fisMetadataDir, "conversionReport.log").exists());
   }
    
    @Test
    public void shouldReturnEmptyResultForFailedConversion() throws IOException, ConverterToolboxServiceException {
        //Given failed conversion
        File outputDir = this.testDirectory.newFolder();
        File fisMetadataDir = new File(outputDir, ".fis");
        Archive archive = mock(Archive.class);
        Entry resultEntry = mock(Entry.class);
        File resultEntryFile = new File("/mock/path/to/file.ext");
        File expectedResultFile = new File(outputDir,"test.mdl");
        when(resultEntry.extractFile(eq(expectedResultFile))).thenReturn(expectedResultFile);
        when(resultEntry.getFileName()).thenReturn(resultEntryFile.getName());
        List<Entry> mainEntries = Lists.newArrayList(resultEntry);
        when(archive.getMainEntries()).thenReturn(mainEntries);
        when(archiveFactory.createArchive(any(File.class))).thenReturn(archive);
        
        ConversionReport conversionReport = new ConversionReport();
        conversionReport.setReturnCode(ConversionReportOutcomeCode.FAILURE);
        when(converterToolboxService.isConversionSupported(same(jsonLanguage), same(mdlLanguage))).thenReturn(true);
        when(converterToolboxService.convert(any(Archive.class), same(jsonLanguage), same(mdlLanguage))).thenReturn(conversionReport);

        WriteMdlRequest request = new WriteMdlRequest();
        request.setFileContent("Dummy JSON file content");
        request.setFileName(expectedResultFile.getAbsolutePath());
        //when
        WriteMdlResponse response = fileWriter.process(request);
        
        //then
        assertTrue("Wrte Report contains 'Failed' status.", response.getStatus().startsWith("Failed"));
        assertTrue("Conversion Report file exists", new File(fisMetadataDir, "conversionReport.log").exists());
    }
    
    @Test
    public void shouldReturnFailedStatusEvenIfExceptionIsThrown() throws IOException, ConverterToolboxServiceException {
        //Given conversion in error
        File outputDir = this.testDirectory.newFolder();
        Archive archive = mock(Archive.class);
        Entry resultEntry = mock(Entry.class);
        File resultEntryFile = new File("/mock/path/to/file.ext");
        File expectedResultFile = new File(outputDir,"test.mdl");
        when(resultEntry.extractFile(eq(expectedResultFile))).thenReturn(expectedResultFile);
        when(resultEntry.getFileName()).thenReturn(resultEntryFile.getName());
        List<Entry> mainEntries = Lists.newArrayList(resultEntry);
        when(archive.getMainEntries()).thenReturn(mainEntries);
        when(archiveFactory.createArchive(any(File.class))).thenReturn(archive);
        
        ConversionReport conversionReport = new ConversionReport();
        conversionReport.setReturnCode(ConversionReportOutcomeCode.FAILURE);
        when(converterToolboxService.isConversionSupported(same(jsonLanguage), same(mdlLanguage))).thenReturn(true);
        doThrow(ConverterToolboxServiceException.class).when(converterToolboxService).convert(any(Archive.class), same(jsonLanguage), same(mdlLanguage));

        WriteMdlRequest request = new WriteMdlRequest();
        request.setFileContent("Dummy JSON file content");
        request.setFileName(expectedResultFile.getAbsolutePath());
        //when
        WriteMdlResponse response = fileWriter.process(request);
        
        //then
        assertTrue("Wrte Report contains 'Failed' status.", response.getStatus().startsWith("Failed"));

    }
}
