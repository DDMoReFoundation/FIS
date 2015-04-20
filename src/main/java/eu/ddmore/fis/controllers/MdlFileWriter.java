package eu.ddmore.fis.controllers;

import eu.ddmore.fis.domain.WriteMdlRequest;
import eu.ddmore.fis.domain.WriteMdlResponse;
import eu.ddmore.fis.service.GroovyScriptExecutor;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
/**
 * Uses a Groovy script to write an MDL resource 
 */
public class MdlFileWriter extends GroovyScriptExecutor {
    
    public MdlFileWriter() {
    	super();
    }

    public MdlFileWriter(Binding binding) {
        super(binding);
    }
    
    /**
     * Executes a Groovy script that is responsible for processing content included in the write request and dumping the result to a 
     * location specified by write request.
     * 
     * @param writeRequest a request to write MDL file with a given content
     * @return a response holding status of the operation
     */
    public WriteMdlResponse process(WriteMdlRequest writeRequest) {
        Binding binding = getBinding();
        binding.setVariable("scriptFile", getScriptFile());
        binding.setVariable("fileContent", writeRequest.getFileContent());
        binding.setVariable("fileName", writeRequest.getFileName());
        GroovyShell shell = createShell(binding);
        return (WriteMdlResponse)execute(shell);
    }
}