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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;


/**
 * Tests {@link SuccessfulStartupTraceFileCreator}
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(FileUtils.class)
public class SuccessfulStartupTraceFileCreatorTest {
    @Mock 
    private SpringApplication application;
    @Mock
    private ConfigurableApplicationContext context;
    
    private static String oldFisTraceName;
    
    private static final String STARTUP_TRACE_FILE_PROP="startup.trace.file";
    
    @BeforeClass
    public static void initClass() {
        oldFisTraceName=System.getProperty(STARTUP_TRACE_FILE_PROP);
        System.setProperty(STARTUP_TRACE_FILE_PROP,"");
    }
    
    @AfterClass
    public static void tearDownClass() {
        if(oldFisTraceName!=null) {
            System.setProperty(STARTUP_TRACE_FILE_PROP,oldFisTraceName);
        }
    }
    
    @Test
    public void finished_shouldCreateTraceFileIfExceptionIsNull() throws IOException {
        PowerMockito.mockStatic(FileUtils.class);
        
        SuccessfulStartupTraceFileCreator instance = new SuccessfulStartupTraceFileCreator(application, null);
        instance.finished(context, null);
        
        PowerMockito.verifyStatic();
        FileUtils.write(any(File.class), any(String.class));
    }
    
    @Test(expected = RuntimeException.class)
    public void finished_shouldThrowRuntimeExceptionIfCantCreateTraceFile() throws IOException {
        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.doThrow(mock(IOException.class)).when(FileUtils.class);
        FileUtils.write(any(File.class), any(String.class));

        SuccessfulStartupTraceFileCreator instance = new SuccessfulStartupTraceFileCreator(application, null);
        instance.finished(context, null);
    }
    
    @Test
    public void finished_shouldNotCreateTraceFileIfExceptionIsNotNull() throws IOException {
        PowerMockito.mockStatic(FileUtils.class);
        
        SuccessfulStartupTraceFileCreator instance = new SuccessfulStartupTraceFileCreator(application, null);
        instance.finished(context, mock(Exception.class));
        
        PowerMockito.verifyStatic(times(0));
        FileUtils.write(any(File.class), any(String.class));
    }

}
