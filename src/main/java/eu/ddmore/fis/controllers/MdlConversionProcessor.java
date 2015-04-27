package eu.ddmore.fis.controllers;

import eu.ddmore.fis.service.GroovyScriptExecutor;
import groovy.lang.Binding;
/**
 * MDL conversion processor which will configure and execute groovy script for MDL conversion.
 * 
 */
public class MdlConversionProcessor extends GroovyScriptExecutor {

    public MdlConversionProcessor() {
        super();
    }

    public MdlConversionProcessor(Binding binding) {
        super(binding);
    }

    public String process(String fileName, String outputDir) {
        Binding binding = getBinding();
        binding.setVariable("scriptFile", getScriptFile());
        binding.setVariable("filePath", fileName);
        binding.setVariable("outputDir", outputDir);
        return (String)execute(binding);
    }
}