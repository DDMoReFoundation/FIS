/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers;

import java.util.Set;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Sets;

import eu.ddmore.fis.controllers.ClientError.JobNotFound;
import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.LocalJobStatus;
import eu.ddmore.fis.service.JobDispatcher;
import eu.ddmore.fis.service.LocalJobService;

@Controller
@RequestMapping("/jobs")
public class JobsController {
    private static final Logger LOG = Logger.getLogger(JobsController.class);
    
    @Autowired
	private LocalJobService localJobService;
    @Autowired
	private JobDispatcher jobDispatcher;
	
	/**
	 * @return a set of jobs being processed
	 */
	@RequestMapping(method=RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
	@Description("Endpoint returning a list of all jobs being processed by the service.")
    public @ResponseBody Set<LocalJob> getJobs() {
		Set<LocalJob> jobs = Sets.newHashSet();
		for(LocalJob job : localJobService.getAll() ) {
			jobs.add(job);
		}
		return jobs;
    }
	/**
	 * Method responsible for handling job submission.
	 * @param submittedJob - a job entity
	 * @return a job on successful submission
	 * @throws RuntimeException representing service fault if submission can't be performed.
	 */
	@RequestMapping(method=RequestMethod.POST, produces={MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
	@Description("Endpoint responsible for accepting job entities for processing.")
    public @ResponseBody LocalJob submit(@Valid @RequestBody LocalJob submittedJob) {
    	LOG.debug(String.format("Received job for submission: %s", submittedJob));
    	LocalJob job = localJobService.init(submittedJob);
    	try {
    	    LocalJob dispatchedJob = jobDispatcher.dispatch(job);
            job = localJobService.save(dispatchedJob);
		} catch (RuntimeException e) {
		    LOG.error("Unable to submit job", e);
			// passing the exception to the client
			throw e;
		} catch (Exception e) {
		    LOG.error("Unable to submit job", e);
		    // wrap the exception and pass to the client
            throw new RuntimeException("Unable to submit job", e);
		}
	    return job;
    }

	/**
	 * @param jobId - a job id for which status should be retrieved.
	 * @return a job status
	 */
    @RequestMapping(value = "status/{jobId}", method=RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
	@Description("Endpoint returning a job status.")
    public @ResponseBody ResponseEntity<LocalJobStatus> getJobStatus(@PathVariable("jobId") String jobId) throws JobNotFound {
    	LocalJob localJob = localJobService.getJob(jobId);
    	if(localJob==null) {
            throw new JobNotFound(String.format("Job with id %s does not exist.", jobId));
    	}
        return new ResponseEntity<LocalJobStatus>(localJob.getStatus(), HttpStatus.OK);
    }

    /**
     * @param jobId - job id for which state should be returned
     * @return a job state
     */
    @RequestMapping(value = "{jobId}", method=RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
	@Description("Endpoint returning a job state.")
    public @ResponseBody ResponseEntity<LocalJob> getJob(@PathVariable("jobId") String jobId) throws JobNotFound {
    	LocalJob localJob = localJobService.getJob(jobId);
    	if(localJob==null) {
    		throw new JobNotFound(String.format("Job with id %s does not exist.", jobId));
    	}
        return new ResponseEntity<LocalJob>(localJob, HttpStatus.OK);
    }

    @ExceptionHandler(JobNotFound.class)
    public ResponseEntity<String> handleIOException(JobNotFound ex) {
      return new ResponseEntity<String>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new SubmittedLocalJobValidator());
    }
    
    public void setLocalJobService(LocalJobService localJobService) {
        this.localJobService = localJobService;
    }
    
    public LocalJobService getLocalJobService() {
        return localJobService;
    }

    public void setJobDispatcher(JobDispatcher jobDispatcher) {
        this.jobDispatcher = jobDispatcher;
    }
    
    public JobDispatcher getJobDispatcher() {
        return jobDispatcher;
    }
}
