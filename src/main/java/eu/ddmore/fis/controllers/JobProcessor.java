package eu.ddmore.fis.controllers;

import eu.ddmore.fis.domain.LocalJob;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

/**
 * Uses a Groovy script to process a job
 */
public class JobProcessor extends GroovyScriptProcessor implements JobResourceProcessor {

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