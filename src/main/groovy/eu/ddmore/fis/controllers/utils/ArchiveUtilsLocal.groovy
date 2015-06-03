/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers.utils

import static groovy.io.FileType.*
import static groovy.io.FileVisitResult.*

import java.nio.file.Path

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.log4j.Logger

import eu.ddmore.archive.Archive
import eu.ddmore.archive.ArchiveFactory
import eu.ddmore.archive.Entry


/**
 * Contains utility methods relating to the Archive functionality within FIS.
 */
public class ArchiveUtilsLocal {
    private static final Logger LOG = Logger.getLogger(ArchiveUtilsLocal.class)
    
    public static Archive buildMDLArchive(final MdlUtils mdlUtils, final ArchiveFactory archiveFactory, final File archiveFile, final File modelFile) {
        
        if (archiveFile.exists()) {
            LOG.warn("Archive file ${archiveFile} already exists, removed.")
            FileUtils.deleteQuietly(archiveFile)
        }
        
        final Collection<File> dataFiles = mdlUtils.getDataFileFromMDL(modelFile)
        dataFiles = dataFiles.collect { new File(FilenameUtils.normalize(it.getAbsolutePath())) }
        
        final Path commonBasePath = getCommonBasePath(dataFiles, modelFile.getParentFile())
        LOG.debug("Input file ${modelFile.getPath()} references ${dataFiles}")
        LOG.debug("Common base path for all inputs is ${commonBasePath}")
        
        final Archive archive = archiveFactory.createArchive(archiveFile)
        try {
            archive.open()
            final String modelFileDirPathInArchive = "/" + commonBasePath.relativize(modelFile.getParentFile().toPath())
            Entry en = archive.addFile(modelFile, modelFileDirPathInArchive)
            LOG.debug("Adding ${modelFile} at ${en.getFilePath()}")
            archive.addMainEntry(en)
            dataFiles.each {
                String location = "/" + commonBasePath.relativize(it.getParentFile().toPath())
                LOG.debug("Adding ${it} at ${location}")
                archive.addFile(it,location)
            }
        } finally {
            archive.close()
        }
        
        return archive
    }
    
    /**
     * Finds a common base path for a list of files starting from the given candidate path.
     * @param files
     * @param candidate
     * @return the common base path
     */
    private static Path getCommonBasePath(Collection<File> files, File candidate) {
        int size = files.size()
        if (files.findAll { it.toPath().startsWith(candidate.toPath()) }.size() == size) {
            return candidate.toPath()
        } else {
            return getCommonBasePath(files,candidate.getParentFile())
        }
    }
    
}
