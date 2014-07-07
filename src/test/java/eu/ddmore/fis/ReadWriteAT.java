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
 * Verifies that standalone service fulfils functional requirements
 */
public class ReadWriteAT extends SystemPropertiesAware {

    private static final String TEST_DATA_DIR = "/eu/ddmore/testdata/models/%s/ThamCCR2008/";
    private static final String MDL_FILE_NAME = "tumour_size_25June2014_OAM.mdl";
    private static final String JSON_FILE_NAME = "tumour_size_25June2014_OAM.json";

    private static final URL MDL_FILE_URL = ReadWriteAT.class.getResource(String.format(TEST_DATA_DIR, "mdl") + MDL_FILE_NAME);
    private static final URL JSON_FILE_URL = ReadWriteAT.class.getResource(String.format(TEST_DATA_DIR, "json") + JSON_FILE_NAME);
    
    // This is where the output from FIS and MIF can be found
    private static final File parentWorkingDir = new File("target", "ReadWriteAT_Test_Working_Dir");
    private static final FISHttpRestClient fisClient = new FISHttpRestClient(System.getProperty("fis.url"));

    
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
 
        final File jsonFile = new File(workingDir, JSON_FILE_NAME + ".tmp");
        FileUtils.copyURLToFile(JSON_FILE_URL, jsonFile);
        String expected = FileUtils.readFileToString(jsonFile);
        assertEquals("MDL file should be correctly read.", expected, jsonFormat);

        final String jsonFileFullPath = mdlFileFullPath.replace(MDL_FILE_NAME, JSON_FILE_NAME);
        final File f = new File(jsonFileFullPath);
        assertTrue("Temporary JSON file should not be present.", !f.exists());
        
    	FileUtils.deleteDirectory(workingDir);
    }
    
    @Test
    public void shouldCorrectlyWriteMdlFile() throws IOException {
        final File workingDir = new File(parentWorkingDir, "WriteMdlFile");
        workingDir.mkdir();

        final File jsonFile = new File(workingDir, JSON_FILE_NAME);
        FileUtils.copyURLToFile(JSON_FILE_URL, jsonFile);

        WriteMdlRequest writeReq = new WriteMdlRequest();
        writeReq.setFileContent(FileUtils.readFileToString(jsonFile));
        writeReq.setFileName(workingDir.getAbsolutePath() + "/" + MDL_FILE_NAME);
        WriteMdlResponse writeResp = fisClient.writeMdl(writeReq);
        assertEquals("MDL file should be correctly written.", "Successful", writeResp.getStatus());
        
    	FileUtils.deleteDirectory(workingDir);
    }
}