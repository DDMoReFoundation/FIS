/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FilenameUtils;

import eu.ddmore.archive.ArchiveFactory;


/**
 * Contains MDL-specific utility methods relating to the Archive functionality within FIS.
 */
public final class MdlArchiveCreator extends BaseArchiveCreator {

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

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<File> gatherDataFilesFromReferencesInModelFile(final File mdlFile) {
        final Collection<File> dataFiles = new ArrayList<File>();
        for (final File dataFile : this.mdlUtils.getDataFileFromMDL(mdlFile)) {
            dataFiles.add(new File(FilenameUtils.normalize(dataFile.getAbsolutePath())));
        }
        return dataFiles;
    }

}
