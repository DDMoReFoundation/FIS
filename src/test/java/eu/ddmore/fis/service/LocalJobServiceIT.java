/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.endpoint.ShutdownEndpoint;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import eu.ddmore.fis.CommonIntegrationTestContextConfiguration;
import eu.ddmore.fis.SystemPropertiesAware;
import eu.ddmore.fis.domain.LocalJob;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {CommonIntegrationTestContextConfiguration.class})
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class LocalJobServiceIT extends SystemPropertiesAware {
	@Autowired
	LocalJobService localJobService;
	
	@Test
	public void createLocalJob() {
    	LocalJob job = localJobService.newJob();
    	job.setCommand("A test command");
    	job.setControlFile("The test control file");
        job.setWorkingDirectory("The test working directory");
    	job.setSubmitTime(new DateTime().toString());

    	String jobId = job.getId();
    	localJobService.save(job);

    	LocalJob stored = localJobService.getJob(jobId);
    	assertEquals(stored.getControlFile(), job.getControlFile());
	}
	
}
