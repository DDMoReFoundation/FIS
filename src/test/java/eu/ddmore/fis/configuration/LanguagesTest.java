/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.configuration;

import static org.junit.Assert.*;

import org.junit.Test;

import eu.ddmore.convertertoolbox.domain.Version;


/**
 * Tests {@link Languages}
 */
public class LanguagesTest {

    @Test
    public void parseVersion_shouldParseVersionWithQualifier() {
        String versionStr = "1.0.0-qual";
        Version version = new Languages().parseVersion(versionStr);
        
        assertEquals(1,version.getMajor());
        assertEquals(0,version.getMinor());
        assertEquals(0,version.getPatch());
        assertEquals("qual",version.getQualifier());
    }

    @Test
    public void parseVersion_shouldParseVersionWithoutQualifier() {
        String versionStr = "1.0.0";
        Version version = new Languages().parseVersion(versionStr);
        assertEquals(1,version.getMajor());
        assertEquals(0,version.getMinor());
        assertEquals(0,version.getPatch());
        assertEquals("",version.getQualifier());
    }
    
    @Test
    public void parseVersion_shouldParseVersionWithEmptyQualifier() {
        String versionStr = "1.0.0-";
        Version version = new Languages().parseVersion(versionStr);
        assertEquals(1,version.getMajor());
        assertEquals(0,version.getMinor());
        assertEquals(0,version.getPatch());
        assertEquals("",version.getQualifier());
    }

    @Test(expected=IllegalArgumentException.class)
    public void parseVersion_shouldThrowExceptionIfMinorIsMissing() {
        String versionStr = "1..0";
        new Languages().parseVersion(versionStr);
    }
    
    @Test(expected=NumberFormatException.class)
    public void parseVersion_shouldThrowExceptionIfMinorNotNumber() {
        String versionStr = "1.sd.0";
        new Languages().parseVersion(versionStr);
    }
}
