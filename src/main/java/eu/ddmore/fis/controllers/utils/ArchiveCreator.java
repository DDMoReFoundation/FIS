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
import java.util.Collection;
import java.util.List;

import eu.ddmore.archive.Archive;
import eu.ddmore.archive.exception.ArchiveException;

/**
 * Contains utility methods relating to the Archive functionality within FIS.
 * <p>
 * TODO: Consider introducing Builder design pattern here, especially if
 *       another optional parameter is introduced. ArchiveCreator would return
 *       a Builder instance based on supplied execution type, and the Builder
 *       would build the archive with contents specific to that execution type.
 */
public interface ArchiveCreator {

    /**
     * Build the Archive based on the provided model/control {@link File}.
     * The model/control file is expected to have an absolute path.
     * <p>
     * The model file is to be parsed to determine the {@link File}s of any data file(s)s
     * referenced by it. The common base path among the model file and its data file(s)
     * should then be determined. Finally the Archive is then to be created, populated
     * with the files, in the directory structure rooted at the derived common base path.
     * <p>
     * @param archiveFile - {@link File} specifying the archive file to be created
     * @param modelFile - {@link File} that specifies the model/control file
     * @return the created {@link Archive}
     * @throws ArchiveException - if something fatal occurred during the addition of files to the archive 
     */
    Archive buildArchive(File archiveFile, File modelFile) throws ArchiveException;

    /**
     * Build the Archive based on the provided model/control {@link File} and an optional
     * set of extra input {@link File}s to be included. The model/control file is
     * expected to have an absolute path, and the extra input files can either have absolute
     * paths too, or relative paths in which case they will be resolved against the model
     * file.
     * <p>
     * The model file is to be parsed to determine the {@link File}s of any data file(s)
     * referenced by it. The common base path among the model file, its data file(s)
     * and any extra input files, should then be determined. Finally the Archive is
     * then to be created, populated with the files, in the directory structure
     * rooted at the derived common base path.
     * <p>
     * @param archiveFile - {@link File} specifying the archive file to be created
     * @param modelFile - {@link File} that specifies the model/control file
     * @param extraInputFiles - {@link List} of additional {@link File}s to be included in the Archive
     * @return the created {@link Archive}
     * @throws ArchiveException - if something fatal occurred during the addition of files to the archive 
     */
    Archive buildArchive(File archiveFile, File modelFile, Collection<File> extraInputFiles) throws ArchiveException;

}
