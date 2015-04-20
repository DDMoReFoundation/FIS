package eu.ddmore.fis.service.processors;

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.service.GroovyScriptExecutor;
import eu.ddmore.fis.service.JobResourceProcessor;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

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
        GroovyShell shell = createShell(binding);
        execute(shell);
        return (LocalJob) binding.getVariable("job");
    }
}