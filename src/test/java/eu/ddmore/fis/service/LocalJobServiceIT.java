/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
    	job.setExecutionType("MY_CMD");
    	job.setControlFile("The test control file");
        job.setWorkingDirectory("The test working directory");
        job.setCommandParameters("-myparam1 -myparam2");
        job.setExtraInputFiles(Arrays.asList("Test Extra File 1", "Test Extra File 2"));
    	job.setSubmitTime(new DateTime().toString());
    	job.setOutputFilenamesRegex(".*");

    	String jobId = job.getId();
    	localJobService.save(job);

    	LocalJob stored = localJobService.getJob(jobId);
    	assertEquals("Checking execution type is set on the returned LocalJob", stored.getExecutionType(), job.getExecutionType());
    	assertEquals("Checking control file is set on the returned LocalJob", stored.getControlFile(), job.getControlFile());
    	assertEquals("Checking working directory is set on the returned LocalJob", stored.getWorkingDirectory(), job.getWorkingDirectory());
    	assertEquals("Checking command parameters is set on the returned LocalJob", stored.getCommandParameters(), job.getCommandParameters());
    	assertEquals("Checking extra input files is set on the returned LocalJob", stored.getExtraInputFiles(), job.getExtraInputFiles());
    	assertEquals("Checking submit time is set on the returned LocalJob", stored.getSubmitTime(), job.getSubmitTime());
    	assertEquals("Checking output filenames regex is set on the returned LocalJob", stored.getOutputFilenamesRegex(), job.getOutputFilenamesRegex());
    	
	}
	
}
