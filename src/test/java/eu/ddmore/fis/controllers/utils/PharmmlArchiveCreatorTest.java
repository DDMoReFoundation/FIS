package eu.ddmore.fis.controllers.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.ddmore.archive.Archive;
import eu.ddmore.archive.ArchiveFactory;
import eu.ddmore.archive.exception.ArchiveException;
import eu.ddmore.libpharmml.ILibPharmML;
import eu.ddmore.libpharmml.IPharmMLResource;
import eu.ddmore.libpharmml.dom.PharmML;
import eu.ddmore.libpharmml.dom.dataset.DataSet;
import eu.ddmore.libpharmml.dom.dataset.ExternalFile;
import eu.ddmore.libpharmml.dom.modellingsteps.ExternalDataSet;
import eu.ddmore.libpharmml.dom.modellingsteps.ModellingSteps;


@RunWith(MockitoJUnitRunner.class)
public class PharmmlArchiveCreatorTest extends AbstractArchiveCreatorTestBase {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    private final static String PHARMML_FILE_NAME = "model.xml";
    private final static String DATA_FILE_NAME = "model_data.csv";
    
    private final static String DUMMY_PHARMML_FILE_CONTENT = "<DummyPharmMLContent />";
    
    @Mock
    private ILibPharmML mockLibPharmML;

    @Override
    protected ArchiveCreator createArchiveCreator(final ArchiveFactory mockArchiveFactory) {
        return new PharmmlArchiveCreator(mockArchiveFactory, this.mockLibPharmML);
    }

    /**
     * Test method for {@link eu.ddmore.fis.controllers.utils.BaseArchiveCreator#buildArchive(java.io.File, java.io.File)}.
     * <p>
     * @throws IOException 
     * @throws ArchiveException 
     */
    @Test
    public void testBuildArchiveWhereDataFileInSameDirectoryAsModelFile() throws IOException, ArchiveException {
    
        // Prepare model file and data file - note that data file doesn't need to exist but PharmML file does (but can have dummy data)
        final File pharmmlFile = new File(new File(this.tempFolder.getRoot(), "mydir"), PHARMML_FILE_NAME);
        final File dataFile = new File(new File(this.tempFolder.getRoot(), "mydir"), DATA_FILE_NAME);
        FileUtils.write(pharmmlFile, DUMMY_PHARMML_FILE_CONTENT);
        
        // Simulate the data file being associated with the model file
        final IPharmMLResource mockPharmmlResource = mock(IPharmMLResource.class);
        simulatePharmMLResourceReferencingDataFiles(mockPharmmlResource, DATA_FILE_NAME);
        when(this.mockLibPharmML.createDomFromResource(any(FileInputStream.class))).thenReturn(mockPharmmlResource);
        
        final Archive archive = mockArchiveCreation(pharmmlFile, "/");
        
        // Call the method under test
        invokeBuildArchive(pharmmlFile);
        
        verifyArchiveCreation(pharmmlFile, dataFile, "/", "/", archive);
        verifyCorrectPharmmlFileReadIn();
        verify(mockPharmmlResource).getDom();
        verifyNoMoreInteractions(mockPharmmlResource);
    }
    
    /**
     * Test method for {@link eu.ddmore.fis.controllers.utils.BaseArchiveCreator#buildArchive(java.io.File, java.io.File)}.
     * <p>
     * @throws IOException 
     * @throws ArchiveException 
     */
    @Test
    public void testBuildArchiveWhereDataFileInDifferentDirectoryToModelFile() throws IOException, ArchiveException {
    
        // Prepare model file and data file - note that data file doesn't need to exist but PharmML file does (but can have dummy data)
        final File pharmmlFile = new File(new File(this.tempFolder.getRoot(), "models"), PHARMML_FILE_NAME);
        final File dataFile = new File(new File(this.tempFolder.getRoot(), "data"), DATA_FILE_NAME);
        FileUtils.write(pharmmlFile, DUMMY_PHARMML_FILE_CONTENT);
        
        // Simulate the data file being associated with the model file
        final IPharmMLResource mockPharmmlResource = mock(IPharmMLResource.class);
        simulatePharmMLResourceReferencingDataFiles(mockPharmmlResource, "../data/" + DATA_FILE_NAME);
        when(this.mockLibPharmML.createDomFromResource(any(FileInputStream.class))).thenReturn(mockPharmmlResource);
        
        final Archive archive = mockArchiveCreation(pharmmlFile, "/models");
        
        // Call the method under test
        invokeBuildArchive(pharmmlFile);
        
        verifyArchiveCreation(pharmmlFile, dataFile, "/models", "/data", archive);
        verifyCorrectPharmmlFileReadIn();
        verify(mockPharmmlResource).getDom();
        verifyNoMoreInteractions(mockPharmmlResource);
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
    
        // Prepare model file and data file - note that data file doesn't need to exist but PharmML file does (but can have dummy data)
        final File pharmmlFile = new File(new File(this.tempFolder.getRoot(), "mydir"), PHARMML_FILE_NAME);
        final File dataFile = new File(new File(this.tempFolder.getRoot(), "mydir"), DATA_FILE_NAME);
        FileUtils.write(pharmmlFile, DUMMY_PHARMML_FILE_CONTENT);
        
        // Simulate the data file being associated with the model file
        final IPharmMLResource mockPharmmlResource = mock(IPharmMLResource.class);
        simulatePharmMLResourceReferencingDataFiles(mockPharmmlResource, DATA_FILE_NAME);
        when(this.mockLibPharmML.createDomFromResource(any(FileInputStream.class))).thenReturn(mockPharmmlResource);
        
        mockArchiveCreationToThrowExceptionWhenAddDataFile(pharmmlFile, dataFile);
        
        // Call the method under test
        invokeBuildArchive(pharmmlFile);

    }
    
    /**
     * Mock out the LibPharmML functionality to simulate one or more data files, having the
     * specified (relative) paths, being associated with the specified PharmML Resource
     * representing a PharmML model file.
     * <p>
     * @param mockPharmmlResource - Mocked out {@link IPharmMLResource} representing the PharmML model file
     * @param dataFilePath - (Relative) path to data file to be associated with the PharmML model file
     */
    private void simulatePharmMLResourceReferencingDataFiles(final IPharmMLResource mockPharmmlResource, final String dataFilePath) {

        final ExternalFile externalFile = new ExternalFile();
        externalFile.setPath(dataFilePath);
        
        final DataSet dataSet = new DataSet();
        dataSet.setExternalFile(externalFile);
        
        final ExternalDataSet extDataSet = new ExternalDataSet();
        extDataSet.setDataSet(dataSet);
        
        final ModellingSteps modellingSteps = new ModellingSteps();
        modellingSteps.getListOfExternalDataSet().add(extDataSet);
        
        final PharmML pharmML = new PharmML();
        pharmML.setModellingSteps(modellingSteps);
        
        when(mockPharmmlResource.getDom()).thenReturn(pharmML);
        
    }

    private void verifyCorrectPharmmlFileReadIn() throws IOException {
        final ArgumentCaptor<FileInputStream> pharmmlFileInputStreamArgCaptor = ArgumentCaptor.forClass(FileInputStream.class);
        verify(this.mockLibPharmML).createDomFromResource(pharmmlFileInputStreamArgCaptor.capture());
        final FileInputStream realFileInputStream = pharmmlFileInputStreamArgCaptor.getValue();
        assertEquals("Checking that the correct PharmML file was read", DUMMY_PHARMML_FILE_CONTENT, new BufferedReader(new InputStreamReader(realFileInputStream)).readLine());
        verifyNoMoreInteractions(this.mockLibPharmML);
    }


}
