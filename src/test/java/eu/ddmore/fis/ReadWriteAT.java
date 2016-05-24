/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import eu.ddmore.fis.domain.WriteMdlRequest;
import eu.ddmore.fis.domain.WriteMdlResponse;

/**
 * Verifies that standalone service fulfils functional requirements.
 */
public class ReadWriteAT extends SystemPropertiesAware {
    private static final Logger LOG = Logger.getLogger(ReadWriteAT.class);
	private static final String TEST_DATA_DIR = "/test-models/%s/8.0.0/";
	private static final String MDL_FILE_NAME = "UseCase1.mdl";
	private static final String JSON_FILE_NAME = "UseCase1.json";

	private static final URL MDL_FILE_URL = ReadWriteAT.class.getResource(String.format(TEST_DATA_DIR, "MDL") + MDL_FILE_NAME);
	private static final URL JSON_FILE_URL = ReadWriteAT.class.getResource(String.format(TEST_DATA_DIR, "json") + JSON_FILE_NAME);

	private final FISHttpRestClient fisClient = new FISHttpRestClient(System.getProperty("fis.url"),System.getProperty("fis.management.url"));

	// This is where the output from FIS and MIF can be found
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void shouldCorrectlyReadMdlFile() throws IOException {
		final File workingDir = new File(temporaryFolder.getRoot(), "ReadMdlFile");
		workingDir.mkdir();

		final File mdlFile = new File(workingDir, MDL_FILE_NAME);
		FileUtils.copyURLToFile(MDL_FILE_URL, mdlFile);

		final String mdlFileFullPath = mdlFile.getAbsolutePath().replace('\\', '/');
		String jsonFormat = fisClient.readMdl(mdlFileFullPath);
		LOG.debug(jsonFormat);
		assertTrue("Should be some data returned", jsonFormat.length() > 1000);
		assertTrue("Returned data should contain some sort of parameter object",
			jsonFormat.contains("\"warfarin_PK_ODE_par\":{"));
		assertTrue("Returned data should contain some sort of data object",
			jsonFormat.contains("\"warfarin_PK_ODE_dat\":{"));
		assertTrue("Returned data should contain some sort of model object",
			jsonFormat.contains("\"warfarin_PK_ODE_mdl\":{"));
		assertTrue("Returned data should contain some sort of task object",
			jsonFormat.contains("\"warfarin_PK_ODE_task\":{"));
		final String jsonFileFullPath = mdlFileFullPath.replace(MDL_FILE_NAME, JSON_FILE_NAME);
		final File f = new File(jsonFileFullPath);
		assertTrue("Temporary JSON file should not be present.", !f.exists());

	}

	@Test
	public void shouldCorrectlyWriteMdlFile() throws IOException {
		final File workingDir = new File(temporaryFolder.getRoot(), "WriteMdlFile");
		workingDir.mkdir();

		final File jsonFile = new File(workingDir, JSON_FILE_NAME);
		FileUtils.copyURLToFile(JSON_FILE_URL, jsonFile);

		WriteMdlRequest writeReq = new WriteMdlRequest();
		writeReq.setFileContent(FileUtils.readFileToString(jsonFile));
		writeReq.setFileName(workingDir.getAbsolutePath() + "/" + MDL_FILE_NAME);

		// Call the method under test
		final WriteMdlResponse writeResp = this.fisClient.writeMdl(writeReq);

		assertTrue("MDL output file should have been created", new File(workingDir, MDL_FILE_NAME).exists());
		assertEquals("Success status should be reported from the service", "Successful", writeResp.getStatus());
	}

}
