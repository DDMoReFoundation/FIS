/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.internal;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * A Spring Boot application listener that creates a special trace file only if FIS starts up successfully. The file name can be configured
 * by system property: "startup.trace.file" and defaults to fis.trace.
 * 
 * Notice!!!
 * This is not to be mistaken with PID file, for which creation there is different listener responsible {@link ApplicationPidListener}
 * and is bound to a different Spring application life-cycle phase.
 * 
 */
public class SuccessfulStartupTraceFileCreator implements SpringApplicationRunListener {
    private static final Logger LOG = Logger.getLogger(SuccessfulStartupTraceFileCreator.class);
    private static final String DEFAULT_FILE_NAME = "fis.trace";
    private final File traceFile;
    
    public SuccessfulStartupTraceFileCreator(SpringApplication application, String[] args) {
        traceFile = new File(getTraceFileName());
    }
    
    private String getTraceFileName() {
        String sysPropertyValue = System.getProperty("startup.trace.file");
        if(StringUtils.isBlank(sysPropertyValue)) {
            return DEFAULT_FILE_NAME;
        }
        return sysPropertyValue;
    }
    
    @Override
    public void started() {
        FileUtils.deleteQuietly(traceFile);
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
    }

    @Override
    public void finished(ConfigurableApplicationContext context, Throwable exception) {
        if(exception==null) {
            LOG.debug(String.format("Creating application startup trace file %s", traceFile.getAbsolutePath()));
            try {
                FileUtils.write(traceFile, "SUCCESS");
            } catch (IOException e) {
                throw new RuntimeException("Could not create application startup trace file.",e);
            }
            traceFile.deleteOnExit();
        }
    }
}
