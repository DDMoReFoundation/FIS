/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import eu.ddmore.fis.domain.LocalJob;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:META-INF/application-context.xml"})
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class LocalJobServiceIT {

	@Autowired
	LocalJobService localJobService;
	
	@Test
	public void createLocalJob() {
    	LocalJob job = localJobService.newJob();
    	job.setCommand("A test command");
    	job.setControlFile("The test control file");
        job.setWorkingDirectory("The test working directory");
    	job.setSubmitTime(new DateTime().toString());

    	localJobService.save(job);

    	Iterable<LocalJob> jobs = localJobService.getAll();
    	LocalJob stored = jobs.iterator().next();
    	assertEquals(stored.getControlFile(), job.getControlFile());
	}
	
}
