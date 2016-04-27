/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.domain;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests ObjectMapper behaviour for the {@link LocalJob}
 */
public class LocalJobObjectMapperTest {
    private static final Logger LOG = Logger.getLogger(LocalJobObjectMapperTest.class);

    private static final String MOCK_ID = "MOCK_ID";
    private static final String MOCK_PASSWORD = "MOCK_PASSWORD";
    private static final String MOCK_PASSPHRASE = "MOCK_PASSPHRASE";
    
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    public void shouldSerializeLocalJob() throws JsonProcessingException {
        final String expected = "{\"id\":\"MOCK_ID\",\"executionType\":null,\"commandParameters\":null,\"workingDirectory\":null,\"executionFile\":null,\"extraInputFiles\":null,\"submitTime\":null,\"status\":null,\"resultsIncludeRegex\":null,\"resultsExcludeRegex\":null,\"version\":0}";
        LocalJob job = new LocalJob();
        job.setId(MOCK_ID);
        
        UserInfo userInfo = new UserInfo();
        userInfo.setExecuteAsUser(true);
        userInfo.setPassword(MOCK_PASSWORD);
        userInfo.setIdentityFilePassphrase(MOCK_PASSPHRASE);
        job.setUserInfo(userInfo);
        
        String serialized = objectMapper.writeValueAsString(job);
        
        LOG.info(String.format("Serialized job %s ", serialized));
        assertEquals("Resulting JSON should be as expected", expected, serialized);
        
    }

    @Test
    public void shouldDeSerializeLocalJob() throws IOException {
        final String serialized = "{\"id\":\"MOCK_ID\",\"executionType\":null,\"commandParameters\":null,\"workingDirectory\":null,\"executionFile\":null,\"extraInputFiles\":null,\"userInfo\":{\"id\":null,\"userName\":null,\"executeAsUser\":true, \"password\":\"MOCK_PASSWORD\", \"identityFilePassphrase\":\"MOCK_PASSPHRASE\"},\"submitTime\":null,\"status\":null,\"resultsIncludeRegex\":null,\"resultsExcludeRegex\":null,\"version\":0}";
        
        LocalJob job = objectMapper.readValue(serialized, LocalJob.class);
        
        LOG.info(String.format("Serialized job %s ", serialized));
        assertEquals("Password should be populated.", MOCK_PASSWORD, job.getUserInfo().getPassword());
        assertEquals("Password should be populated.", MOCK_PASSPHRASE, job.getUserInfo().getIdentityFilePassphrase());
        
    }
}
