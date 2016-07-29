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
package eu.ddmore.fis.service.processors;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.LocalJobStatus;
import eu.ddmore.fis.service.processors.JobProcessor;


@RunWith(MockitoJUnitRunner.class)
public class JobProcessorTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldExecuteScript() {
        JobProcessor jobProcessor = new JobProcessor();
        jobProcessor.setScriptFile(FileUtils.toFile(JobProcessorTest.class.getResource("/eu/ddmore/fis/controllers/updateJobStatus.groovy")));
        LocalJob job = mock(LocalJob.class);
        
        jobProcessor.process(job);
        
        verify(job).setStatus(eq(LocalJobStatus.COMPLETED));
    }
}