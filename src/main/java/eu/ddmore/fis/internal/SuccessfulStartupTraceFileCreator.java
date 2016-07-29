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
package eu.ddmore.fis.internal;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.actuate.system.ApplicationPidListener;
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
