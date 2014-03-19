/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.miflocal.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.annotation.Scheduled;

import com.mango.mif.MIFHttpRestClient;
import com.mango.mif.MIFJobStatusHelper;

import eu.ddmore.miflocal.controllers.JobResourceProcessor;
import eu.ddmore.miflocal.domain.LocalJob;
import eu.ddmore.miflocal.domain.LocalJobStatus;

/**
 * A background task being responsible for monitoring MIF jobs
 */
public class RemoteJobStatusPoller {
    private static final Logger LOG = Logger.getLogger(RemoteJobStatusPoller.class);

    private MIFHttpRestClient mifClient;
    
    private LocalJobService localJobService;

    private JobResourceProcessor jobResourceRetriever;
    
    @Scheduled(fixedRate=5000)
    public void retrieveJobStatuses() {
        LOG.debug("Updating job statuses");
        List<LocalJob> runningJobs = localJobService.getUncompletedJobs();
        for(LocalJob job : runningJobs) {
            updateJobStatus(job);
        }
    }
    
    private void updateJobStatus(LocalJob job) {
        LOG.debug(String.format("Job %s is %s", job.getId(), job.getStatus()));
        String remoteJobStatus = mifClient.checkStatus(job.getId());
        LOG.debug(String.format("MIF status is: %s",remoteJobStatus));
        if("NOT_AVAILABLE".equals(remoteJobStatus)) {
            return;
        }
        LocalJobStatus localJobStatus = toLocalStatus(remoteJobStatus);
        job.setStatus(localJobStatus);
        
        if(!LocalJobStatus.RUNNING.equals(localJobStatus)) {
            job = jobResourceRetriever.process(job);
        }
        
        localJobService.setJobStatus(job.getId(),job.getStatus());
    }

    private LocalJobStatus toLocalStatus(String remoteJobStatus) {
        if(MIFJobStatusHelper.running(remoteJobStatus)) {
            return LocalJobStatus.RUNNING;
        }
        if(MIFJobStatusHelper.cancelled(remoteJobStatus)) {
            return LocalJobStatus.CANCELLED;
        }
        if(MIFJobStatusHelper.failed(remoteJobStatus)) {
            return LocalJobStatus.FAILED;
        }
        if(MIFJobStatusHelper.complete(remoteJobStatus)) {
            return LocalJobStatus.COMPLETED;
        }
        throw new IllegalStateException(String.format("Unrecognized job state", remoteJobStatus));
    }
    
    public LocalJobService getLocalJobService() {
        return localJobService;
    }

    @Required
    public void setLocalJobService(LocalJobService localJobService) {
        this.localJobService = localJobService;
    }
    
    public MIFHttpRestClient getMifClient() {
        return mifClient;
    }

    @Required
    public void setMifClient(MIFHttpRestClient mifClient) {
        this.mifClient = mifClient;
    }
    
    @Required
    public void setJobResourceRetriever(JobResourceProcessor jobResourceRetriever) {
        this.jobResourceRetriever = jobResourceRetriever;
    }
    
    public JobResourceProcessor getJobResourceRetriever() {
        return jobResourceRetriever;
    }
}
