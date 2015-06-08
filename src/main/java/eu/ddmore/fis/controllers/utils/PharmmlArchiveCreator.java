/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import eu.ddmore.archive.ArchiveFactory;
import eu.ddmore.libpharmml.ILibPharmML;
import eu.ddmore.libpharmml.IPharmMLResource;
import eu.ddmore.libpharmml.dom.modellingsteps.ExternalDataSet;


/**
 * Contains PharmML-specific utility methods relating to the Archive functionality within FIS.
 */
public final class PharmmlArchiveCreator extends BaseArchiveCreator {

    private final ILibPharmML libPharmML;

    /**
     * Constructor injecting the required {@link ArchiveFactory} and {@link ILibPharmML} dependencies.
     * <p>
     * @param archiveFactory - instance of {@link ArchiveFactory}
     * @param libPharmML - instance of {@link ILibPharmML}
     */
    public PharmmlArchiveCreator(final ArchiveFactory archiveFactory, final ILibPharmML libPharmML) {
        super(archiveFactory);
        this.libPharmML = libPharmML;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<File> gatherDataFilesFromReferencesInModelFile(final File pharmmlFile) {
        final Collection<File> dataFiles = new ArrayList<File>();

        IPharmMLResource pharmMLResource;
        try {
            pharmMLResource = this.libPharmML.createDomFromResource(FileUtils.openInputStream(pharmmlFile));
        } catch (final IOException ioe) {
            throw new RuntimeException("Unable to read PharmML file " + pharmmlFile, ioe);
        }
        final List<ExternalDataSet> extDataSets = pharmMLResource.getDom().getModellingSteps().getListOfExternalDataSet();
        if (CollectionUtils.isNotEmpty(extDataSets)) {
            for (final ExternalDataSet extDataSet : extDataSets) {
                final String dataFilePath = extDataSet.getDataSet().getExternalFile().getPath();
                final File dataFile = new File(pharmmlFile.getParentFile(), dataFilePath);
                dataFiles.add(new File(FilenameUtils.normalize(dataFile.getAbsolutePath())));
            }
        }

        return dataFiles;
    }

}
