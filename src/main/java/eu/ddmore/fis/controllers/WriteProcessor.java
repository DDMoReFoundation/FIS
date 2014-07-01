package eu.ddmore.fis.controllers;

import eu.ddmore.fis.domain.WriteMdlRequest;
import eu.ddmore.fis.domain.WriteMdlResponse;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
/**
 * Uses a Groovy script to process a job
 */
public class WriteProcessor extends GroovyScriptProcessor {
    
    public WriteProcessor() {
    	super();
    }

    public WriteProcessor(Binding binding) {
        super(binding);
    }
    
    public WriteMdlResponse process(WriteMdlRequest writeRequest) {
        Binding binding = getBinding();
        binding.setVariable("scriptFile", getScriptFile());
        binding.setVariable("fileContent", writeRequest.getFileContent());
        binding.setVariable("fileName", writeRequest.getFileName());
        GroovyShell shell = createShell(binding);
        return (WriteMdlResponse)execute(shell);
    }
}