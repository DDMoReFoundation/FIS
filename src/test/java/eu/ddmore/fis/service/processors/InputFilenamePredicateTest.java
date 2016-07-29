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

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.ddmore.fis.domain.LocalJob;


/**
 * Tests {@link InputFilenamePredicate}
 */
@RunWith(MockitoJUnitRunner.class)
public class InputFilenamePredicateTest {

    @Mock
    private LocalJob job;

    private final InputFilenamePredicate instance = new InputFilenamePredicate(".*\\.EXT$");
    
    @Test(expected = IllegalArgumentException.class)
    public void constructor_shouldThrowExceptionIfFilenamePatternIsBlank() {
        new InputFilenamePredicate("");
    }
    
    @Test(expected = NullPointerException.class)
    public void constructor_shouldThrowExceptionIfFilenamePatternIsNull() {
        new InputFilenamePredicate(null);
    }
    
    
    @Test(expected = NullPointerException.class)
    public void apply_shouldThrowNullPointerExceptionForNullJob() {
        instance.apply(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void apply_shouldThrowExceptionIfInputFileIsEmpty() {
        when(job.getExecutionFile()).thenReturn("");
        instance.apply(job);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void apply_shouldThrowExceptionIfInputFileIsNull() {
        when(job.getExecutionFile()).thenReturn(null);
        instance.apply(job);
    }
    
    @Test
    public void apply_shouldReturn_TRUE_IfInputFileHas_EXT_extension() {
        when(job.getExecutionFile()).thenReturn("my-file-with.EXT");
        assertTrue(instance.apply(job));
    }

    @Test
    public void apply_shouldReturn_FALSE_IfInputFileHasNot_EXT_extension() {
        when(job.getExecutionFile()).thenReturn("my-file-with.NON-EXT");
        assertFalse(instance.apply(job));
    }

    @Test
    public void apply_shouldReturn_TRUE_IfInputFileHas_EXT_extension_and_no_basename() {
        when(job.getExecutionFile()).thenReturn(".EXT");
        assertTrue(instance.apply(job));
    }

    @Test
    public void apply_shouldReturn_TRUE_IfInputFileHas_EXT_extension_andDirectoryPath() {
        when(job.getExecutionFile()).thenReturn("/this/is/some/directory/structure/filename.EXT");
        assertTrue(instance.apply(job));
    }


    @Test
    public void apply_shouldReturn_TRUE_IfInputFileHas_EXT_extension_andDirectoryPathWithWindowsFilePathSeparator() {
        when(job.getExecutionFile()).thenReturn("c:\\this\\is\\some\\directory\\structure\\filename.EXT");
        assertTrue(instance.apply(job));
    }
    
    @Test
    public void apply_shouldReturn_FALSE_IfInputFileHas_DOT_EXT_inBasenameButNotExtension_EXT() {
        when(job.getExecutionFile()).thenReturn(".EXT.incorrect");
        assertFalse(instance.apply(job));
    }
}
