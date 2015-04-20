/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.processors;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

import eu.ddmore.fis.domain.LocalJob;


/**
 * Predicate implementation that matches the name of the input file of the LocalJob against specified regular expression pattern
 */
public class InputFilenamePredicate implements Predicate<LocalJob> {

    private final Pattern filenamePattern;
    
    /**
     * @param filenamePattern a regular expression specifying the file name pattern
     */
    public InputFilenamePredicate(String filenamePattern) {
        Preconditions.checkNotNull(filenamePattern, "File name pattern can't be null.");
        Preconditions.checkArgument(StringUtils.isNotBlank(filenamePattern), "File name pattern was blank.");
        this.filenamePattern = Pattern.compile(filenamePattern);
    }
    
    /**
     * @param job the job which input filename should match the pattern of this predicate
     * @return true if the job's input file matches the matching pattern, false otherwise
     */
    @Override
    public boolean apply(LocalJob job) {
        Preconditions.checkNotNull(job, "Job can't be null.");
        Preconditions.checkArgument(StringUtils.isNotBlank(job.getControlFile()),"Job's input file was not set or blank.");
        return filenamePattern.matcher(job.getControlFile()).matches();
    }

}
