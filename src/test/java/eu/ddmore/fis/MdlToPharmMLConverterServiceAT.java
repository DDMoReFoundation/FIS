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
package eu.ddmore.fis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Verifies that MDL to PharmML HTTP endpoint conforms to API.
 */
public class MdlToPharmMLConverterServiceAT extends SystemPropertiesAware {
	private static final String TEST_DATA_DIR = "/test-models/%s/8.0.0/";
	private static final String MDL_FILE_NAME = "UseCase1.mdl";
	private static final String PHARMML_FILE_NAME = "UseCase1.xml";
    private static final String DATA_FILE_NAME = "warfarin_conc.csv";

	private static final URL MDL_FILE_URL = MdlToPharmMLConverterServiceAT.class.getResource(String.format(TEST_DATA_DIR, "MDL") + MDL_FILE_NAME);
	private static final URL DATA_FILE_URL = MdlToPharmMLConverterServiceAT.class.getResource(String.format(TEST_DATA_DIR, "MDL") + DATA_FILE_NAME);

	private final FISHttpRestClient fisClient = new FISHttpRestClient(System.getProperty("fis.url"),System.getProperty("fis.management.url"));
	
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	
	@Test
	public void shouldCorrectlyConvertMdlFileInPlace() throws IOException {
		final File workingDir = new File(temporaryFolder.getRoot(), "shouldCorrectlyConvertMdlFileInPlace");
		workingDir.mkdir();
		final File mdlFile = new File(workingDir, MDL_FILE_NAME);
        final File dataFile = new File(workingDir, DATA_FILE_NAME);
		FileUtils.copyURLToFile(MDL_FILE_URL, mdlFile);
        FileUtils.copyURLToFile(DATA_FILE_URL, dataFile);
		
		String output = fisClient.convertMdlToPharmML(mdlFile.getAbsolutePath(), workingDir.getAbsolutePath());
		
        assertFalse("No result PharmML file path was returned", StringUtils.isEmpty(output));
        assertEquals("Checking that it is the PharmML file that is returned", PHARMML_FILE_NAME, new File(output).getName());
        assertTrue("Result PharmML file exists", new File(output).exists());
	}
	
    @Test
    public void shouldCorrectlyConvertMdlFileIntoOutputDirectory() throws IOException {
        final File workingDir = new File(temporaryFolder.getRoot(), "shouldCorrectlyConvertMdlFileIntoOutputDirectory");
        final File outputDir = new File(workingDir, "outputDir");
        workingDir.mkdir();
        outputDir.mkdir();
        final File mdlFile = new File(workingDir, MDL_FILE_NAME);
        final File dataFile = new File(workingDir, DATA_FILE_NAME);
        FileUtils.copyURLToFile(MDL_FILE_URL, mdlFile);
        FileUtils.copyURLToFile(DATA_FILE_URL, dataFile);
        
        String output = fisClient.convertMdlToPharmML(mdlFile.getAbsolutePath(), outputDir.getAbsolutePath());
        
        assertFalse("No result PharmML file path was returned", StringUtils.isEmpty(output));
        assertEquals("Checking that it is the PharmML file that is returned", PHARMML_FILE_NAME, new File(output).getName());
        assertTrue("Result PharmML file exists", new File(output).exists());
    }
	
	@Test
	public void shouldCreateOutputDirectoryIfItDoesntExist() throws IOException {
        final File workingDir = new File(temporaryFolder.getRoot(), "shouldCreateOutputDirectoryIfItDoesntExist");
        workingDir.mkdir();
        File inputsDir = new File(workingDir, "inputs");
        File outputsDir = new File(workingDir, "outputs");
        inputsDir.mkdir();
        final File mdlFile = new File(inputsDir, MDL_FILE_NAME);
        final File dataFile = new File(inputsDir, DATA_FILE_NAME);
        FileUtils.copyURLToFile(MDL_FILE_URL, mdlFile);
        FileUtils.copyURLToFile(DATA_FILE_URL, dataFile);
        
        String output = fisClient.convertMdlToPharmML(mdlFile.getAbsolutePath(), outputsDir.getAbsolutePath());

        assertFalse("No result PharmML file path was returned", StringUtils.isEmpty(output));
        assertEquals("Checking that it is the PharmML file that is returned", PHARMML_FILE_NAME, new File(output).getName());
        assertTrue("Result PharmML file exists", new File(output).exists());
        assertEquals("Result PharmML file exists in the correct directory", outputsDir, new File(output).getParentFile());
	}
	
}
