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
        Preconditions.checkArgument(StringUtils.isNotBlank(job.getExecutionFile()),"Job's input file was not set or blank.");
        return filenamePattern.matcher(job.getExecutionFile()).matches();
    }

}
