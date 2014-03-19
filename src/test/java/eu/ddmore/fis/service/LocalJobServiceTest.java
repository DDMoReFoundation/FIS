/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.miflocal.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

import eu.ddmore.miflocal.domain.LocalJob;
import eu.ddmore.miflocal.domain.LocalJobStatus;
import eu.ddmore.miflocal.repository.LocalJobRepository;

@RunWith(MockitoJUnitRunner.class)
public class LocalJobServiceTest {
    
    @Mock LocalJobRepository localJobRepository;
    
    @InjectMocks
	private LocalJobService localJobService = new LocalJobService();
	
	@Test
	public void shouldCreateNewJob() {
    	LocalJob job = localJobService.newJob();
    	assertEquals(job.getStatus(), LocalJobStatus.NEW);
	}

    @Test
    public void shouldReturnAllJobs() {
        localJobService.getAll();
        verify(localJobRepository, times(1)).findAll();
    }

    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowExceptionForNullJob() {
        localJobService.save(null);
    }
    
    @Test
    public void shouldSaveJob() {
        LocalJob job = mock(LocalJob.class);
        localJobService.save(job);
        verify(localJobRepository, times(1)).save(job);
    }

    @Test
    public void shouldReturnUncompletedJobs() {
        List<LocalJob> newJobs = Lists.newArrayList(createMockJob("1"), createMockJob("2"));
        List<LocalJob> runningJobs = Lists.newArrayList(createMockJob("10"), createMockJob("20"));
        when(localJobRepository.findByStatus(LocalJobStatus.NEW)).thenReturn(newJobs);
        when(localJobRepository.findByStatus(LocalJobStatus.RUNNING)).thenReturn(runningJobs);
        List<LocalJob> jobs = localJobService.getUncompletedJobs();
        assertEquals(jobs.size(),4);
    }

    @Test
    public void shouldReturnJobStatus() {
        LocalJob job = createMockJob("1");
        when(job.getStatus()).thenReturn(LocalJobStatus.RUNNING);
        when(localJobRepository.findOne("1")).thenReturn(job);
        localJobService.getJobStatus("1");
        verify(job).getStatus();
    }

    @Test
    public void shouldSetJobStatus() {
        LocalJob job = createMockJob("1");
        when(localJobRepository.findOne((String)any())).thenReturn(job);
        localJobService.setJobStatus("1",LocalJobStatus.RUNNING);
        verify(job).setStatus(eq(LocalJobStatus.RUNNING));
    }

    @Test
    public void shouldReturnJob() {
        LocalJob job = createMockJob("1");
        when(localJobRepository.findOne((String)any())).thenReturn(job);
        LocalJob result = localJobService.getJob("1");
        assertNotNull(result);
    }

    private LocalJob createMockJob(String id) {
        LocalJob job = mock(LocalJob.class);
        when(job.getId()).thenReturn(id);
        return job;
    }
}
