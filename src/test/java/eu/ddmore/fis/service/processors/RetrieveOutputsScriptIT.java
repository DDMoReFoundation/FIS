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
package eu.ddmore.fis.service.processors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import eu.ddmore.fis.domain.LocalJob;
import groovy.lang.Binding;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RetrieveOutputsScriptIT {

	private static final Logger LOG = Logger.getLogger(RetrieveOutputsScriptIT.class);

    @Rule
    public TemporaryFolder testFisTemporaryFolder = new TemporaryFolder();
    @Rule
    public TemporaryFolder testMifTemporaryFolder = new TemporaryFolder();
    
    private File fisWorkingDir;
    private File executionHostFileshareLocal;
    
    private JobProcessor jobProcessor;

    @Before
    public void setUp() throws IOException {
        // Prepare the MIF execution host fileshare
        this.executionHostFileshareLocal = this.testMifTemporaryFolder.getRoot();
        LOG.debug(String.format("Test MIF execution host fileshare %s", this.executionHostFileshareLocal));
        
        final File testDataDir = FileUtils.toFile(RetrieveOutputsScriptIT.class.getResource("/eu/ddmore/fis/controllers/testWorkingDir"));
        FileUtils.copyDirectory(testDataDir, this.executionHostFileshareLocal);
        
        // Set up the FIS working directory that will receive the retrieved output files from the MIF working directory
        this.fisWorkingDir = this.testFisTemporaryFolder.getRoot();
        LOG.debug(String.format("Test FIS working dir %s", this.fisWorkingDir));
        
        final Binding binding = new Binding();
        binding.setVariable("execution.host.fileshare.local", this.executionHostFileshareLocal);
        binding.setVariable("fis.metadata.dir", ".fis");
        
        this.jobProcessor = new JobProcessor(binding);
        this.jobProcessor.setScriptFile(FileUtils.toFile(RetrieveOutputsScriptIT.class.getResource("/scripts/retrieveOutputs.groovy")));
        
    }

    @Test
    public void shouldRetrieveResultFilesFromMIFWorkingDirectory() {
        final File mifWorkingDir = new File(this.executionHostFileshareLocal, "MIF_JOB_ID");

        final String INCLUDE_REGEX = ".*\\..*";
        final String EXCLUDE_REGEX = ".*\\.blah";
        final String FILE_THAT_SHOULD_NOT_BE_COPIED_BACK_1 = "should_not_be_copied_back.blah";
        final String FILE_THAT_SHOULD_NOT_BE_COPIED_BACK_2 = "dummyfile";
        
        assertTrue("Double-checking that the file that should be excluded from the retrieval does initially exist in the MIF working dir", new File(
                mifWorkingDir, FILE_THAT_SHOULD_NOT_BE_COPIED_BACK_1).exists());

        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(this.fisWorkingDir.getAbsolutePath());
        when(job.getExecutionFile()).thenReturn("model.mdl");
        when(job.getId()).thenReturn("MIF_JOB_ID");
        when(job.getResultsIncludeRegex()).thenReturn(INCLUDE_REGEX);
        when(job.getResultsExcludeRegex()).thenReturn(EXCLUDE_REGEX);

        // Invoke the retrieveOutputs script being tested
        jobProcessor.process(job);

        assertTrue("Output LST resource should be copied back", new File(this.fisWorkingDir, "model.lst").exists());
        assertTrue("PharmML resource should be copied back", new File(this.fisWorkingDir, "model.xml").exists());
        assertFalse("File that matches the regex exclusion pattern should not be copied back",
                new File(this.fisWorkingDir, FILE_THAT_SHOULD_NOT_BE_COPIED_BACK_1).exists());
        assertFalse("File that doesn't match the regex inclusion pattern should not be copied back",
                new File(this.fisWorkingDir, FILE_THAT_SHOULD_NOT_BE_COPIED_BACK_2).exists());
        assertFalse(".MIF hidden directory should not be copied back", new File(this.fisWorkingDir, ".MIF").exists());
                
        File fisHiddenDir = new File(this.fisWorkingDir, ".fis");
        assertTrue(String.format("%s directory should be created", fisHiddenDir), new File(this.fisWorkingDir, ".fis").exists());
        File stdOut = new File(fisHiddenDir, "stdout.txt");
        File stdErr = new File(fisHiddenDir, "stderr.txt");
        assertTrue(String.format("%s file should be created", stdOut), stdOut.exists());
        assertTrue(String.format("%s file should be created", stdErr), stdErr.exists());
    }
    
    /**
     * This test uses actual result files from TPT execution (currently Monolix), this test is for verification that real-world 
     * test data is correctly handled by the implementation.
     */
    @Test
    public void shouldRetrieveExpectedTPTResultFiles() {
        final String jobId = "monolixExecution";
        
        final File mifWorkingDir = new File(this.executionHostFileshareLocal, jobId);

        final String INCLUDE_REGEX = ".*";
        final String EXCLUDE_REGEX = "(converter\\.out|project\\.xmlx)";

        assertTrue("Double-checking that the file that shouldn't be copied back does initially exist in the MIF working dir", new File(
                mifWorkingDir, "converter.out").exists());
        assertTrue("Double-checking that file that shouldn't be copied back does initially exist in the MIF working dir", new File(
                mifWorkingDir, "UseCase1/UseCase1_project/project.xmlx").exists());

        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(this.fisWorkingDir.getAbsolutePath());
        when(job.getExecutionFile()).thenReturn("UseCase1.xml");
        when(job.getId()).thenReturn(jobId);
        when(job.getResultsIncludeRegex()).thenReturn(INCLUDE_REGEX);
        when(job.getResultsExcludeRegex()).thenReturn(EXCLUDE_REGEX);

        // Invoke the retrieveOutputs script being tested
        jobProcessor.process(job);

        assertTrue("Output SO should be copied back", new File(this.fisWorkingDir, "UseCase1.SO.xml").exists());
        assertTrue("FIM resource should be copied back", new File(this.fisWorkingDir, "ddmore_fim.csv").exists());
        assertTrue("UseCase1/UseCase1_project/fim_lin.txt resource should be copied back", new File(this.fisWorkingDir, "UseCase1/UseCase1_project/fim_lin.txt").exists());
        assertTrue("CSV resource should be copied back", new File(this.fisWorkingDir, "warfarin_conc.csv").exists());
        assertFalse("File that matches the regex exclusion pattern should not be copied back",
                new File(this.fisWorkingDir, "converter.out").exists());
        assertFalse("File that doesn't match the regex inclusion pattern should not be copied back",
                new File(this.fisWorkingDir, "UseCase1/UseCase1_project/project.xmlx").exists());
        assertFalse(".MIF hidden directory should not be copied back", new File(this.fisWorkingDir, ".MIF").exists());

        File fisHiddenDir = new File(this.fisWorkingDir, ".fis");
        assertTrue(String.format("%s directory should be created", fisHiddenDir), new File(this.fisWorkingDir, ".fis").exists());
        File stdOut = new File(fisHiddenDir, "stdout.txt");
        File stdErr = new File(fisHiddenDir, "stderr.txt");
        assertTrue(String.format("%s file should be created", stdOut), stdOut.exists());
        assertTrue(String.format("%s file should be created", stdErr), stdErr.exists());
    }

    /**
     * Model execution takes place within the directory containing the model file. 
     * Therefore where a project has the structure models/mymodel.mdl and data/mydata.csv, the execution
     * directory is the subdirectory "models" of the MIF working directory.
     * This test checks that filtering is applied within the execution directory, and files/directories
     * outside of that (which should only be the data file and any extra input files) are *not* subject
     * to the filtering.
     */
    @Test
    public void shouldRetrieveResultFilesFromMIFWorkingDirectoryWithDirectoryStructure() {
        final File mifWorkingDir = new File(this.executionHostFileshareLocal, "exec_output_from_model_file_in_subdir");
        
        final String INCLUDE_REGEX = ".*\\..*";
        final String EXCLUDE_REGEX = "(nonmem.exe|temp_dir)";

        assertTrue("Double-checking that the file that shouldn't be copied back does initially exist in the MIF working dir", new File(
                mifWorkingDir, "models/nonmem.exe").exists());
        assertTrue("Double-checking that the directory that shouldn't be copied back does initially exist in the MIF working dir", new File(
                mifWorkingDir, "models/temp_dir").exists());

        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(this.fisWorkingDir.getAbsolutePath());
        when(job.getExecutionFile()).thenReturn("models/UseCase1.ctl");
        when(job.getId()).thenReturn("exec_output_from_model_file_in_subdir");
        when(job.getResultsIncludeRegex()).thenReturn(INCLUDE_REGEX);
        when(job.getResultsExcludeRegex()).thenReturn(EXCLUDE_REGEX);

        // Invoke the retrieveOutputs script being tested
        jobProcessor.process(job);

        assertTrue("Output LST resource should be copied back", new File(this.fisWorkingDir, "models/UseCase1.lst").exists());
        assertTrue("CTL resource should be copied back", new File(this.fisWorkingDir, "models/UseCase1.ctl").exists());
        assertTrue("PharmML resource should be copied back", new File(this.fisWorkingDir, "models/UseCase1.xml").exists());
        assertTrue("CSV resource should be copied back", new File(this.fisWorkingDir, "data/warfarin_conc.csv").exists());
        assertFalse("File that matches the regex exclusion pattern should not be copied back",
                new File(this.fisWorkingDir, "models/nonmem.exe").exists());
        assertFalse("Directory that matches the regex exclusion pattern should not be copied back",
                new File(this.fisWorkingDir, "models/temp_dir").exists());
        assertFalse("File that doesn't match the regex inclusion pattern should not be copied back",
                new File(this.fisWorkingDir, "models/FSUBS").exists());
        assertFalse(".MIF hidden directory should not be copied back", new File(this.fisWorkingDir, ".MIF").exists());

        File fisHiddenDir = new File(this.fisWorkingDir, ".fis");
        assertTrue(String.format("%s directory should be created", fisHiddenDir), new File(this.fisWorkingDir, ".fis").exists());
        File stdOut = new File(fisHiddenDir, "stdout.txt");
        File stdErr = new File(fisHiddenDir, "stderr.txt");
        assertTrue(String.format("%s file should be created", stdOut), stdOut.exists());
        assertTrue(String.format("%s file should be created", stdErr), stdErr.exists());
    }
}
