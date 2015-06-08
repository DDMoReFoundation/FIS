/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import eu.ddmore.archive.ArchiveFactory;


/**
 * Contains NMTRAN-specific utility methods relating to the Archive functionality within FIS.
 */
public final class CtlArchiveCreator extends BaseArchiveCreator {

    private static final String DATA_STATEMENT = "$DATA";

    /**
     * Constructor injecting the required {@link ArchiveFactory} dependency.
     * <p>
     * @param archiveFactory - instance of {@link ArchiveFactory}
     */
    public CtlArchiveCreator(final ArchiveFactory archiveFactory) {
        super(archiveFactory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<File> gatherDataFilesFromReferencesInModelFile(final File controlFile) {
        final Collection<File> dataFiles = new ArrayList<File>();

        // Extract the $DATA statement from the control file (allowing for more than one if that is at all possible)
        
        String controlFileContents;
        try {
            controlFileContents = FileUtils.readFileToString(controlFile);
        } catch (final IOException ioe) {
            throw new RuntimeException("Unable to read control file " + controlFile, ioe);
        }
        final Pattern dataStatementRegex = Pattern.compile("(?s)" + Pattern.quote(DATA_STATEMENT) + "\\s+(\\S+)");
        final Matcher dataStatementMatcher = dataStatementRegex.matcher(controlFileContents);
        while (dataStatementMatcher.find()) {
            final File dataFile = new File(controlFile.getParentFile(), dataStatementMatcher.group(1));
            dataFiles.add(new File(FilenameUtils.normalize(dataFile.getAbsolutePath())));
        }

        return dataFiles;
    }

}
