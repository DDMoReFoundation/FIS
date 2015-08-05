/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.mockito.runners.MockitoJUnitRunner;

import com.mango.mif.MIFHttpRestClient;
import com.mango.mif.client.api.rest.MIFResponse;
import com.mango.mif.client.api.rest.ResponseStatus;

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.LocalJobStatus;


/**
 * Tests {@link JobCanceller}
 */
@RunWith(MockitoJUnitRunner.class)
public class JobCancellerTest {

    private
    @Mock MIFHttpRestClient mifClient;
    
    private
    @Mock LocalJobService localJobService;
    
    @InjectMocks
    private JobCanceller instance;
    
    @Test(expected=NullPointerException.class)
    public void cancel_shouldThrowNullPointerExceptionIfJobNull() {
        instance.cancel(null);
    }
    @Test(expected=IllegalArgumentException.class)
    public void cancel_shouldThrowIllegalArgumentExceptionIfJobHasNoId() {
        instance.cancel(mock(LocalJob.class));
    }

    @Test(expected=IllegalArgumentException.class)
    public void cancel_shouldThrowIllegalArgumentExceptionIfJobIsInFinalStatus() {
        LocalJob job = mock(LocalJob.class);
        when(job.getId()).thenReturn("ID");
        when(job.getStatus()).thenReturn(LocalJobStatus.COMPLETED);
        instance.cancel(job);
    }
    
    @Test
    public void shouldReturnNewJobStatusIfCancellationRequestSuccessfullySubmittedToMIF() {
        LocalJob job = mock(LocalJob.class);
        when(job.getId()).thenReturn("ID");
        when(job.getStatus()).thenReturn(LocalJobStatus.RUNNING);
        
        when(localJobService.getJob("ID")).thenReturn(job);
        LocalJob newJob = mock(LocalJob.class);
        when(localJobService.save(same(job))).thenReturn(newJob);

        MIFResponse response = new MIFResponse();
        response.setStatus(ResponseStatus.SUCCESS);
        when(mifClient.cancel("ID")).thenReturn(response);
        
        LocalJob result = instance.cancel(job);
        
        verify(job).setStatus(eq(LocalJobStatus.CANCELLING));
        verify(localJobService).save(same(job));
        assertNotNull("Checking that the cancellation result is not null", result);
    }
    
    @Test(expected=IllegalStateException.class)
    public void shouldThrowIllegalStateExceptionIfMIFRejectedCancellationRequest() {
        LocalJob job = mock(LocalJob.class);
        when(job.getId()).thenReturn("ID");
        when(job.getStatus()).thenReturn(LocalJobStatus.RUNNING);
        
        when(localJobService.getJob("ID")).thenReturn(job);
        LocalJob newJob = mock(LocalJob.class);
        when(localJobService.save(same(job))).thenReturn(newJob);
        
        MIFResponse response = new MIFResponse();
        response.setStatus(ResponseStatus.FAILURE);
        when(mifClient.cancel("ID")).thenReturn(response);
        
        LocalJob result = instance.cancel(job);
        
        verify(job).setStatus(eq(LocalJobStatus.CANCELLING));
        verify(localJobService).save(same(job));
        assertNotNull("Checking that the cancellation result is not null", result);
    }
}
