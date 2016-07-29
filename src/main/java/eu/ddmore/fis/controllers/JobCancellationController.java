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
package eu.ddmore.fis.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.ddmore.fis.controllers.ClientError.JobNotFound;
import eu.ddmore.fis.controllers.ClientError.JobStateConflict;
import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.LocalJobStatus;
import eu.ddmore.fis.service.JobCanceller;
import eu.ddmore.fis.service.LocalJobService;

/**
 * Controller handling job cancellation requests
 */
@Controller
@RequestMapping("/cmd/jobs/cancel")
public class JobCancellationController {
    @Autowired
    private LocalJobService localJobService;
    @Autowired
    private JobCanceller jobCanceller;
    
    /**
     * Cancels a job with the given id
     * @param jobId - a job id that should be cancelled
     * @return a most recent job state on success
     * @throws ClientError if there is a client error
     * @throws RuntimeException if there is an internal server error
     */
    @RequestMapping(value = "{jobId}", method=RequestMethod.POST, produces={MediaType.APPLICATION_JSON_VALUE})
    @Description("Endpoint for requesting job cancellation.")
    public @ResponseBody ResponseEntity<LocalJob> cancelJob(@PathVariable("jobId") String jobId) throws ClientError {
        LocalJob job = localJobService.getJob(jobId);
        if(job==null) {
            throw new JobStateConflict(String.format("Job %s does not exist.", jobId));
        }
        if(job.getStatus().isFinal()) {
            throw new JobStateConflict(String.format("Job %s is already in final state.", jobId));
        }
        if(LocalJobStatus.CANCELLING.equals(job.getStatus())) {
            throw new JobStateConflict(String.format("Job %s is already being cancelled", jobId));
        }
        return new ResponseEntity<LocalJob>(jobCanceller.cancel(job), HttpStatus.OK);
    }
    
    public void setJobCanceller(JobCanceller jobCanceller) {
        this.jobCanceller = jobCanceller;
    }

    public void setLocalJobService(LocalJobService localJobService) {
        this.localJobService = localJobService;
    }
    
    @ExceptionHandler(JobNotFound.class)
    public ResponseEntity<String> handleIOException(JobNotFound ex) {
      return new ResponseEntity<String>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(JobStateConflict.class)
    public ResponseEntity<String> handleIOException(JobStateConflict ex) {
      return new ResponseEntity<String>(ex.getMessage(), HttpStatus.CONFLICT);
    }

}
