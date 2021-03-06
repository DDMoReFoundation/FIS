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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

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
	public void initJob() {
	
    	LocalJob job = new LocalJob();
    	job.setExecutionType("MY_CMD");
    	job.setExecutionFile("The test control file");
        job.setWorkingDirectory("The test working directory");
        job.setCommandParameters("-myparam1 -myparam2");
        job.setExtraInputFiles(Arrays.asList("models/testExtraInputFile1.lst", "../testExtraInputFile2.txt"));
    	job.setResultsIncludeRegex("include-pattern");
        job.setResultsExcludeRegex("exclude-pattern");
        
        job = localJobService.init(job);

    	String jobId = job.getId();
    	localJobService.save(job);

    	LocalJob stored = localJobService.getJob(jobId);
    	assertEquals("Checking execution type is set on the returned LocalJob", stored.getExecutionType(), job.getExecutionType());
    	assertEquals("Checking control file is set on the returned LocalJob", stored.getExecutionFile(), job.getExecutionFile());
    	assertEquals("Checking working directory is set on the returned LocalJob", stored.getWorkingDirectory(), job.getWorkingDirectory());
    	assertEquals("Checking command parameters is set on the returned LocalJob", stored.getCommandParameters(), job.getCommandParameters());
    	assertEquals("Checking extra input files is set on the returned LocalJob", stored.getExtraInputFiles(), job.getExtraInputFiles());
    	assertEquals("Checking submit time is set on the returned LocalJob", stored.getSubmitTime(), job.getSubmitTime());
    	assertEquals("Checking output filenames include regex is set on the returned LocalJob", stored.getResultsIncludeRegex(), job.getResultsIncludeRegex());
        assertEquals("Checking output filenames exclude regex is set on the returned LocalJob", stored.getResultsExcludeRegex(), job.getResultsExcludeRegex());
    	
	}
	
}
