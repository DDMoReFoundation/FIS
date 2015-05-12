/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Verifies that MDL to PharmML HTTP endpoint conforms to API.
 */
public class MdlToPharmMLConverterServiceAT extends SystemPropertiesAware {
	private static final String TEST_DATA_DIR = "/test-models/%s/6.0.7/Warfarin_ODE/";
	private static final String MDL_FILE_NAME = "Warfarin-ODE-latest.mdl";

	private static final URL MDL_FILE_URL = MdlToPharmMLConverterServiceAT.class.getResource(String.format(TEST_DATA_DIR, "mdl") + MDL_FILE_NAME);

	private final FISHttpRestClient fisClient = new FISHttpRestClient(System.getProperty("fis.url"));
	
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	
	@Test
	public void shouldCorrectlyConvertMdlFile() throws IOException {
		final File workingDir = new File(temporaryFolder.getRoot(), "shouldCorrectlyConvertMdlFile");
		workingDir.mkdir();
		final File mdlFile = new File(workingDir, MDL_FILE_NAME);
		FileUtils.copyURLToFile(MDL_FILE_URL, mdlFile);
		
		String output = fisClient.convertMdlToPharmML(mdlFile.getAbsolutePath(), workingDir.getAbsolutePath());
		
		assertFalse("Result MDL file path is not empty", output.isEmpty());
		assertTrue("Result MDL file exists", new File(output).exists());
	}
	
	@Test
	public void shouldCreateOutputDirectoryIfItDoesntExist() throws IOException {
        final File workingDir = new File(temporaryFolder.getRoot(), "shouldCreateOutputDirectoryIfItDoesntExist");
        workingDir.mkdir();
        File inputsDir = new File(workingDir, "inputs");
        File outputsDir = new File(workingDir, "outputs");
        inputsDir.mkdir();
        final File mdlFile = new File(inputsDir, MDL_FILE_NAME);
        FileUtils.copyURLToFile(MDL_FILE_URL, mdlFile);
        
        String output = fisClient.convertMdlToPharmML(mdlFile.getAbsolutePath(), outputsDir.getAbsolutePath());

        assertFalse("Result MDL file path is not empty", output.isEmpty());
        assertTrue("Result MDL file exists", new File(output).exists());
        assertEquals("Result MDL file exists", outputsDir, new File(output).getParentFile());
	}
	
}
