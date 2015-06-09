package eu.ddmore.fis.controllers.utils;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.ddmore.archive.Archive;
import eu.ddmore.archive.ArchiveFactory;
import eu.ddmore.archive.exception.ArchiveException;


@RunWith(MockitoJUnitRunner.class)
public class MdlArchiveCreatorTest extends AbstractArchiveCreatorTestBase {
    
    private final static String MDL_FILE_NAME = "UseCase1.mdl";
    private final static String DATA_FILE_NAME = "warfarin_conc.csv";
    
    @Mock
    private MdlUtils mockMdlUtils;

    /**
     * {@inheritDoc}
     */
    protected ArchiveCreator createArchiveCreator(final ArchiveFactory mockArchiveFactory) {
        return new MdlArchiveCreator(mockArchiveFactory, this.mockMdlUtils);
    }

    /**
     * Test method for {@link eu.ddmore.fis.controllers.utils.BaseArchiveCreator#buildArchive(java.io.File, java.io.File)}.
     * <p>
     * @throws IOException 
     * @throws ArchiveException 
     */
    @Test
    public void testBuildArchiveWhereDataFileInSameDirectoryAsModelFile() throws IOException, ArchiveException {
    
        // Prepare model file and data file - note that they don't actually need to exist
        final File modelFile = Paths.get(FileUtils.getTempDirectoryPath(), "warfarin", MDL_FILE_NAME).toFile();
        final File dataFile = Paths.get(FileUtils.getTempDirectoryPath(), "warfarin", DATA_FILE_NAME).toFile();
        
        // Simulate the data file being associated with the model file
        when(this.mockMdlUtils.getDataFileFromMDL(modelFile)).thenReturn(Arrays.asList(dataFile));
        
        final Archive archive = mockArchiveCreation(modelFile, "/");
        
        // Call the method under test
        invokeBuildArchive(modelFile);
        
        verifyArchiveCreation(modelFile, dataFile, "/", "/", archive);
        verify(this.mockMdlUtils).getDataFileFromMDL(modelFile);
        verifyNoMoreInteractions(this.mockMdlUtils);
    }
    
    /**
     * Test method for {@link eu.ddmore.fis.controllers.utils.BaseArchiveCreator#buildArchive(java.io.File, java.io.File)}.
     * <p>
     * @throws IOException 
     * @throws ArchiveException 
     */
    @Test
    public void testBuildArchiveWhereDataFileInDifferentDirectoryToModelFile() throws IOException, ArchiveException {
    
        // Prepare model file and data file - note that they don't actually need to exist
        final File modelFile = Paths.get(FileUtils.getTempDirectoryPath(), "models", MDL_FILE_NAME).toFile();
        final File dataFile = Paths.get(FileUtils.getTempDirectoryPath(), "data", DATA_FILE_NAME).toFile();
        
        // Simulate the data file being associated with the model file
        when(this.mockMdlUtils.getDataFileFromMDL(modelFile)).thenReturn(Arrays.asList(dataFile));
        
        final Archive archive = mockArchiveCreation(modelFile, "/models");
        
        // Call the method under test
        invokeBuildArchive(modelFile);
        
        verifyArchiveCreation(modelFile, dataFile, "/models", "/data", archive);
        verify(this.mockMdlUtils).getDataFileFromMDL(modelFile);
        verifyNoMoreInteractions(this.mockMdlUtils);
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
    
        // Prepare model file and data file - note that they don't actually need to exist
        final File modelFile = Paths.get(FileUtils.getTempDirectoryPath(), "warfarin", MDL_FILE_NAME).toFile();
        final File dataFile = Paths.get(FileUtils.getTempDirectoryPath(), "warfarin", DATA_FILE_NAME).toFile();
    
        // Simulate the data file being associated with the model file
        when(this.mockMdlUtils.getDataFileFromMDL(modelFile)).thenReturn(Arrays.asList(dataFile));
        
        mockArchiveCreationToThrowExceptionWhenAddDataFile(modelFile, dataFile);
        
        // Call the method under test
        invokeBuildArchive(modelFile);

    }

}
