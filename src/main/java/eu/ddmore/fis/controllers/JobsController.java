/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Sets;

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.LocalJobStatus;
import eu.ddmore.fis.service.LocalJobService;

@Controller
@RequestMapping("/jobs")
public class JobsController {

	@Autowired
	private LocalJobService localJobService;
	
	@RequestMapping(method=RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody Set<LocalJob> getJobs() {
		Set<LocalJob> jobs = Sets.newHashSet();
		for(LocalJob job : localJobService.getAll() ) {
			jobs.add(job);
		}
		return jobs;
    }

    @RequestMapping(value = "status/{jobId}", method=RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody LocalJobStatus getJobStatus(@PathVariable("jobId") String jobId) {
        return localJobService.getJobStatus(jobId);
    }

    @RequestMapping(value = "{jobId}", method=RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody LocalJob getJob(@PathVariable("jobId") String jobId) {
        return localJobService.getJob(jobId);
    }
    
	public void setLocalJobService(LocalJobService localJobService) {
		this.localJobService = localJobService;
	}

}
