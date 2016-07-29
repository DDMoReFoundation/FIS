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
package eu.ddmore.fis.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.mango.mif.MIFHttpRestClient;
import com.mango.mif.client.api.rest.MIFResponse;
import com.mango.mif.client.api.rest.ResponseStatus;

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.LocalJobStatus;


/**
 * Component responsible for job cancellation
 */
@Component
public class JobCanceller {
    @Autowired
    private LocalJobService localJobService;
    @Autowired
    private MIFHttpRestClient mifClient;
    /**
     * Cancels a given job
     * @param job that should be cancelled
     */
    public LocalJob cancel(final LocalJob job) {
        Preconditions.checkNotNull(job, "Job can't be null.");
        Preconditions.checkArgument(StringUtils.isNotBlank(job.getId()), "Job must have a non-empty id.");
        Preconditions.checkArgument(!job.getStatus().isFinal(), "Job must have a non-empty id.");
        LocalJob localJob = localJobService.getJob(job.getId());
        
        MIFResponse mifResponse = mifClient.cancel(localJob.getId());
        if(ResponseStatus.SUCCESS.equals(mifResponse.getStatus())) {
            localJob.setStatus(LocalJobStatus.CANCELLING);
            return localJobService.save(localJob);
        }
        
        throw new IllegalStateException(String.format("Could not request cancellation of the job [%s] from MIF. Error message: %s",  job.getId(), mifResponse.getErrorMessage()));
    }
    
    public void setLocalJobService(LocalJobService localJobService) {
        this.localJobService = localJobService;
    }
    
    public void setMifClient(MIFHttpRestClient mifClient) {
        this.mifClient = mifClient;
    }
}
