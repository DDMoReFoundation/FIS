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

import eu.ddmore.archive.ArchiveFactory;


/**
 * Contains NMTRAN-specific utility methods relating to the Archive functionality within FIS.
 * <p>
 * <a href="https://nonmem.iconplc.com/nonmem720/guides/iv.pdf">https://nonmem.iconplc.com/nonmem720/guides/iv.pdf</a>
 * pg 18 is the reference for the permitted filename syntax on the $DATA statement, that
 * needs to be catered for by {@link #gatherDataFilesFromReferencesInModelFile(File)}.
 */
public final class CtlArchiveCreator extends BaseArchiveCreator {

    private static final String DATA_STATEMENT = "$DATA";
    private static final String INFILE_STATEMENT = "$INFILE";

    /**
     * Constructor injecting the required {@link ArchiveFactory} dependency.
     * <p>
     * @param archiveFactory - instance of {@link ArchiveFactory}
     */
    public CtlArchiveCreator(final ArchiveFactory archiveFactory) {
        super(archiveFactory);
    }

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

        final Pattern dataStatementRegex = Pattern.compile("(?m)^\\s*(?:" + Pattern.quote(DATA_STATEMENT) + "|" + Pattern.quote(INFILE_STATEMENT) + ")\\s+([^\n]+)"); // (?m) turns on multi-line mode
        final Matcher dataStatementMatcher = dataStatementRegex.matcher(controlFileContents);
        while (dataStatementMatcher.find()) {
        
            // Parse the $DATA statement to extract the filename, without quotes but preserving embedded spaces and special characters
            final String dataStatementContent = dataStatementMatcher.group(1);
            String dataFilename;
            if (dataStatementContent.startsWith("\"")) {
                dataFilename = dataStatementContent.split("\"")[1];
            } else if (dataStatementContent.startsWith("'")) {
                dataFilename = dataStatementContent.split("'")[1];
            } else {
                dataFilename = dataStatementContent.split(" ")[0];
            }
            
            dataFiles.add(resolveRelativePathAgainstDirectoryOfModelFile(controlFile, dataFilename));
        }

        return dataFiles;
    }

}
