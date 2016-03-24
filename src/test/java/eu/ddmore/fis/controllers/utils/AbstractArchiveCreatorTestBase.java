package eu.ddmore.fis.controllers.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.xtext.util.Files;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import eu.ddmore.archive.Archive;
import eu.ddmore.archive.ArchiveFactory;
import eu.ddmore.archive.Entry;
import eu.ddmore.archive.exception.ArchiveException;


/**
 * Superclass containing common functionality shared between the
 * test classes of the implementations of {@link ArchiveCreator}.
 */
public abstract class AbstractArchiveCreatorTestBase {
    
    // The location in which the archive will be created
    @Rule
    public TemporaryFolder fisMetadataDirectory = new TemporaryFolder();
    
    private File archiveFile;

    @Mock
    private ArchiveFactory mockArchiveFactory;
    
    // Instance of class under test
    private ArchiveCreator archiveCreator;

    /**
     * Set-up tasks prior to each test being run.
     */
    @Before
    public void setUp() {
        this.archiveFile = new File(this.fisMetadataDirectory.getRoot(), "foobar.phex");
        // Create instance of the class under test
        this.archiveCreator = createArchiveCreator(this.mockArchiveFactory);
    }
    
    /**
     * Create the instance of the {@link ArchiveCreator} under test.
     * <p>
     * @param mockArchiveFactory - mock {@link ArchiveFactory} to constructor-inject
     * @return the {@link ArchiveCreator}
     */
    protected abstract ArchiveCreator createArchiveCreator(final ArchiveFactory mockArchiveFactory);
    
    /**
     * Call the method under test, implementation of {@link ArchiveCreator#buildArchive(File, File)}.
     */
    protected final void invokeBuildArchive(final File modelFile, final File ... extraInputFiles) throws ArchiveException {
        this.archiveCreator.buildArchive(this.archiveFile, modelFile, Arrays.asList(extraInputFiles));
    }
    
    protected final Archive mockArchiveCreation(final File modelFile, final String modelFileDirPathInArchive)
            throws IOException, ArchiveException {
        
        final Archive archive = mock(Archive.class);
        
        // Mock the archive creation behaviour
        final Entry mainEntry = mock(Entry.class);
        when(mainEntry.getFilePath()).thenReturn(modelFileDirPathInArchive);
        when(archive.addFile(modelFile, modelFileDirPathInArchive)).thenReturn(mainEntry); // No behaviour req'd for dataFile, just the verification
        when(this.mockArchiveFactory.createArchive(this.archiveFile)).then(new Answer<Archive>() {
            @Override
            public Archive answer(InvocationOnMock invocation) throws Throwable {
                FileUtils.writeStringToFile(AbstractArchiveCreatorTestBase.this.archiveFile, "This is mock Phex file contents");
                when(archive.getArchiveFile()).thenReturn(AbstractArchiveCreatorTestBase.this.archiveFile);
                return archive;
            }
        });
        
        return archive;
    }
    
    protected final void verifyArchiveCreation(
            final File modelFile, final String modelFileDirPathInArchive,
            final List<File> dataFilePlusExtraInputFiles, final List<String> dirPathsInArchiveForDataFilePlusExtraInputFiles,
            final Archive archive) throws IOException, ArchiveException {
            
        assertTrue("Archive is created in dummy FIS metadata directory", this.archiveFile.exists());
        
        verify(archive).open();
        final ArgumentCaptor<Entry> entryArgCaptor = ArgumentCaptor.forClass(Entry.class);
        verify(archive).addMainEntry(entryArgCaptor.capture());
        assertEquals("Checking that the mainEntry that was added to the Archive has the correct file path",
            modelFileDirPathInArchive, entryArgCaptor.getValue().getFilePath());
        verify(archive).addFile(modelFile, modelFileDirPathInArchive);
        for (int i = 0; i < dataFilePlusExtraInputFiles.size(); i++) {
            verify(archive).addFile(dataFilePlusExtraInputFiles.get(i), dirPathsInArchiveForDataFilePlusExtraInputFiles.get(i));
        }
        verify(archive).close();
        verifyNoMoreInteractions(archive);
        
        verify(this.mockArchiveFactory).createArchive(this.archiveFile);
        verifyNoMoreInteractions(this.mockArchiveFactory);
    }

    /**
     * Mock the archive creation behaviour so that the addition of a data file will throw
     * IllegalArgumentException (this is what the real CombineArchive library does)
     * <p>
     * @param modelFile
     * @param dataFile
     * @throws ArchiveException
     */
    protected final void mockArchiveCreationToThrowExceptionWhenAddDataFile(final File modelFile, final File dataFile) throws ArchiveException {
        final Archive archive = mock(Archive.class);
        final Entry mainEntry = mock(Entry.class);
        when(mainEntry.getFilePath()).thenReturn("/");
        when(archive.addFile(modelFile, "/")).thenReturn(mainEntry);
        when(archive.addFile(dataFile, "/")).thenThrow(new IllegalArgumentException("Data file does not exist"));
        when(this.mockArchiveFactory.createArchive(this.archiveFile)).then(new Answer<Archive>() {
            @Override
            public Archive answer(final InvocationOnMock invocation) throws Throwable {
                FileUtils.writeStringToFile(AbstractArchiveCreatorTestBase.this.archiveFile, "This is mock Phex file contents");
                when(archive.getArchiveFile()).thenReturn(AbstractArchiveCreatorTestBase.this.archiveFile);
                return archive;
            }
        });
    }

    protected static void writeDummyFile(File file, String content) {
        file.getParentFile().mkdirs();
        Files.writeStringIntoFile(file.getAbsolutePath(), content);
    }
}
