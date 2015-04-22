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