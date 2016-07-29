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
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import eu.ddmore.archive.ArchiveFactory;


/**
 * Contains MDL-specific utility methods relating to the Archive functionality within FIS.
 */
public final class MdlArchiveCreator extends BaseArchiveCreator {
    private final static Logger LOG = Logger.getLogger(MdlArchiveCreator.class);

    private final MdlUtils mdlUtils;

    /**
     * Constructor injecting the required {@link ArchiveFactory} and {@link MdlUtils} dependencies.
     * <p>
     * @param archiveFactory - instance of {@link ArchiveFactory}
     * @param mdlUtils - instance of {@link MdlUtils}
     */
    public MdlArchiveCreator(final ArchiveFactory archiveFactory, final MdlUtils mdlUtils) {
        super(archiveFactory);
        this.mdlUtils = mdlUtils;
    }

    @Override
    protected Collection<File> gatherDataFilesFromReferencesInModelFile(final File mdlFile) {
        final Collection<File> dataFiles = new ArrayList<File>();
        
        final Collection<File> dataFileReferencesFromMDL = new ArrayList<>();
        try {
            dataFileReferencesFromMDL.addAll(this.mdlUtils.getDataFileFromMDL(mdlFile));
        } catch (org.eclipse.xtext.parser.ParseException pex) {
            LOG.error("Unable to parse MDL file to extract data file references; no data files will be included in the archive to be passed to the Converter Toolbox Service.");
        }
        
        for (final File dataFile : dataFileReferencesFromMDL) {
            dataFiles.add(new File(FilenameUtils.normalize(dataFile.getAbsolutePath())));
        }
        return dataFiles;
    }

}
