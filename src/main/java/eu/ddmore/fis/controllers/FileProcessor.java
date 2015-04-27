package eu.ddmore.fis.controllers;

import eu.ddmore.fis.service.GroovyScriptExecutor;
import groovy.lang.Binding;
/**
 * Uses a file processor that applies some processing implemented in Groovy script 
 * and returns the result of processing.
 */
public class FileProcessor extends GroovyScriptExecutor {
    
    public FileProcessor() {
    	super();
    }

    public FileProcessor(Binding binding) {
        super(binding);
    }
    
    /**
     * 
     * @param fileName a file that should be processed
     * @return a result of the processing (a contents of the file after processing)
     */
    public String process(String fileName) {
        Binding binding = getBinding();
        binding.setVariable("scriptFile", getScriptFile());
        binding.setVariable("filePath", fileName);
        return (String)execute(binding);
    }
}