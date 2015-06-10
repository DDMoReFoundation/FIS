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
import org.mockito.runners.MockitoJUnitRunner;

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.SubmissionRequest;
import eu.ddmore.fis.domain.SubmissionResponse;
import eu.ddmore.fis.service.JobDispatcher;
import eu.ddmore.fis.service.LocalJobService;


@RunWith(MockitoJUnitRunner.class) 
public class SubmitControllerTest {

    private SubmitController submitController;
    
    @Mock
    private JobDispatcher mockJobDispatcher;
    
    @Mock
    private LocalJobService mockLocalJobService;

    @Before
    public void setUp() {
        this.submitController = new SubmitController();
        this.submitController.setJobDispatcher(this.mockJobDispatcher);
        this.submitController.setLocalJobService(this.mockLocalJobService);
    }

    @Test
    public void testSubmit() {
    
        final String jobId = "jobId";
        final String executionFile = "model.mdl";

        // Set up expected behaviour
        final LocalJob jobForSubmissionRequest = new LocalJob(); // Properties are null; the service populates them
        final LocalJob dispatchedJob = new LocalJob();
        final LocalJob savedJob = new LocalJob();
        savedJob.setId(jobId);
        when(this.mockLocalJobService.newJob()).thenReturn(jobForSubmissionRequest); 
        when(this.mockJobDispatcher.dispatch(same(jobForSubmissionRequest))).thenReturn(dispatchedJob);
        when(this.mockLocalJobService.save(same(dispatchedJob))).thenReturn(savedJob);
        
        // Call method under test
        final SubmissionRequest submissionRequest = new SubmissionRequest();
        submissionRequest.setCommand("NONMEM");
        submissionRequest.setCommandParameters("-myparam1 -myparam2");
        submissionRequest.setExecutionFile(executionFile);
        submissionRequest.setWorkingDirectory("C:\\Temp\\fisworkingdir");
        submissionRequest.setExtraInputFiles(Arrays.asList("model.lst", "catab", "patab"));
        final SubmissionResponse submitResponse = this.submitController.submit(submissionRequest);
        
        // Assertions on resulting state
        assertEquals("Checking the jobId on the returned SubmissionResponse", jobId, submitResponse.getRequestID());
        assertEquals("Checking executionType on created LocalJob that was dispatched to the JobDispatcher",
            submissionRequest.getCommand(), jobForSubmissionRequest.getExecutionType());
        assertEquals("Checking controlFile on created LocalJob that was dispatched to the JobDispatcher",
            submissionRequest.getExecutionFile(), jobForSubmissionRequest.getControlFile());
        assertEquals("Checking extraInputFiles on created LocalJob that was dispatched to the JobDispatcher",
            submissionRequest.getExtraInputFiles(), jobForSubmissionRequest.getExtraInputFiles());
        assertEquals("Checking commandParameters on created LocalJob that was dispatched to the JobDispatcher",
            submissionRequest.getCommandParameters(), jobForSubmissionRequest.getCommandParameters());
        assertNotNull("Checking submitTime on created LocalJob that was dispatched to the JobDispatcher",
            jobForSubmissionRequest.getSubmitTime());

        // Interaction verification
        verify(this.mockJobDispatcher).dispatch(same(jobForSubmissionRequest));
        verifyNoMoreInteractions(this.mockJobDispatcher);
        verify(this.mockLocalJobService).newJob();
        verify(this.mockLocalJobService).save(same(dispatchedJob));
        verifyNoMoreInteractions(this.mockLocalJobService);
    }

}
