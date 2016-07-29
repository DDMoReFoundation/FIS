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

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.service.GroovyScriptExecutor;
import eu.ddmore.fis.service.JobResourceProcessor;
import groovy.lang.Binding;

/**
 * Uses a Groovy script to process a job
 */
public class JobProcessor extends GroovyScriptExecutor implements JobResourceProcessor {

    public JobProcessor() {
    	super();
    }

    public JobProcessor(Binding binding) {
        super(binding);
    }

    @Override
    public LocalJob process(LocalJob job) {
        Binding binding = getBinding();
        binding.setVariable("job", job);
        binding.setVariable("scriptFile", getScriptFile());
        execute(binding);
        return (LocalJob) binding.getVariable("job");
    }
}