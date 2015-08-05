/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.mango.mif.MIFHttpRestClient;

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.LocalJobStatus;


/**
 * Tests {@link RemoteJobStatusPoller}
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoteJobStatusPollerTest {
    private 
    @Mock MIFHttpRestClient mifClient;
    
    private 
    @Mock LocalJobService localJobService;

    private 
    @Mock JobResourceProcessor jobResourceRetriever;
    
    private
    @InjectMocks
    RemoteJobStatusPoller instance;
    
    @Test
    public void toLocalStatus_shouldReturnCancellingEvenIfStatusIsRunning() {
        LocalJob job = mock(LocalJob.class);
        when(job.getStatus()).thenReturn(LocalJobStatus.CANCELLING);
        LocalJobStatus result = instance.toLocalStatus(job, "PROCESSING");
        assertEquals("Returned status should be", LocalJobStatus.CANCELLING, result);
    }

    @Test
    public void toLocalStatus_shouldReturnRunning() {
        LocalJob job = mock(LocalJob.class);
        when(job.getStatus()).thenReturn(LocalJobStatus.NEW);
        LocalJobStatus result = instance.toLocalStatus(job, "PROCESSING");
        assertEquals("Returned status should be", LocalJobStatus.RUNNING, result);
    }

    @Test
    public void toLocalStatus_shouldReturnCancelled() {
        LocalJob job = mock(LocalJob.class);
        when(job.getStatus()).thenReturn(LocalJobStatus.RUNNING);
        LocalJobStatus result = instance.toLocalStatus(job, "CANCELLED");
        assertEquals("Returned status should be", LocalJobStatus.CANCELLED, result);
    }

    @Test
    public void toLocalStatus_shouldReturnCompleted() {
        LocalJob job = mock(LocalJob.class);
        when(job.getStatus()).thenReturn(LocalJobStatus.RUNNING);
        LocalJobStatus result = instance.toLocalStatus(job, "COMPLETED");
        assertEquals("Returned status should be", LocalJobStatus.COMPLETED, result);
    }

    @Test
    public void toLocalStatus_shouldReturnFailed() {
        LocalJob job = mock(LocalJob.class);
        when(job.getStatus()).thenReturn(LocalJobStatus.RUNNING);
        LocalJobStatus result = instance.toLocalStatus(job, "FAILED");
        assertEquals("Returned status should be", LocalJobStatus.FAILED, result);
    }

    @Test
    public void toLocalStatus_shouldReturnCompletedEvenIfJobsStatusIsCancelling() {
        LocalJob job = mock(LocalJob.class);
        when(job.getStatus()).thenReturn(LocalJobStatus.CANCELLING);
        LocalJobStatus result = instance.toLocalStatus(job, "COMPLETED");
        assertEquals("Returned status should be", LocalJobStatus.COMPLETED, result);
    }

    @Test
    public void updateJobStatus_shouldNotUpdateJobStatusIfItIsNotAvailableOnMIF() {
        LocalJob job = mock(LocalJob.class);
        when(job.getId()).thenReturn("ID");
        when(mifClient.checkStatus("ID")).thenReturn("NOT_AVAILABLE");
        instance.updateJobStatus(job);
        verify(localJobService, never()).setJobStatus(eq("ID"), any(LocalJobStatus.class));
    }

    @Test
    public void updateJobStatus_shouldTriggerResultsRetrivalIfJobIsInFinalState() {
        LocalJob job = mock(LocalJob.class);
        when(job.getId()).thenReturn("ID");
        when(mifClient.checkStatus("ID")).thenReturn("COMPLETED");
        when(jobResourceRetriever.process(same(job))).thenReturn(job);
        instance.updateJobStatus(job);
        verify(localJobService).setJobStatus(eq("ID"), any(LocalJobStatus.class));
        verify(jobResourceRetriever).process(same(job));
    }

    @Test
    public void updateJobStatus_shouldJustUpdateJobStatusForJobInNonFinalState() {
        LocalJob job = mock(LocalJob.class);
        when(job.getId()).thenReturn("ID");
        when(mifClient.checkStatus("ID")).thenReturn("PROCESSING");
        instance.updateJobStatus(job);
        verify(localJobService).setJobStatus(eq("ID"), any(LocalJobStatus.class));
        verify(jobResourceRetriever, never()).process(same(job));
    }
}
