package eu.ddmore.fis.controllers;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
/**
 * Uses a Groovy script to process a job
 */
public class MdlConversionProcessor extends GroovyScriptProcessor {
    
    public MdlConversionProcessor() {
    	super();
    }

    public MdlConversionProcessor(Binding binding) {
        super(binding);
    }
    
    public String process(String fileName, String outputDir) {
        Binding binding = getBinding();
        binding.setVariable("scriptFile", getScriptFile());
        binding.setVariable("fileName", fileName);
        binding.setVariable("outputDir", outputDir);
        
        GroovyShell shell = createShell(binding);
        return (String)execute(shell);
    }
}