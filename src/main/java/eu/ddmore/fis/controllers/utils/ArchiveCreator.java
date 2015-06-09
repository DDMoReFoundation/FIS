/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers.utils;

import java.io.File;

import eu.ddmore.archive.Archive;
import eu.ddmore.archive.exception.ArchiveException;

/**
 * Contains utility methods relating to the Archive functionality within FIS.
 */
public interface ArchiveCreator {

    /**
     * Build the Archive based on the provided model/control file.
     * <p>
     * @param archiveFile - {@link File} specifying the archive file to be created
     * @param modelFile - {@link File} that specifies the model/control file
     * @return the created {@link Archive}
     * @throws ArchiveException - if something fatal occurred during the addition of files to the archive 
     */
    Archive buildArchive(File archiveFile, File modelFile) throws ArchiveException;

}
