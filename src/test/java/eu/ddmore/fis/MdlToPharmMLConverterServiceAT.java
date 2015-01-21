/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Verifies that standalone service fulfils functional requirements.
 */
public class MdlToPharmMLConverterServiceAT extends SystemPropertiesAware {

	private static final String TEST_DATA_DIR = "/eu/ddmore/testdata/models/%s/6.0.7/Warfarin_ODE/";
	private static final String MDL_FILE_NAME = "Warfarin-ODE-latest.mdl";
	private static final String OUTPUT_FILE_NAME = "Warfarin-ODE-latest.xml";

	private static final URL MDL_FILE_URL = MdlToPharmMLConverterServiceAT.class.getResource(String.format(TEST_DATA_DIR, "mdl") + MDL_FILE_NAME);

	// This is where the output from FIS and MIF can be found
	private static final File parentWorkingDir = new File("target", "convertAT_Test_Working_Dir");
	private final FISHttpRestClient fisClient = new FISHttpRestClient(System.getProperty("fis.url"));
	
	@BeforeClass
	public static void globalSetUp() throws Exception {
		FileUtils.deleteDirectory(parentWorkingDir);
		parentWorkingDir.mkdir();
	}

	@Test
	public void shouldCorrectlyConvertMdlFile() throws IOException {
		final File workingDir = new File(parentWorkingDir, "ReadMdlFile");
		workingDir.mkdir();

		final File mdlFile = new File(workingDir, MDL_FILE_NAME);
		FileUtils.copyURLToFile(MDL_FILE_URL, mdlFile);

		final String mdlFileFullPath = mdlFile.getAbsolutePath();
		final String outputFileFullPath = (parentWorkingDir.getAbsolutePath() +File.separator+ OUTPUT_FILE_NAME);
		String output = fisClient.convertMdlToPharmML(mdlFileFullPath, outputFileFullPath);
		
		//Checks if returned file location does exist.
		assertTrue("Output file should be created", new File(output).exists());
		
	}
	
	@Test
	public void shouldCreateOutputDirectroyIfDoesntExist() throws IOException {
		final File workingDir = new File(parentWorkingDir, "ReadMdlFile");
		final String newLocation = "NewLocation";
		workingDir.mkdir();

		final File mdlFile = new File(workingDir, MDL_FILE_NAME);
		FileUtils.copyURLToFile(MDL_FILE_URL, mdlFile);

		final String mdlFileFullPath = mdlFile.getAbsolutePath();
		File outputFileLocation = new File(parentWorkingDir.getAbsolutePath()+File.separator+newLocation);
		if(outputFileLocation.exists()){
			FileUtils.deleteDirectory(outputFileLocation);
		}
		final String outputFileFullPath = (outputFileLocation+File.separator+ OUTPUT_FILE_NAME);
		String output = fisClient.convertMdlToPharmML(mdlFileFullPath, outputFileFullPath);
		
		//Checks if returned file location does exist.
		assertTrue("Output file should be in newly created directory", output.equals(outputFileFullPath));
		assertTrue("Output file should be created", new File(output).exists());
		
	}
	
}
