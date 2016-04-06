/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import eu.ddmore.fis.IntegrationTestParent;
import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.UserInfo;

@Transactional
@Rollback
public class LocalJobServiceIT extends IntegrationTestParent {
    private static final String MOCK_USER = "MOCK_USER";
    private static final String MOCK_PASSWORD = "MOCK_PASSWORD";
    private static final String MOCK_IDENTITY = "MOCK_IDENTITY";
    private static final String MOCK_IDENTITY_PASSPHRASE = "MOCK_IDENTITY_PASSPHRASE";
    
    @Autowired
    private LocalJobService localJobService;

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
        UserInfo userInfo = new UserInfo(MOCK_USER, MOCK_PASSWORD, MOCK_IDENTITY, MOCK_IDENTITY_PASSPHRASE, false);
        job.setUserInfo(userInfo);

        job = localJobService.init(job);

        String jobId = job.getId();
        localJobService.save(job);

        LocalJob stored = localJobService.getJob(jobId);
        assertEquals("Job's execution type should be set on the returned LocalJob", stored.getExecutionType(), job.getExecutionType());
        assertEquals("Job's control file should be set on the returned LocalJob", stored.getExecutionFile(), job.getExecutionFile());
        assertEquals("Job's working directory should be set on the returned LocalJob", stored.getWorkingDirectory(), job.getWorkingDirectory());
        assertEquals("Job's command parameters should be set on the returned LocalJob", stored.getCommandParameters(),
            job.getCommandParameters());
        assertEquals("Job's extra input files should be set on the returned LocalJob", stored.getExtraInputFiles(), job.getExtraInputFiles());
        assertEquals("Job's submit time should be set on the returned LocalJob", stored.getSubmitTime(), job.getSubmitTime());
        assertEquals("Job's output filenames include regex should be set on the returned LocalJob", stored.getResultsIncludeRegex(),
            job.getResultsIncludeRegex());
        assertEquals("Job's output filenames exclude regex should be set on the returned LocalJob", stored.getResultsExcludeRegex(),
            job.getResultsExcludeRegex());

        assertEquals("Job's user info user name should have expected value.", MOCK_USER, stored.getUserInfo().getUserName());
        assertEquals("Job's user info password should have expected value.", MOCK_PASSWORD, stored.getUserInfo().getPassword());
        assertEquals("Job's user info identity file should have expected value.", MOCK_IDENTITY, stored.getUserInfo().getIdentityFilePath());
        assertEquals("Job's user info identity file pass phrase should have expected value.", MOCK_IDENTITY_PASSPHRASE, stored.getUserInfo().getIdentityFilePassphrase());
        assertFalse("Job's user info execute as user should have expected value.", stored.getUserInfo().isExecuteAsUser());
    }

}
