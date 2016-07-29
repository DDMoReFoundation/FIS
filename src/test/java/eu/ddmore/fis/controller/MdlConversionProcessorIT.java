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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
import eu.ddmore.archive.Entry;
import eu.ddmore.archive.exception.ArchiveException;
import eu.ddmore.convertertoolbox.domain.ConversionReport;
import eu.ddmore.convertertoolbox.domain.ConversionReportOutcomeCode;
import eu.ddmore.convertertoolbox.domain.LanguageVersion;
import eu.ddmore.fis.controllers.MdlConversionProcessor;
import eu.ddmore.fis.controllers.utils.ArchiveCreator;
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
    private ArchiveCreator mockArchiveCreator;
    
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
        this.binding.setVariable("fis.cts.output.conversionReport", "conversionReport.log");
        this.binding.setVariable("fis.cts.output.archive", PHEX_ARCHIVE);
        this.binding.setVariable("fis.metadata.dir", ".fis");
        
        this.binding.setVariable("archiveCreator", this.mockArchiveCreator);
        
        this.outputDir = this.testDirectory.newFolder();
        this.expectedResultFile = new File(this.outputDir,"file.ext");
        
        this.conversionProcessor = new MdlConversionProcessor(this.binding);
        
        this.conversionProcessor.setScriptFile(FileUtils.toFile(MdlConversionProcessorIT.class.getResource(MDL_CONVERTER_SCRIPT)));
    }
    
    @Test
    public void shouldPerformConversionExtractTheResultFileAndDumpConversionReportToAFile() throws ConverterToolboxServiceException, IOException, ArchiveException {
    
        //Given successful conversion
        File mdlFile = new File(outputDir, "test.mdl");
        FileUtils.writeStringToFile(mdlFile, "This is mock MDL file contents");
        
        final Archive archive = mockArchiveCreationAndConversion(this.outputDir, mdlFile);
        
        //when
        String resultFile = conversionProcessor.process(mdlFile.getAbsolutePath(), this.outputDir.getAbsolutePath());
        
        //then
        verifyArchiveCreationAndConversion(this.outputDir, mdlFile, archive);
        assertTrue("Conversion Result file path is not blank", StringUtils.isNotBlank(resultFile));
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
        final File phexFile = new File(fisMetadataDir, PHEX_ARCHIVE);
        when(this.mockArchiveCreator.buildArchive(phexFile, mdlFile)).thenReturn(archive);
        
        // Mock a failed conversion
        final ConversionReport conversionReport = new ConversionReport();
        conversionReport.setReturnCode(ConversionReportOutcomeCode.FAILURE);
        when(this.converterToolboxService.isConversionSupported(same(mdlLanguage), same(pharmmlLanguage))).thenReturn(true);
        when(this.converterToolboxService.convert(same(archive), same(mdlLanguage), same(pharmmlLanguage))).thenReturn(conversionReport);
        
        //when
        try {
            conversionProcessor.process(mdlFile.getAbsolutePath(), outputDir.getAbsolutePath());
        } catch(RuntimeException ex) {
            assertTrue("Type of the cause exception should be IllegalStateException", ex.getCause() instanceof IllegalStateException);
        }
        
        //then
        
        verify(this.converterToolboxService).isConversionSupported(same(mdlLanguage), same(pharmmlLanguage));
        verify(this.converterToolboxService).convert(same(archive), same(mdlLanguage), same(pharmmlLanguage));
        verifyNoMoreInteractions(this.converterToolboxService);
        assertTrue("Conversion Report file exists", new File(fisMetadataDir, "conversionReport.log").exists());
        
        verify(this.mockArchiveCreator).buildArchive(new File(fisMetadataDir, PHEX_ARCHIVE), mdlFile);
        verifyNoMoreInteractions(this.mockArchiveCreator);
        
    }
    
    @Test(expected=RuntimeException.class)
    public void shouldReThrowExceptionIfRuntimeExceptionOccurs() throws IOException, ConverterToolboxServiceException, ArchiveException {
    
        //Given conversion in error
        
        File outputDir = this.testDirectory.newFolder();
        File fisMetadataDir = new File(outputDir, ".fis");
        
        File mdlFile = new File(outputDir, "test.mdl");
        FileUtils.writeStringToFile(mdlFile, "This is mock MDL file contents");
        
        // Mock the archive creation behaviour
        final Archive archive = mock(Archive.class);
        final File phexFile = new File(fisMetadataDir, PHEX_ARCHIVE);
        when(this.mockArchiveCreator.buildArchive(phexFile, mdlFile)).thenReturn(archive);
        
        // Mock a failed conversion
        final ConversionReport conversionReport = new ConversionReport();
        conversionReport.setReturnCode(ConversionReportOutcomeCode.FAILURE);
        when(this.converterToolboxService.isConversionSupported(same(mdlLanguage), same(pharmmlLanguage))).thenReturn(true);
        doThrow(ConverterToolboxServiceException.class).when(this.converterToolboxService).convert(any(Archive.class), same(mdlLanguage), same(pharmmlLanguage));
        
        //when
        conversionProcessor.process(mdlFile.getAbsolutePath(), outputDir.getAbsolutePath());
    }
    
    @Test(expected=RuntimeException.class)
    public void shouldThrowRuntimeExceptionIfConversionIsNotSupported() throws IOException, ConverterToolboxServiceException, ArchiveException {
    
        //Given conversion in error
        
        File outputDir = this.testDirectory.newFolder();
        File fisMetadataDir = new File(outputDir, ".fis");
        
        File mdlFile = new File(outputDir, "test.mdl");
        FileUtils.writeStringToFile(mdlFile, "This is mock MDL file contents");
        
        // Mock the archive creation behaviour
        final Archive archive = mock(Archive.class);
        final File phexFile = new File(fisMetadataDir, PHEX_ARCHIVE);
        when(this.mockArchiveCreator.buildArchive(phexFile, mdlFile)).thenReturn(archive);
        
        ConversionReport conversionReport = new ConversionReport();
        conversionReport.setReturnCode(ConversionReportOutcomeCode.FAILURE);
        when(converterToolboxService.isConversionSupported(same(mdlLanguage), same(pharmmlLanguage))).thenReturn(false);
        
        //when
        conversionProcessor.process(mdlFile.getAbsolutePath(), outputDir.getAbsolutePath());
    }
    
    private Archive mockArchiveCreationAndConversion(final File workingDir, final File modelFile)
            throws IOException, ArchiveException, ConverterToolboxServiceException {
        
        final Archive archive = mock(Archive.class);
        
        final File phexFile = new File(new File(workingDir, ".fis"), PHEX_ARCHIVE);
        
        when(this.mockArchiveCreator.buildArchive(phexFile, modelFile)).thenReturn(archive);
        
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
    
    private void verifyArchiveCreationAndConversion(final File workingDir, final File modelFile, final Archive archive)
                    throws ConverterToolboxServiceException, ArchiveException, IOException {
                    
        verify(this.converterToolboxService).isConversionSupported(same(mdlLanguage), same(pharmmlLanguage));
        verify(this.converterToolboxService).convert(same(archive), same(mdlLanguage), same(pharmmlLanguage));
        verifyNoMoreInteractions(this.converterToolboxService);
        assertTrue("Conversion Report file exists", new File(workingDir, ".fis/conversionReport.log").exists());
        
        verify(this.mockArchiveCreator).buildArchive(new File(workingDir, ".fis/" + PHEX_ARCHIVE), modelFile);
        verifyNoMoreInteractions(this.mockArchiveCreator);
        verify(archive).open(); // Just once, for conversion execution, since archive creation is mocked out
        verify(archive, times(2)).getMainEntries();
        verify(archive).close(); // Just once, for conversion execution, since archive creation is mocked out
    }
    
}
