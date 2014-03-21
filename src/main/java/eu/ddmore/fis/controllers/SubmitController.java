/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.SubmissionRequest;
import eu.ddmore.fis.domain.SubmissionResponse;
import eu.ddmore.fis.service.JobDispatcher;
import eu.ddmore.fis.service.LocalJobService;

@Controller
@RequestMapping("/submit")
public class SubmitController implements ApplicationContextAware {
    private static final Logger LOG = Logger.getLogger(SubmitController.class.getName());
	private LocalJobService localJobService;
	private ApplicationContext applicationContext;
	
	private JobDispatcher jobDispatcher;
	
	@RequestMapping(method=RequestMethod.POST, produces={MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody SubmissionResponse submit(@RequestParam(value="submissionRequest") SubmissionRequest submissionRequest) {
    	SubmissionResponse response = new SubmissionResponse();
    	LOG.debug("Command is " + submissionRequest);
    	LocalJob job = createJobForSubmissionRequest(submissionRequest);
    	try {
    	    LocalJob dispatchedJob = jobDispatcher.dispatch(job);
            job = localJobService.save(dispatchedJob);
		} catch (Exception e) {
		    String errorMsg = String.format("Couldn't execute task %s", submissionRequest);
			LOG.error(errorMsg, e);
			// passing the exception to the client
			throw new RuntimeException(errorMsg, e);
		}
    	response.setRequestID(job.getId());
	    return response;
    }

    private LocalJob createJobForSubmissionRequest(SubmissionRequest submissionRequest) {
        LocalJob job = localJobService.newJob();
        job.setCommand(submissionRequest.getCommand());
        job.setWorkingDirectory(submissionRequest.getWorkingDirectory());
        job.setControlFile(submissionRequest.getExecutionFile());
        job.setCommandParameters(submissionRequest.getCommandParameters());
        job.setSubmitTime(new DateTime().toString());
        return job;
    }

    @Required
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
    
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Required
    public void setLocalJobService(LocalJobService localJobService) {
        this.localJobService = localJobService;
    }

    
    public LocalJobService getLocalJobService() {
        return localJobService;
    }

    @Required
    public void setJobDispatcher(JobDispatcher jobDispatcher) {
        this.jobDispatcher = jobDispatcher;
    }
    
    public JobDispatcher getJobDispatcher() {
        return jobDispatcher;
    }

}