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
