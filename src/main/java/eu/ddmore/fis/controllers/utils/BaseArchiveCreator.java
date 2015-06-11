/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers.utils;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import eu.ddmore.archive.Archive;
import eu.ddmore.archive.ArchiveFactory;
import eu.ddmore.archive.Entry;
import eu.ddmore.archive.exception.ArchiveException;


/**
 * Contains utility methods relating to the Archive functionality within FIS.
 */
abstract class BaseArchiveCreator implements ArchiveCreator {
    private final static Logger LOG = Logger.getLogger(BaseArchiveCreator.class);
    
    private final ArchiveFactory archiveFactory;
    
    /**
     * Constructor injecting the required {@link ArchiveFactory} dependency.
     * <p>
     * @param archiveFactory - instance of {@link ArchiveFactory}
     */
    protected BaseArchiveCreator(final ArchiveFactory archiveFactory) {
        this.archiveFactory = archiveFactory;
    }
    
    /**
     * Delegate to {@link #buildArchive(File, File, Collection)} passing in an empty list of extraInputFiles.
     * <p>
     * @param archiveFile - {@link File} specifying the archive file to be created
     * @param modelFile - {@link File} that specifies the model/control file
     * @return the created {@link Archive}
     * @throws ArchiveException - if something fatal occurred during the addition of files to the archive 
     */
    public final Archive buildArchive(final File archiveFile, final File modelFile) throws ArchiveException {
        return buildArchive(archiveFile, modelFile, new ArrayList<File>());
    }
    
    /**
     * Build the Archive based on the provided model/control {@link File} and an optional
     * set of extra input {@link File}s to be included.
     * <ol>
     * <li>Check if archive file already exists and if so, delete it.
     * <li>Parse the model/control file - which is expected to have an absolute path -
     *     to find references to data files and resolve those data file relative paths
     *     against the directory of the model file; subclasses responsible for this,
     *     see {@link #gatherDataFilesFromReferencesInModelFile(File)}.
     * <li>Resolve any of the extra input files having relative paths, against the
     *     directory of the model file.
     * <li>Determine the common base path among the model file, its data file(s), and
     *     any extra input files.
     * <li>Create the Archive and populate it with the model file and its data files,
     *     with a directory structure rooted at the common base path determined in the
     *     previous step. 
     * </ol>
     * <p>
     * @param archiveFile - {@link File} specifying the archive file to be created
     * @param modelFile - {@link File} that specifies the model/control file
     * @param extraInputFiles - {@link List} of additional {@link File}s to be included in the Archive
     * @return the created {@link Archive}
     * @throws ArchiveException - if something fatal occurred during the addition of files to the archive 
     */
    public final Archive buildArchive(final File archiveFile, final File modelFile, final Collection<File> extraInputFiles) throws ArchiveException {
    
        if (archiveFile.exists()) {
            LOG.warn(String.format("Archive file %1$s already exists, removed", archiveFile));
            FileUtils.deleteQuietly(archiveFile);
        }
        
        final Collection<File> allInputFiles = gatherDataFilesFromReferencesInModelFile(modelFile);
        // Process the extra input files to make paths absolute and add the resulting Files
        // to the list of data files to produce the list of all input files
        if (CollectionUtils.isNotEmpty(extraInputFiles)) {
            for (final File extraInputFile : extraInputFiles) {
                if (extraInputFile.isAbsolute()) {
                    allInputFiles.add(extraInputFile);
                } else {
                    allInputFiles.add(resolveRelativePathAgainstDirectoryOfModelFile(modelFile, extraInputFile.getPath()));
                }
            }
        }
        
        final Path commonBasePath = getCommonBasePath(allInputFiles, modelFile.getParentFile());
        LOG.debug(String.format("Input file %1$s references %2$s", modelFile.getPath(), allInputFiles));
        LOG.debug(String.format("Common base path for all inputs is %1$s", commonBasePath));
        
        final Archive archive = this.archiveFactory.createArchive(archiveFile);
        try {
            archive.open();
            final String modelFileDirPathInArchive = "/" + commonBasePath.relativize(modelFile.getParentFile().toPath());
            final Entry en = archive.addFile(modelFile, modelFileDirPathInArchive);
            LOG.debug(String.format("Adding %1$s at %2$s", modelFile, en.getFilePath()));
            archive.addMainEntry(en);
            for (final File inputFile : allInputFiles) {
                String location = "/" + commonBasePath.relativize(inputFile.getParentFile().toPath());
                LOG.debug(String.format("Adding %1$s at %2$s", inputFile, location));
                archive.addFile(inputFile, location);
            }
        } finally {
            archive.close();
        }
        
        return archive;
    }
    
    /**
     * Parse the model/control file to find references to paths to data files, and resolve those
     * data file relative paths against the directory of the model file.
     * <p>
     * Subclasses to implement this to provide specific behaviour based on the type of model file
     * (MDL, PharmML, NM-TRAN).
     * <p>
     * @param modelFile - {@link File} to parse to derive data files
     * @return {@link Collection} of {@link File}s specifying the data files associated with the
     *         specified model file
     */
    protected abstract Collection<File> gatherDataFilesFromReferencesInModelFile(final File modelFile);
    
    /**
     * Derive the physical file location of a data file by resolving the filename or relative path
     * of that data file against the directory in which the model file lives.
     * <p> 
     * @param modelFile - model {@link File}, having an absolute path
     * @param relativePath - filename or relative path of a data file
     * @return data {@link File} having an absolute path
     */
    protected static File resolveRelativePathAgainstDirectoryOfModelFile(final File modelFile, final String relativePath) {
        final File inputFile = new File(modelFile.getParentFile(), relativePath);
        return new File(FilenameUtils.normalize(inputFile.getAbsolutePath()));
    }
    
    /**
     * Finds a common base path for a list of files starting from the given candidate path.
     * <p>
     * @param files - {@link Collection} of {@link File}s
     * @param candidate - {@link File} providing the candidate path
     * @return the common base {@link Path}
     */
    private static Path getCommonBasePath(final Collection<File> files, final File candidate) {
        for (final File file : files) {
            if (!file.toPath().startsWith(candidate.toPath())) {
                // One of the paths of the files didn't share the candidate path prefix - try the next parent directory up
                return getCommonBasePath(files, candidate.getParentFile());
            }
        }
        // If reached here then all files share the same candidate path prefix - we are done
        return candidate.toPath();
    }

}
