/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.service.JobResourceProcessor;


/**
 * Delegates processing of a job to a {@link JobResourceProcessor} for which applicability criteria are being met
 */
public class DelegatingJobProcessor implements JobResourceProcessor {
    private static final Logger LOG = Logger.getLogger(DelegatingJobProcessor.class);
    private final Map<JobResourceProcessor, Predicate<LocalJob>> processors;
    /**
     * Creates a new instance which will use
     * 
     * @param processors the map holding processors and the applicability criteria
     */
    public DelegatingJobProcessor(Map<JobResourceProcessor, Predicate<LocalJob>> processors) {
        Preconditions.checkNotNull(processors, "Processors should be not null");
        this.processors = processors;
    }
    
    /**
     * Delegates processing of the job to the {@link JobResourceProcessor} for which Job fulfils criteria.
     * * If the job does not fulfil criteria of any of the job processors the job will not be changed
     * * if the job fulfils criteria of more than one job processor, a {@link IllegalStateException } is thrown 
     * 
     * @param job the job that should be processed
     */
    @Override
    public LocalJob process(LocalJob job) {
        Preconditions.checkNotNull(job, "Job can't be null");
        Preconditions.checkNotNull(job.getId(), "Job id can't be null");
        List<JobResourceProcessor> jobProcessors = new ArrayList<JobResourceProcessor>();
        for(Map.Entry<JobResourceProcessor, Predicate<LocalJob>> en: processors.entrySet()) {
            if(en.getValue().apply(job)) {
                jobProcessors.add(en.getKey());
            }
        }
        
        Preconditions.checkState(jobProcessors.size()<2, String.format("Too many processors are applicable to job %s: processors %s",job.getId(),jobProcessors));
        
        if(jobProcessors.size()==1) {
            LocalJob result = jobProcessors.get(0).process(job);
            Preconditions.checkState(result!=null, "Job Processor returned null job instance");
            return result;
        } else {
            LOG.warn(String.format("No processors found for job %s", job.getId()));
        }
        return job;
    }

}
