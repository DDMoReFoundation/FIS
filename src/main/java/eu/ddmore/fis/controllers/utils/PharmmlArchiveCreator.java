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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;

import eu.ddmore.archive.ArchiveFactory;
import eu.ddmore.libpharmml.ILibPharmML;
import eu.ddmore.libpharmml.IPharmMLResource;
import eu.ddmore.libpharmml.dom.trialdesign.ExternalDataSet;


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

    @Override
    protected Collection<File> gatherDataFilesFromReferencesInModelFile(final File pharmmlFile) {
        final Collection<File> dataFiles = new ArrayList<File>();

        IPharmMLResource pharmMLResource;
        try {
            pharmMLResource = this.libPharmML.createDomFromResource(FileUtils.openInputStream(pharmmlFile));
        } catch (final IOException ioe) {
            throw new RuntimeException("Unable to read PharmML file " + pharmmlFile, ioe);
        }
        final List<ExternalDataSet> extDataSets = pharmMLResource.getDom().getTrialDesign().getListOfExternalDataSet();
        if (CollectionUtils.isNotEmpty(extDataSets)) {
            for (final ExternalDataSet extDataSet : extDataSets) {
                final String dataFilePath = extDataSet.getDataSet().getExternalFile().getPath();
                dataFiles.add(resolveRelativePathAgainstDirectoryOfModelFile(pharmmlFile, dataFilePath));
            }
        }

        return dataFiles;
    }

}
