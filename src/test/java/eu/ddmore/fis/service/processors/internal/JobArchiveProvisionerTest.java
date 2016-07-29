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
package eu.ddmore.fis.service.processors.internal;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.Lists;

import eu.ddmore.archive.Archive;
import eu.ddmore.archive.Entry;
import eu.ddmore.archive.exception.ArchiveException;
import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.service.processors.internal.JobArchiveProvisioner;


/**
 * Tests {@link JobArchiveProvisioner}
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(FileUtils.class)
public class JobArchiveProvisionerTest {

    @Mock
    private File mifJobDirectory;
    
    @Mock
    private Archive archive;
    
    @Mock
    private LocalJob job;
    
    @Test(expected=NullPointerException.class)
    public void shouldThrowExceptionIfJobIsNull() throws ArchiveException, IOException {
        new JobArchiveProvisioner().provision(null, archive, mifJobDirectory);
    }

    @Test(expected=NullPointerException.class)
    public void shouldThrowExceptionIfArchiveIsNull() throws ArchiveException, IOException {
        new JobArchiveProvisioner().provision(job, null, mifJobDirectory);
    }

    @Test(expected=NullPointerException.class)
    public void shouldThrowExceptionIfMIFJobDirectoryIsNull() throws ArchiveException, IOException {
        new JobArchiveProvisioner().provision(job, archive, null);
    }

    @Test(expected=IllegalStateException.class)
    public void shouldThrowExceptionIfNoMainEntriesInArchive() throws ArchiveException, IOException {
        List<Entry> emptyEntries = Lists.newArrayList();
        when(archive.getMainEntries()).thenReturn(emptyEntries);
        new JobArchiveProvisioner().provision(job, archive, mifJobDirectory);
    }

    @Test
    public void shouldExtractArchiveIfMIFDoesntSupportArchive() throws ArchiveException, IOException {
        PowerMockito.mockStatic(FileUtils.class);
        Entry mainEntry = mock(Entry.class);
        when(mainEntry.getFilePath()).thenReturn("/path/to/control/file");
        List<Entry> mainEntries = Lists.newArrayList(mainEntry);
        when(archive.getMainEntries()).thenReturn(mainEntries);
        JobArchiveProvisioner provisioner = new JobArchiveProvisioner();
        
        provisioner.setMifArchiveSupport(false);
        
        provisioner.provision(job, archive, mifJobDirectory);
        
        verify(job).setExecutionFile("path/to/control/file"); //job must have a relative path to the control file
        verify(archive).extractArchiveTo(eq(mifJobDirectory));
        verify(archive).close();
        PowerMockito.verifyStatic(times(0));
    }
    
    @Test
    public void shouldCopyArchiveIfMIFSupportsArchive() throws ArchiveException, IOException {
        PowerMockito.mockStatic(FileUtils.class);
        
        Entry mainEntry = mock(Entry.class);
        when(mainEntry.getFilePath()).thenReturn("/path/to/control/file");
        List<Entry> mainEntries = Lists.newArrayList(mainEntry);
        when(archive.getMainEntries()).thenReturn(mainEntries);
        File archiveFile = new File("mock/archive/file.phex");
        when(archive.getArchiveFile()).thenReturn(archiveFile);
        JobArchiveProvisioner provisioner = new JobArchiveProvisioner();
        
        provisioner.setMifArchiveSupport(true);
        
        provisioner.provision(job, archive, mifJobDirectory);
        verify(job).setExecutionFile(eq("file.phex"));
        verify(archive).close();
        
        PowerMockito.verifyStatic(times(1));
    }
}
