/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
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
