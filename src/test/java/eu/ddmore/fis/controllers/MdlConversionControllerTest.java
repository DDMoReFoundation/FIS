/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests {@link MdlConversionController}
 */
@RunWith(MockitoJUnitRunner.class) 
public class MdlConversionControllerTest {

    private static final String TEST_DATA_DIR = "/test-models/MDL_with_mock_PharmML/";
    private static final String MOCK_OUTPUT_DIR = "/MockOutputDir/";
    private static final String MDL_FILE_NAME = "MockGeneratedPharmML.mdl";
    private static final String PHARMML_FILE_NAME = "UseCase1.xml";

    private static final String INVALID_TEST_DATA_DIR = "INVALID_TEST_DATA_DIR";

    @InjectMocks
    private MdlConversionController mdlConversionController = new MdlConversionController();

    @Mock
    private MdlConversionProcessor mockMdlConversionProcessor;
    
    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowExceptionIfFilePathNotSet() {

        final String filePath = null;
        final String outputDir = MOCK_OUTPUT_DIR;

        this.mdlConversionController.convertMdlToPharmML(filePath, outputDir);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowExceptionIfOutputDirNotSet() {

        final String filePath = MdlConversionControllerTest.class.getResource(TEST_DATA_DIR + MDL_FILE_NAME).toString();
        final String outputDir = null;

        this.mdlConversionController.convertMdlToPharmML(filePath, outputDir);
    }

    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowExceptionIfFilePathDoesNotExist() {

        final String filePath = INVALID_TEST_DATA_DIR;
        final String outputDir = MOCK_OUTPUT_DIR;

        this.mdlConversionController.convertMdlToPharmML(filePath, outputDir);
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowExceptionIfConversionFails() {

    	final String filePath = MdlConversionControllerTest.class.getResource(TEST_DATA_DIR + MDL_FILE_NAME).getPath();
        final String outputDir = MOCK_OUTPUT_DIR;

        final Throwable ise = new IllegalStateException("Requested conversion is not supported by Converter Toolbox Service.", new Exception("Cause"));
        when(this.mockMdlConversionProcessor.process(same(filePath), same(outputDir))).thenThrow(ise);

        this.mdlConversionController.convertMdlToPharmML(filePath, outputDir);

        verify(this.mockMdlConversionProcessor).process(same(filePath), same(outputDir));
        verifyNoMoreInteractions(this.mockMdlConversionProcessor);
    }

    @Test
    public void shouldSuccessfullyConvertMdlFileAndBeCalledJustOnce() {

    	final String filePath = MdlConversionControllerTest.class.getResource(TEST_DATA_DIR + MDL_FILE_NAME).getPath();
        final String outputDir = MOCK_OUTPUT_DIR;

        when(this.mockMdlConversionProcessor.process(same(filePath), same(outputDir))).thenReturn(MOCK_OUTPUT_DIR + PHARMML_FILE_NAME);

        final String outputFilePath = this.mdlConversionController.convertMdlToPharmML(filePath, outputDir);

        assertEquals("Output File Path should be correct", MOCK_OUTPUT_DIR + PHARMML_FILE_NAME, outputFilePath);

        verify(this.mockMdlConversionProcessor).process(same(filePath), same(outputDir));
        verifyNoMoreInteractions(this.mockMdlConversionProcessor);
    }
}