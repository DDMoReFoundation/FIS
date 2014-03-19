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

import com.google.common.annotations.VisibleForTesting;
import com.mango.mif.MIFHttpRestClient;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.domain.ExecutionRequestBuilder;

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.SubmissionRequest;
import eu.ddmore.fis.domain.SubmissionResponse;
import eu.ddmore.fis.service.LocalJobService;

@Controller
@RequestMapping("/submit")
public class SubmitController implements ApplicationContextAware {
    /**
     * Logger
     */
    public static Logger LOG = Logger.getLogger(SubmitController.class.getName());
	private LocalJobService localJobService;
	private ApplicationContext applicationContext;
	private MIFHttpRestClient mifClient;
    private String mifUserName = "tel-user";
    private String converterToolboxPath;
    private String environmentSetupScript;
    private JobResourceProcessor jobResourcePublisher;
	
	@RequestMapping(method=RequestMethod.POST, produces={MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody SubmissionResponse submit(@RequestParam(value="submissionRequest") SubmissionRequest submissionRequest) {
    	SubmissionResponse response = new SubmissionResponse();
    	
    	LOG.debug("Command is " + submissionRequest);
    	
    	LocalJob job = createJobForSubmissionRequest(submissionRequest);

    	try {
    	    LocalJob publishedJob = jobResourcePublisher.process(job);
            ExecutionRequest executionRequest = buildExecutionRequest(publishedJob, "TEL service job", "NONMEM");
            mifClient.executeJob(executionRequest);
		} catch (Exception e) {
		    String errorMsg = String.format("Couldn't execute task %s", submissionRequest);
			LOG.error(errorMsg, e);
			// passing the exception to the client
			throw new RuntimeException(errorMsg, e);
		}
        job = localJobService.save(job);
    	response.setRequestID(job.getId());
	    return response;
    }
	
	private LocalJob createJobForSubmissionRequest(SubmissionRequest submissionRequest) {
	    LocalJob job = localJobService.newJob();
        job.setCommand(submissionRequest.getCommand());
        job.setWorkingDirectory(submissionRequest.getWorkingDirectory());
        job.setControlFile(submissionRequest.getExecutionFile());
        job.setSubmitTime(new DateTime().toString());
        return job;
    }

    @VisibleForTesting
    ExecutionRequest buildExecutionRequest(LocalJob job, String description,  String executionType) {
        ExecutionRequestBuilder requestBuilder = new ExecutionRequestBuilder().setRequestId(job.getId()).setName(description)
            .setExecutionType(executionType)
                .setExecutionFile(job.getControlFile()).setCommand(job.getCommand()).setSubmitAsUserMode(false);
        requestBuilder.addAttribute("EXECUTION_HOST_FILESHARE", job.getWorkingDirectory());
        requestBuilder.addAttribute("EXECUTION_HOST_FILESHARE_REMOTE", job.getWorkingDirectory());
        requestBuilder.setUserName(mifUserName);
        requestBuilder.addAttribute("CONVERTER_TOOLBOX_PATH", converterToolboxPath);
        requestBuilder.setSubmitHostPreamble(environmentSetupScript);
        return requestBuilder.getExecutionRequest();
    }

    @Required
	public void setLocalJobService(LocalJobService localJobService) {
		this.localJobService = localJobService;
	}


    @Required
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

    
    public MIFHttpRestClient getMifClient() {
        return mifClient;
    }


    @Required
    public void setMifClient(MIFHttpRestClient mifClient) {
        this.mifClient = mifClient;
    }

    
    public String getConverterToolboxPath() {
        return converterToolboxPath;
    }


    @Required
    public void setConverterToolboxPath(String converterToolboxPath) {
        this.converterToolboxPath = converterToolboxPath;
    }

    
    public String getEnvironmentSetupScript() {
        return environmentSetupScript;
    }


    @Required
    public void setEnvironmentSetupScript(String environmentSetupScript) {
        this.environmentSetupScript = environmentSetupScript;
    }

    
    public JobResourceProcessor getJobResourcePublisher() {
        return jobResourcePublisher;
    }

    @Required
    public void setJobResourcePublisher(JobResourceProcessor jobResourcePublisher) {
        this.jobResourcePublisher = jobResourcePublisher;
    }

    
    public LocalJobService getLocalJobService() {
        return localJobService;
    }

    
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }


}