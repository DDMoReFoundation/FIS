/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.LocalJobStatus;
import eu.ddmore.fis.repository.LocalJobRepository;
/**
 * Service responsible for handling job entities
 */
@Service
public class LocalJobService {
    private static final Logger LOG = Logger.getLogger(LocalJobService.class);

	@Autowired
    private LocalJobRepository localJobRepository;
	
	@Transactional(readOnly=true)
    public Iterable<LocalJob> getAll() {
	    Preconditions.checkNotNull(localJobRepository, "Local Job Repository must be set");
        return getLocalJobRepository().findAll();
    }

    @Transactional(readOnly=false)
    public LocalJob save(LocalJob localJob) {
        Preconditions.checkNotNull(localJobRepository, "Local Job Repository must be set");
        Preconditions.checkArgument(localJob!=null, "Job can't be null");
        LOG.debug(String.format("Saving job %s", localJob));
        logTxStatus();
        return getLocalJobRepository().save(localJob);
    }

    public LocalJob init(LocalJob newJob) {
        newJob.setId(UUID.randomUUID().toString());
        newJob.setStatus(LocalJobStatus.NEW);
        newJob.setSubmitTime(new DateTime().toString());
        return newJob;
    }

    @Transactional(readOnly=true)
    public List<LocalJob> getUncompletedJobs() {
        Preconditions.checkNotNull(localJobRepository, "Local Job Repository must be set");
        List<LocalJob> jobs = Lists.newArrayList();
        jobs.addAll(getLocalJobRepository().findByStatus(LocalJobStatus.RUNNING));
        jobs.addAll(getLocalJobRepository().findByStatus(LocalJobStatus.NEW));
        return jobs;
    }

    private LocalJobRepository getLocalJobRepository() {
        return localJobRepository;
    }
    
    public void setLocalJobRepository(LocalJobRepository localJobRepository) {
        this.localJobRepository = localJobRepository;
    }

    @Transactional(readOnly=true)
    public LocalJob getJob(String jobId) {
        Preconditions.checkNotNull(localJobRepository, "Local Job Repository must be set");
        Preconditions.checkArgument(localJobRepository!=null, "Job id can't be null");
        return getLocalJobRepository().findOne(jobId);
    }

    @Transactional(readOnly=true)
    public LocalJobStatus getJobStatus(String jobId) {
        Preconditions.checkNotNull(localJobRepository, "Local Job Repository must be set");
        Preconditions.checkArgument(localJobRepository!=null, "Job id can't be null");
        LOG.debug(String.format("Getting job %s status", jobId));
        return getLocalJobRepository().findOne(jobId).getStatus();
    }

    @Transactional(readOnly=false)
    public void setJobStatus(String jobId, LocalJobStatus status) {
        Preconditions.checkNotNull(localJobRepository, "Local Job Repository must be set");
        Preconditions.checkArgument(localJobRepository!=null, "Job ID can't be null");
        Preconditions.checkArgument(status!=null, "Job status can't be null");
        
        LOG.debug(String.format("Fetching job %s and setting its status to %s", jobId, status));
        logTxStatus();
        
        LocalJob localJob = getLocalJobRepository().findOne(jobId);
        Preconditions.checkArgument(localJob != null, String.format("No job found with ID %s", jobId));
        localJob.setStatus(status);
        getLocalJobRepository().save(localJob);
    }
    
    private void logTxStatus() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            LOG.debug(String.format("Tx active [ JobManagementService = %s ] [ Thread = %s ]", this.hashCode(), Thread.currentThread().getId()) );
        } else {
            LOG.warn(String.format("No tx Active [ JobManagementService = %s ] [ Thread = %s ]", this.hashCode(), Thread.currentThread().getId()) );
        }
    }
}
