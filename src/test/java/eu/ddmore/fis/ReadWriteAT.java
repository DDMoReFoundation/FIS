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
import org.junit.BeforeClass;
import org.junit.Test;

import eu.ddmore.fis.domain.WriteMdlRequest;
import eu.ddmore.fis.domain.WriteMdlResponse;

/**
 * Verifies that standalone service fulfils functional requirements.
 */
public class ReadWriteAT extends SystemPropertiesAware {

	private static final String TEST_DATA_DIR = "/eu/ddmore/testdata/models/%s/ThamCCR2008/";
	private static final String MDL_FILE_NAME = "tumour_size_01July2014_OAM.mdl";
	private static final String JSON_FILE_NAME = "tumour_size_01July2014_OAM.output.json";

	private static final URL MDL_FILE_URL = ReadWriteAT.class.getResource(String.format(TEST_DATA_DIR, "mdl") + MDL_FILE_NAME);
	private static final URL JSON_FILE_URL = ReadWriteAT.class.getResource(String.format(TEST_DATA_DIR, "json") + JSON_FILE_NAME);

	// This is where the output from FIS and MIF can be found
	private static final File parentWorkingDir = new File("target", "ReadWriteAT_Test_Working_Dir");
	private final FISHttpRestClient fisClient = new FISHttpRestClient(System.getProperty("fis.url"));

	@BeforeClass
	public static void globalSetUp() throws Exception {
		FileUtils.deleteDirectory(parentWorkingDir);
		parentWorkingDir.mkdir();
	}

	@Test
	public void shouldCorrectlyReadMdlFile() throws IOException {
		final File workingDir = new File(parentWorkingDir, "ReadMdlFile");
		workingDir.mkdir();

		final File mdlFile = new File(workingDir, MDL_FILE_NAME);
		FileUtils.copyURLToFile(MDL_FILE_URL, mdlFile);

		final String mdlFileFullPath = mdlFile.getAbsolutePath().replace('\\', '/');
		String jsonFormat = fisClient.readMdl(mdlFileFullPath);

		assertTrue("Should be some data returned", jsonFormat.length() > 1000);
		assertTrue("Returned data should contain some sort of parameter object",
			jsonFormat.contains("\"tumour_size_TABLES_ORG_par\":{"));
		assertTrue("Returned data should contain some sort of data object",
			jsonFormat.contains("\"tumour_size_TABLES_ORG_dat\":{"));
		assertTrue("Returned data should contain some sort of model object",
			jsonFormat.contains("\"tumour_size_TABLES_ORG_mdl\":{"));
		assertTrue("Returned data should contain some sort of task object",
			jsonFormat.contains("\"tumour_size_TABLES_ORG_task\":{"));
// WAS: This was checking the R-output JSON not the parsed-in-to-R JSON
//		final File jsonFile = new File(workingDir, JSON_FILE_NAME + ".tmp");
//		FileUtils.copyURLToFile(JSON_FILE_URL, jsonFile);
//		String expected = FileUtils.readFileToString(jsonFile);
//		assertEquals("MDL file should be correctly read.", expected, jsonFormat);


		final String jsonFileFullPath = mdlFileFullPath.replace(MDL_FILE_NAME, JSON_FILE_NAME);
		final File f = new File(jsonFileFullPath);
		assertTrue("Temporary JSON file should not be present.", !f.exists());

	}

	@Test
	public void shouldCorrectlyWriteMdlFile_TumourSize() throws IOException {
		final File workingDir = new File(parentWorkingDir, "WriteMdlFile_TumourSize");
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

	@Test
	public void shouldCorrectlyWriteMdlFile_Prolactin() throws IOException {
		final File workingDir = new File(parentWorkingDir, "WriteMdlFile_Prolactin");
		workingDir.mkdir();

		final String MDL_FILE_NAME = "ex_model7_prolactin_01July2014_OAM.mdl";
		final String JSON_FILE_NAME = "ex_model7_prolactin_01July2014_OAM.output.json";

		final URL JSON_FILE_URL = ReadWriteAT.class.getResource("/eu/ddmore/testdata/models/json/FribergCPT2009/" + JSON_FILE_NAME);

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
