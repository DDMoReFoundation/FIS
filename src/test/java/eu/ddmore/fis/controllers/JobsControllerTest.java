/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.service.JobDispatcher;
import eu.ddmore.fis.service.LocalJobService;


/**
 * Tests {@link JobsController}
 */
@RunWith(MockitoJUnitRunner.class) 
public class JobsControllerTest {

    private JobsController jobsController;
    
    @Mock
    private JobDispatcher mockJobDispatcher;
    
    @Mock
    private LocalJobService mockLocalJobService;

    @Before
    public void setUp() {
        this.jobsController = new JobsController();
        this.jobsController.setJobDispatcher(this.mockJobDispatcher);
        this.jobsController.setLocalJobService(this.mockLocalJobService);
    }

    @Test
    public void testSubmit() {
    
        final String jobId = "jobId";
        final String executionFile = "model.mdl";

        final LocalJob job = new LocalJob();
        job.setExecutionType("NONMEM");
        job.setCommandParameters("-myparam1 -myparam2");
        job.setExecutionFile(executionFile);
        job.setWorkingDirectory("C:\\Temp\\fisworkingdir");
        job.setExtraInputFiles(Arrays.asList("model.lst", "catab", "patab"));
        

        when(this.mockLocalJobService.init(same(job))).thenAnswer(new Answer<LocalJob>() {

			@Override
			public LocalJob answer(InvocationOnMock invocation)
					throws Throwable {
				LocalJob input = (LocalJob)invocation.getArguments()[0];
				input.setId(jobId);
				input.setSubmitTime("Submission time");
				return input;
			}
		}); 
        when(this.mockJobDispatcher.dispatch(same(job))).thenReturn(job);
        when(this.mockLocalJobService.save(same(job))).thenReturn(job);
        
        
        
        final LocalJob submittedJob = this.jobsController.submit(job);
        
        // Assertions on resulting state
        assertEquals("Checking the jobId on the returned Job", jobId, submittedJob.getId());
        assertEquals("Checking executionType on created LocalJob that was dispatched to the JobDispatcher",
        		job.getExecutionType(), submittedJob.getExecutionType());
        assertEquals("Checking controlFile on created LocalJob that was dispatched to the JobDispatcher",
        		job.getExecutionFile(), submittedJob.getExecutionFile());
        assertEquals("Checking extraInputFiles on created LocalJob that was dispatched to the JobDispatcher",
        		job.getExtraInputFiles(), submittedJob.getExtraInputFiles());
        assertEquals("Checking commandParameters on created LocalJob that was dispatched to the JobDispatcher",
        		job.getCommandParameters(), submittedJob.getCommandParameters());
        assertNotNull("Checking submitTime on created LocalJob that was dispatched to the JobDispatcher",
        		submittedJob.getSubmitTime());

        // Interaction verification
        verify(this.mockJobDispatcher).dispatch(same(job));
        verifyNoMoreInteractions(this.mockJobDispatcher);
        verify(this.mockLocalJobService).init(same(job));
        verify(this.mockLocalJobService).save(same(job));
        verifyNoMoreInteractions(this.mockLocalJobService);
    }
}
