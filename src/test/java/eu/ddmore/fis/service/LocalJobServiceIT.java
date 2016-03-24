/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import eu.ddmore.fis.IntegrationTestParent;
import eu.ddmore.fis.domain.LocalJob;

@Transactional
@Rollback
public class LocalJobServiceIT extends IntegrationTestParent {

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
        assertEquals("Checking command parameters is set on the returned LocalJob", stored.getCommandParameters(),
            job.getCommandParameters());
        assertEquals("Checking extra input files is set on the returned LocalJob", stored.getExtraInputFiles(), job.getExtraInputFiles());
        assertEquals("Checking submit time is set on the returned LocalJob", stored.getSubmitTime(), job.getSubmitTime());
        assertEquals("Checking output filenames include regex is set on the returned LocalJob", stored.getResultsIncludeRegex(),
            job.getResultsIncludeRegex());
        assertEquals("Checking output filenames exclude regex is set on the returned LocalJob", stored.getResultsExcludeRegex(),
            job.getResultsExcludeRegex());

    }

}
