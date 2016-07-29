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
package eu.ddmore.fis.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import eu.ddmore.fis.CommonIntegrationTestContextConfiguration;
import eu.ddmore.fis.SystemPropertiesAware;
import eu.ddmore.fis.configuration.RestClientConfiguration;
import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.LocalJobStatus;
import eu.ddmore.fis.service.LocalJobService;

/**
 * Integration test for {@link JobsController}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { CommonIntegrationTestContextConfiguration.class,RestClientConfiguration.class })
@WebAppConfiguration
@IntegrationTest({"server.port=0", "management.port=0"}) //let the framework choose the port
public class JobsControllerIT extends SystemPropertiesAware {
    private final Logger LOG = Logger.getLogger(JobsControllerIT.class);
    private static final String URL = "http://localhost";
    @Autowired
    private EmbeddedWebApplicationContext server;

    @Autowired
    private RestTemplate restTemplate;
    
    private static final String EXECUTION_TYPE_1 = "NONMEM";
    private static final String EXECUTION_TYPE_2 = "MONOLIX";
    private static final String MODEL_FILE_1 = "models/mymodel1.mdl";
    private static final String MODEL_FILE_2 = "mymodel2.mdl";
    private static final List<String> EXTRA_INPUT_FILES_1 = Arrays.asList("model1.lst", "../model1.txt");
    private static final List<String> EXTRA_INPUT_FILES_2 = Arrays.asList("model2.lst", "other/model2.txt");
    private static final String COMMAND_PARAMS = "-myparam1 -myparam2";
    private static final String WORKING_DIR = "C:\\Temp\\fisworkingdir";
    private static final String RESULTS_INCLUDE_REGEX = ".*\\..*";
    private static final String RESULTS_EXCLUDE_REGEX = ".*\\.exe";

    private String jobId1;
    private String jobId2;

    // Class under test
    @Autowired
    private JobsController jobsController;

    // Used to get a Job into the database
    @Autowired
    private LocalJobService localJobService;

    private LocalJob savedLocalJob1;
    private LocalJob savedLocalJob2;

    @Before
    public void setUp() {
        this.jobId1 = UUID.randomUUID().toString();
        this.jobId2 = UUID.randomUUID().toString();

        this.savedLocalJob1 = new LocalJob();
        this.savedLocalJob1.setId(this.jobId1);
        this.savedLocalJob1.setExecutionType(EXECUTION_TYPE_1);
        this.savedLocalJob1.setExecutionFile(MODEL_FILE_1);
        this.savedLocalJob1.setExtraInputFiles(EXTRA_INPUT_FILES_1);
        this.savedLocalJob1.setCommandParameters(COMMAND_PARAMS);
        this.savedLocalJob1.setWorkingDirectory(WORKING_DIR);
        this.savedLocalJob1.setResultsIncludeRegex(RESULTS_INCLUDE_REGEX);
        this.savedLocalJob1.setResultsExcludeRegex(RESULTS_EXCLUDE_REGEX);
        this.savedLocalJob1.setStatus(LocalJobStatus.RUNNING);
        this.savedLocalJob1.setSubmitTime(new DateTime().toString());
        
        this.savedLocalJob2 = new LocalJob();
        this.savedLocalJob2.setId(this.jobId2);
        this.savedLocalJob2.setExecutionType(EXECUTION_TYPE_2);
        this.savedLocalJob2.setExecutionFile(MODEL_FILE_2);
        this.savedLocalJob2.setExtraInputFiles(EXTRA_INPUT_FILES_2);
        this.savedLocalJob2.setCommandParameters(COMMAND_PARAMS);
        this.savedLocalJob2.setWorkingDirectory(WORKING_DIR);
        this.savedLocalJob2.setResultsIncludeRegex(RESULTS_INCLUDE_REGEX);
        this.savedLocalJob2.setResultsExcludeRegex(RESULTS_EXCLUDE_REGEX);
        this.savedLocalJob2.setStatus(LocalJobStatus.NEW);
        this.savedLocalJob2.setSubmitTime(new DateTime().toString());

        // Get the Jobs into the database
        this.localJobService.save(this.savedLocalJob1);
        this.localJobService.save(this.savedLocalJob2);
        
    }

    @After
    public void tearDown() {
        // Finish off the Jobs
        this.localJobService.setJobStatus(this.savedLocalJob1.getId(), LocalJobStatus.COMPLETED);
        this.localJobService.setJobStatus(this.savedLocalJob2.getId(), LocalJobStatus.COMPLETED);
    }

    @Test
    public void getJob_shouldReturnJob() {
        final ResponseEntity<LocalJob> retrievedJob = restTemplate.getForEntity(generateEndpoint("jobs/{jobId}"), LocalJob.class, this.jobId1);
        LocalJob job = retrievedJob.getBody();
        LOG.info(String.format("Retrieved job: %s (modelfile=%s)", job.getId(), job.getExecutionFile()));
        assertNotNull("Matching Job should be found and retrieved", job);
        verifyJob1(job);
    }

    @Test(expected=HttpClientErrorException.class)
    public void getJob_shouldResultIn40xStatusIfJobDoesNotExist() {
        restTemplate.getForEntity(generateEndpoint("jobs/{jobId}"), LocalJob.class, "NON_EXISTING_JOB");
    }

    @Test
    public void getJobs_shouldReturnAllJobs() {
        final ResponseEntity<List<LocalJob>> retrievedJobsResource = restTemplate.exchange(generateEndpoint("jobs"), HttpMethod.GET, null, new ParameterizedTypeReference<List<LocalJob>>(){} );
        List<LocalJob> retrievedJobs = retrievedJobsResource.getBody();
        assertTrue("Should be at least two jobs returned (other integration tests may have populated the database with others)", retrievedJobs.size() > 2);

        final LocalJob retrievedJob1 = CollectionUtils.find(retrievedJobs, new Predicate<LocalJob>() {
            @Override
            public boolean evaluate(final LocalJob localJob) {
                return JobsControllerIT.this.jobId1.equals(localJob.getId());
            }
        });
        final LocalJob retrievedJob2 = CollectionUtils.find(retrievedJobs, new Predicate<LocalJob>() {
            @Override
            public boolean evaluate(final LocalJob localJob) {
                return JobsControllerIT.this.jobId2.equals(localJob.getId());
            }
        });
        
        assertNotNull("Returned jobs list should contain job 1", retrievedJob1);
        verifyJob1(retrievedJob1);
        assertNotNull("Returned jobs list should contain job 2", retrievedJob2);
        verifyJob2(retrievedJob2);
    }
    
    @Test
    public void getJobStatus_shouldReturnJobStatus() {
        final ResponseEntity<LocalJobStatus> retrievedJobStatus = restTemplate.getForEntity(generateEndpoint("jobs/status/{jobId}"), LocalJobStatus.class, this.jobId1);
        assertEquals("Checking the returned job status", LocalJobStatus.RUNNING, retrievedJobStatus.getBody());
    }
    
    @Test(expected=HttpClientErrorException.class)
    public void getJobStatus_shouldResultIn40xStatusIfJobDoesNotExist() {
        restTemplate.getForEntity(generateEndpoint("jobs/status/{jobId}"), LocalJobStatus.class, "NON_EXISTING_JOB");
    }
    
    private void verifyJob1(final LocalJob localJob1) {
        assertEquals("Checking ID of returned job", this.jobId1, localJob1.getId());
        assertEquals("Checking status of returned job", LocalJobStatus.RUNNING, localJob1.getStatus());        
        assertEquals("Checking execution type of returned job", EXECUTION_TYPE_1, localJob1.getExecutionType());
        assertEquals("Checking model/control file of returned job", MODEL_FILE_1, localJob1.getExecutionFile());
        assertEquals("Checking extra input files of returned job", EXTRA_INPUT_FILES_1, localJob1.getExtraInputFiles());
        assertEquals("Checking command parameters of returned job", COMMAND_PARAMS, localJob1.getCommandParameters());
        assertEquals("Checking working directory of returned job", WORKING_DIR, localJob1.getWorkingDirectory());
        assertEquals("Checking results-include regex of returned job", RESULTS_INCLUDE_REGEX, localJob1.getResultsIncludeRegex());
        assertEquals("Checking results-exclude regex of returned job", RESULTS_EXCLUDE_REGEX, localJob1.getResultsExcludeRegex());
        assertNotNull("Checking submitTime of returned job", localJob1.getSubmitTime());
    }
    
    private void verifyJob2(final LocalJob localJob2) {
        assertEquals("Checking ID of returned job", this.jobId2, localJob2.getId());
        assertEquals("Checking status of returned job", LocalJobStatus.NEW, localJob2.getStatus());        
        assertEquals("Checking execution type of returned job", EXECUTION_TYPE_2, localJob2.getExecutionType());
        assertEquals("Checking model/control file of returned job", MODEL_FILE_2, localJob2.getExecutionFile());
        assertEquals("Checking extra input files of returned job", EXTRA_INPUT_FILES_2, localJob2.getExtraInputFiles());
        assertEquals("Checking command parameters of returned job", COMMAND_PARAMS, localJob2.getCommandParameters());
        assertEquals("Checking working directory of returned job", WORKING_DIR, localJob2.getWorkingDirectory());
        assertEquals("Checking results-include regex of returned job", RESULTS_INCLUDE_REGEX, localJob2.getResultsIncludeRegex());
        assertEquals("Checking results-exclude regex of returned job", RESULTS_EXCLUDE_REGEX, localJob2.getResultsExcludeRegex());
        assertNotNull("Checking submitTime of returned job", localJob2.getSubmitTime());
    }
    private String generateEndpoint(String path) {
        return URL+":"+server.getEmbeddedServletContainer().getPort()+path;
    }
}
