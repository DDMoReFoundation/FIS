/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
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
        when(job.getControlFile()).thenReturn("");
        instance.apply(job);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void apply_shouldThrowExceptionIfInputFileIsNull() {
        when(job.getControlFile()).thenReturn(null);
        instance.apply(job);
    }
    
    @Test
    public void apply_shouldReturn_TRUE_IfInputFileHas_EXT_extension() {
        when(job.getControlFile()).thenReturn("my-file-with.EXT");
        assertTrue(instance.apply(job));
    }

    @Test
    public void apply_shouldReturn_FALSE_IfInputFileHasNot_EXT_extension() {
        when(job.getControlFile()).thenReturn("my-file-with.NON-EXT");
        assertFalse(instance.apply(job));
    }

    @Test
    public void apply_shouldReturn_TRUE_IfInputFileHas_EXT_extension_and_no_basename() {
        when(job.getControlFile()).thenReturn(".EXT");
        assertTrue(instance.apply(job));
    }

    @Test
    public void apply_shouldReturn_TRUE_IfInputFileHas_EXT_extension_andDirectoryPath() {
        when(job.getControlFile()).thenReturn("/this/is/some/directory/structure/filename.EXT");
        assertTrue(instance.apply(job));
    }


    @Test
    public void apply_shouldReturn_TRUE_IfInputFileHas_EXT_extension_andDirectoryPathWithWindowsFilePathSeparator() {
        when(job.getControlFile()).thenReturn("c:\\this\\is\\some\\directory\\structure\\filename.EXT");
        assertTrue(instance.apply(job));
    }
    
    @Test
    public void apply_shouldReturn_FALSE_IfInputFileHas_DOT_EXT_inBasenameButNotExtension_EXT() {
        when(job.getControlFile()).thenReturn(".EXT.incorrect");
        assertFalse(instance.apply(job));
    }
}
