/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.processors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.service.JobResourceProcessor;


/**
 * Tests {@link DelegatingJobProcessor}
 */
@RunWith(MockitoJUnitRunner.class)
public class DelegatingJobProcessorTest {

    @Mock 
    private LocalJob job;

    @Mock 
    private LocalJob resultJob;
    
    @Mock 
    private LocalJob resultJobFromOtherProcessor;
    
    @Mock
    private JobResourceProcessor processor;

    @Mock
    private JobResourceProcessor otherProcessor;
    
    @Mock
    private Predicate<LocalJob> alwaysTrue;
    
    @Mock
    private Predicate<LocalJob> alwaysFalse;
    
    @Before
    public void setUp() {
        when(alwaysTrue.apply(any(LocalJob.class))).thenReturn(true);
        when(alwaysFalse.apply(any(LocalJob.class))).thenReturn(false);
        when(processor.process(job)).thenReturn(resultJob);
        when(otherProcessor.process(job)).thenReturn(resultJobFromOtherProcessor);
        when(job.getId()).thenReturn("mock-job-id");
    }
    
    @Test(expected=NullPointerException.class)
    public void constructor_shouldThrowRuntimeExceptionIfProcessorsAreNull() {
        new DelegatingJobProcessor(null);
    }

    @Test(expected=NullPointerException.class)
    public void process_shouldThrowRuntimeExceptionIfJobIsNull() {
        Map<JobResourceProcessor, Predicate<LocalJob>> processors = Maps.newHashMap();
        processors.put(processor, alwaysTrue);
        
        DelegatingJobProcessor instance = new DelegatingJobProcessor(processors);
        
        instance.process(null);
    }

    @Test
    public void process_shouldNotThrowExceptionIfNoProcessorForGivenJobIsFound() {
        Map<JobResourceProcessor, Predicate<LocalJob>> processors = Maps.newHashMap();
        processors.put(processor, alwaysFalse);
        processors.put(processor, alwaysFalse);
        
        DelegatingJobProcessor instance = new DelegatingJobProcessor(processors);
        LocalJob result = instance.process(job);
        
        assertTrue(result==job);
    }

    @Test
    public void process_shouldDelegateProcessingToJobProcessorForWhichJobMeetsCriteria() {
        Map<JobResourceProcessor, Predicate<LocalJob>> processors = Maps.newHashMap();
        processors.put(processor, alwaysFalse);
        processors.put(otherProcessor, alwaysTrue);
        
        DelegatingJobProcessor instance = new DelegatingJobProcessor(processors);
        LocalJob result = instance.process(job);
        
        assertTrue(result==resultJobFromOtherProcessor);
        verify(otherProcessor).process(same(job));
    }

    @Test(expected=IllegalStateException.class)
    public void process_shouldThrowRuntimeExceptionIfJobWouldBeProcessedByMoreThanOneProcessor() {
        Map<JobResourceProcessor, Predicate<LocalJob>> processors = Maps.newHashMap();
        processors.put(processor, alwaysTrue);
        processors.put(otherProcessor, alwaysTrue);
        
        DelegatingJobProcessor instance = new DelegatingJobProcessor(processors);
        instance.process(job);
    }

    @Test(expected=IllegalStateException.class)
    public void process_shouldThrowRuntimeExceptionIfJobProcessorReturnsNull() {
        Map<JobResourceProcessor, Predicate<LocalJob>> processors = Maps.newHashMap();
        processors.put(processor, alwaysFalse);
        JobResourceProcessor returningNull = mock(JobResourceProcessor.class);
        when(returningNull.process(any(LocalJob.class))).thenReturn(null);
        processors.put(returningNull, alwaysTrue);
        
        DelegatingJobProcessor instance = new DelegatingJobProcessor(processors);
        instance.process(job);
    }
}
