package eu.ddmore.fis.controllers;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
/**
 * Uses a Groovy script to process a job
 */
public class ReadProcessor extends GroovyScriptProcessor {
    
    public ReadProcessor() {
    	super();
    }

    public ReadProcessor(Binding binding) {
        super(binding);
    }
    
    public String process(String fileName) {
        Binding binding = getBinding();
        binding.setVariable("scriptFile", getScriptFile());
        binding.setVariable("fileName", fileName);
        GroovyShell shell = createShell(binding);
        return (String)execute(shell);
    }
}