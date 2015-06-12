package eu.ddmore.fis.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.ddmore.fis.CommonIntegrationTestContextConfiguration;
import eu.ddmore.fis.SystemPropertiesAware;
import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.LocalJobStatus;
import eu.ddmore.fis.service.LocalJobService;

/**
 * Integration test for {@link JobsController}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CommonIntegrationTestContextConfiguration.class })
public class JobsControllerIT extends SystemPropertiesAware {

    private final Logger LOG = Logger.getLogger(JobsControllerIT.class);
    
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
        this.savedLocalJob1.setControlFile(MODEL_FILE_1);
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
        this.savedLocalJob2.setControlFile(MODEL_FILE_2);
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

    /**
     * Test method for {@link eu.ddmore.fis.controllers.JobsController#getJob(java.lang.String)}.
     */
    @Test
    public void testGetJob() {
        final LocalJob retrievedJob = this.jobsController.getJob(this.jobId1);
        LOG.info(String.format("Retrieved job: %s (modelfile=%s)", retrievedJob.getId(), retrievedJob.getControlFile()));
        assertNotNull("Matching Job should be found and retrieved", retrievedJob);
        verifyJob1(retrievedJob);
    }

    /**
     * Test method for {@link eu.ddmore.fis.controllers.JobsController#getJobs()}.
     */
    @Test
    public void testGetJobs() {
        final Collection<LocalJob> retrievedJobs = this.jobsController.getJobs();
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
    
    /**
     * Test method for {@link eu.ddmore.fis.controllers.JobsController#getJobStatus(java.lang.String)}.
     */
    @Test
    public void testGetJobStatus() {
        final LocalJobStatus retrievedJobStatus = this.jobsController.getJobStatus(this.jobId1);
        assertEquals("Checking the returned job status", LocalJobStatus.RUNNING, retrievedJobStatus);
    }
    
    private void verifyJob1(final LocalJob localJob1) {
        assertEquals("Checking ID of returned job", this.jobId1, localJob1.getId());
        assertEquals("Checking status of returned job", LocalJobStatus.RUNNING, localJob1.getStatus());        
        assertEquals("Checking execution type of returned job", EXECUTION_TYPE_1, localJob1.getExecutionType());
        assertEquals("Checking model/control file of returned job", MODEL_FILE_1, localJob1.getControlFile());
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
        assertEquals("Checking model/control file of returned job", MODEL_FILE_2, localJob2.getControlFile());
        assertEquals("Checking extra input files of returned job", EXTRA_INPUT_FILES_2, localJob2.getExtraInputFiles());
        assertEquals("Checking command parameters of returned job", COMMAND_PARAMS, localJob2.getCommandParameters());
        assertEquals("Checking working directory of returned job", WORKING_DIR, localJob2.getWorkingDirectory());
        assertEquals("Checking results-include regex of returned job", RESULTS_INCLUDE_REGEX, localJob2.getResultsIncludeRegex());
        assertEquals("Checking results-exclude regex of returned job", RESULTS_EXCLUDE_REGEX, localJob2.getResultsExcludeRegex());
        assertNotNull("Checking submitTime of returned job", localJob2.getSubmitTime());
    }

}
