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
package eu.ddmore.fis.controllers.utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import eu.ddmore.archive.Archive;
import eu.ddmore.archive.ArchiveFactory;
import eu.ddmore.archive.exception.ArchiveException;


@RunWith(MockitoJUnitRunner.class)
public class CtlArchiveCreatorTest extends AbstractArchiveCreatorTestBase {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    private final static String CTL_FILE_NAME = "model.ctl";
    private final static String DATA_FILE_NAME = "model_data.csv";

    @Override
    protected ArchiveCreator createArchiveCreator(final ArchiveFactory mockArchiveFactory) {
        return new CtlArchiveCreator(mockArchiveFactory);
    }

    /**
     * Test method for {@link eu.ddmore.fis.controllers.utils.BaseArchiveCreator#buildArchive(java.io.File, java.io.File)}.
     * <p>
     * @throws IOException 
     * @throws ArchiveException 
     */
    @Test
    public void testBuildArchiveWhereDataFileInSameDirectoryAsModelFile() throws IOException, ArchiveException {
    
        // Prepare model file and data file - note that the model file does have to exist
        // since it is going to be read in and parsed, but the data file doesn't need to exist
        final File controlFile = new File(new File(this.tempFolder.getRoot(), "mydir"), CTL_FILE_NAME);
        final File dataFile = new File(new File(this.tempFolder.getRoot(), "mydir"), DATA_FILE_NAME);
        
        // Simulate the data file being associated with the model file
        writeDummyCtlFileContentWithSpecifiedDataStatement(controlFile, DATA_FILE_NAME);
        
        final Archive archive = mockArchiveCreation(controlFile, "/");
        
        // Call the method under test
        invokeBuildArchive(controlFile);
        
        verifyArchiveCreation(controlFile, "/", Arrays.asList(dataFile), Arrays.asList("/"), archive);
    }
    
    /**
     * Test method for {@link eu.ddmore.fis.controllers.utils.BaseArchiveCreator#buildArchive(java.io.File, java.io.File)}.
     * <p>
     * @throws IOException 
     * @throws ArchiveException 
     */
    @Test
    public void testBuildArchiveWhereDataFileInDifferentDirectoryToModelFile() throws IOException, ArchiveException {
    
        // Prepare model file and data file - note that the model file does have to exist
        // since it is going to be read in and parsed, but the data file doesn't need to exist
        final File controlFile = new File(new File(this.tempFolder.getRoot(), "models"), CTL_FILE_NAME);
        final File dataFile = new File(new File(this.tempFolder.getRoot(), "data"), DATA_FILE_NAME);
        
        // Simulate the data file being associated with the model file
        writeDummyCtlFileContentWithSpecifiedDataStatement(controlFile, "../data/" + DATA_FILE_NAME);
        
        final Archive archive = mockArchiveCreation(controlFile, "/models");
        
        // Call the method under test
        invokeBuildArchive(controlFile);
        
        verifyArchiveCreation(controlFile, "/models", Arrays.asList(dataFile), Arrays.asList("/data"), archive);
    }
    
    /**
     * Test method for {@link eu.ddmore.fis.controllers.utils.BaseArchiveCreator#buildArchive(java.io.File, java.io.File)}.
     * <p>
     * @throws IOException 
     * @throws ArchiveException 
     */
    @Test
    public void testBuildArchiveWhereDataFileInDifferentDirectoryToModelFileAndExtraInputFilesBothRelativeAndAbsoluteAreProvided() throws IOException, ArchiveException {
    
        // Prepare model file, data file and extra input files - note that the model file does
        // have to exist since it is going to be read in and parsed, but the data file and
        // extra input files don't need to exist
        final File controlFile = new File(new File(this.tempFolder.getRoot(), "models"), CTL_FILE_NAME);
        final File dataFile = new File(new File(this.tempFolder.getRoot(), "data"), DATA_FILE_NAME);
        final File extraInputFile1 = new File(new File(this.tempFolder.getRoot(), "models"), "model.lst");
        final File extraInputFile2 = new File(this.tempFolder.getRoot(), "model.txt");
        
        // Simulate the data file being associated with the model file
        writeDummyCtlFileContentWithSpecifiedDataStatement(controlFile, "../data/" + DATA_FILE_NAME);
        
        final Archive archive = mockArchiveCreation(controlFile, "/models");
        
        // Call the method under test
        invokeBuildArchive(controlFile, extraInputFile1, new File("../" + extraInputFile2.getName())); // First extraInputFile has absolute path, second extraInputFile has relative path
        
        verifyArchiveCreation(controlFile, "/models", Arrays.asList(dataFile, extraInputFile1, extraInputFile2), Arrays.asList("/data", "/models", "/"), archive);
    }
    
    /**
     * Test method for {@link eu.ddmore.fis.controllers.utils.BaseArchiveCreator#buildArchive(java.io.File, java.io.File)}.
     * <p>
     * See https://nonmem.iconplc.com/nonmem720/guides/iv.pdf pg 18 for the permitted filename syntax on the $DATA statement.
     * <p>
     * @throws IOException 
     * @throws ArchiveException 
     */
    @Test
    public void testBuildArchiveWhereDataStatementHasFilePathInSingleQuotesThatContainsSpacesAndSpecialCharacters() throws IOException, ArchiveException {
    
        // Prepare model file and data file - note that the model file does have to exist
        // since it is going to be read in and parsed, but the data file doesn't need to exist
        final File controlFile = new File(new File(this.tempFolder.getRoot(), "mydir"), CTL_FILE_NAME);
        final File dataFile = new File(new File(this.tempFolder.getRoot(), "mydir"), "model;, (data).csv");
        
        // Simulate the data file being associated with the model file
        writeDummyCtlFileContentWithSpecifiedDataStatement(controlFile, "'model;, (data).csv'");
        
        final Archive archive = mockArchiveCreation(controlFile, "/");
        
        // Call the method under test
        invokeBuildArchive(controlFile);
        
        verifyArchiveCreation(controlFile, "/", Arrays.asList(dataFile), Arrays.asList("/"), archive);
    }
    
    /**
     * Test method for {@link eu.ddmore.fis.controllers.utils.BaseArchiveCreator#buildArchive(java.io.File, java.io.File)}.
     * <p>
     * See https://nonmem.iconplc.com/nonmem720/guides/iv.pdf pg 18 for the permitted filename syntax on the $DATA statement.
     * <p>
     * @throws IOException 
     * @throws ArchiveException 
     */
    @Test
    public void testBuildArchiveWhereDataStatementHasFilePathInDoubleQuotesThatContainsSpacesAndSpecialCharacters() throws IOException, ArchiveException {
    
        // Prepare model file and data file - note that the model file does have to exist
        // since it is going to be read in and parsed, but the data file doesn't need to exist
        final File controlFile = new File(new File(this.tempFolder.getRoot(), "mydir"), CTL_FILE_NAME);
        final File dataFile = new File(new File(this.tempFolder.getRoot(), "mydir"), "model data.csv");
        
        // Simulate the data file being associated with the model file
        writeDummyCtlFileContentWithSpecifiedDataStatement(controlFile, "\"model data.csv\"");
        
        final Archive archive = mockArchiveCreation(controlFile, "/");
        
        // Call the method under test
        invokeBuildArchive(controlFile);
        
        verifyArchiveCreation(controlFile, "/", Arrays.asList(dataFile), Arrays.asList("/"), archive);
    }
    
    /**
     * Test method for {@link eu.ddmore.fis.controllers.utils.BaseArchiveCreator#buildArchive(java.io.File, java.io.File)}.
     * <p>
     * @throws IOException 
     * @throws ArchiveException 
     */
    @Test
    public void testBuildArchiveWhereDataStatementIndented() throws IOException, ArchiveException {
    
        // Prepare model file and data file - note that the model file does have to exist
        // since it is going to be read in and parsed, but the data file doesn't need to exist
        final File controlFile = new File(new File(this.tempFolder.getRoot(), "mydir"), CTL_FILE_NAME);
        final File dataFile = new File(new File(this.tempFolder.getRoot(), "mydir"), DATA_FILE_NAME);
        
        // Simulate the data file being associated with the model file
        FileUtils.write(controlFile, "$PROBLEM The problem statement\n\t$DATA " + DATA_FILE_NAME + " IGNORE=@\n$INPUT ID TIME WT AMT\n$EST\n");
        
        final Archive archive = mockArchiveCreation(controlFile, "/");
        
        // Call the method under test
        invokeBuildArchive(controlFile);
        
        verifyArchiveCreation(controlFile, "/", Arrays.asList(dataFile), Arrays.asList("/"), archive);
    }
    
    /**
     * Test method for {@link eu.ddmore.fis.controllers.utils.BaseArchiveCreator#buildArchive(java.io.File, java.io.File)}.
     * <p>
     * @throws IOException 
     * @throws ArchiveException 
     */
    @Test
    public void testBuildArchiveWhereDataStatementSplitOverTwoLines() throws IOException, ArchiveException {
    
        // Prepare model file and data file - note that the model file does have to exist
        // since it is going to be read in and parsed, but the data file doesn't need to exist
        final File controlFile = new File(new File(this.tempFolder.getRoot(), "mydir"), CTL_FILE_NAME);
        final File dataFile = new File(new File(this.tempFolder.getRoot(), "mydir"), DATA_FILE_NAME);
        
        // Simulate the data file being associated with the model file
        writeDummyCtlFileContentWithSpecifiedDataStatement(controlFile, System.getProperty("line.separator") + DATA_FILE_NAME);
        
        final Archive archive = mockArchiveCreation(controlFile, "/");
        
        // Call the method under test
        invokeBuildArchive(controlFile);
        
        verifyArchiveCreation(controlFile, "/", Arrays.asList(dataFile), Arrays.asList("/"), archive);
    }
    
    /**
     * Test method for {@link eu.ddmore.fis.controllers.utils.BaseArchiveCreator#buildArchive(java.io.File, java.io.File)}.
     * <p>
     * @throws IOException 
     * @throws ArchiveException 
     */
    @Test
    public void testBuildArchiveWhereDataStatementCommentedOut() throws IOException, ArchiveException {
    
        // Prepare model file and data file - note that the model file does have to exist
        // since it is going to be read in and parsed, but the data file doesn't need to exist
        final File controlFile = new File(new File(this.tempFolder.getRoot(), "mydir"), CTL_FILE_NAME);
        final File dataFile = new File(new File(this.tempFolder.getRoot(), "mydir"), "ThisShouldGetPickedUp.csv");
        
        // Simulate the data file being associated with the model file
        FileUtils.write(controlFile, "$PROBLEM The problem statement\n;$DATA ThisShouldNotGetPickedUp.csv IGNORE=@\n$INPUT ID TIME WT AMT\n$EST\n$DATA ThisShouldGetPickedUp.csv IGNORE=#\n");
        
        final Archive archive = mockArchiveCreation(controlFile, "/");
        
        // Call the method under test
        invokeBuildArchive(controlFile);
        
        verifyArchiveCreation(controlFile, "/", Arrays.asList(dataFile), Arrays.asList("/"), archive);
    }
    
    /**
     * Test method for {@link eu.ddmore.fis.controllers.utils.BaseArchiveCreator#buildArchive(java.io.File, java.io.File)}.
     * <p>
     * @throws IOException 
     * @throws ArchiveException 
     */
    @Test
    public void testBuildArchiveWhereInfileStatementUsedAsSynonymOfDataStatement() throws IOException, ArchiveException {
    
        // Prepare model file and data file - note that the model file does have to exist
        // since it is going to be read in and parsed, but the data file doesn't need to exist
        final File controlFile = new File(new File(this.tempFolder.getRoot(), "mydir"), CTL_FILE_NAME);
        final File dataFile = new File(new File(this.tempFolder.getRoot(), "mydir"), DATA_FILE_NAME);
        
        // Simulate the data file being associated with the model file
        FileUtils.write(controlFile, "$PROBLEM The problem statement\n  $INFILE " + DATA_FILE_NAME + " IGNORE=@\n$INPUT ID TIME WT AMT\n$EST\n");
        
        final Archive archive = mockArchiveCreation(controlFile, "/");
        
        // Call the method under test
        invokeBuildArchive(controlFile);
        
        verifyArchiveCreation(controlFile, "/", Arrays.asList(dataFile), Arrays.asList("/"), archive);
    }
    
    /**
     * Test method for {@link eu.ddmore.fis.controllers.utils.BaseArchiveCreator#buildArchive(java.io.File, java.io.File)}.
     * <p>
     * If a referenced data file does not exist at the specified location on the filesystem
     * then the CombineArchive library throws an {@link IllegalArgumentException}. Mock the
     * archive creation functionality to do this, and check that the exception is propagated
     * up.
     * <p>
     * @throws IOException 
     * @throws ArchiveException 
     */
    @Test(expected=IllegalArgumentException.class)
    public void testBuildArchiveWhereDataFileIsReferencedButNotExists() throws IOException, ArchiveException {
    
        // Prepare model file and data file - note that the model file does have to exist
        // since it is going to be read in and parsed, but the data file doesn't need to exist
        final File controlFile = new File(new File(this.tempFolder.getRoot(), "mydir"), CTL_FILE_NAME);
        final File dataFile = new File(new File(this.tempFolder.getRoot(), "mydir"), DATA_FILE_NAME);
    
        // Simulate the data file being associated with the model file
        writeDummyCtlFileContentWithSpecifiedDataStatement(controlFile, DATA_FILE_NAME);
        
        mockArchiveCreationToThrowExceptionWhenAddDataFile(controlFile, dataFile);
        
        // Call the method under test
        invokeBuildArchive(controlFile);

    }
    
    private void writeDummyCtlFileContentWithSpecifiedDataStatement(final File controlFile, final String dataFileName) throws IOException {
        FileUtils.write(controlFile, "$PROBLEM The problem statement\n$DATA " + dataFileName + " IGNORE=@\n$INPUT ID TIME WT AMT\n$EST\n");
    }

}
