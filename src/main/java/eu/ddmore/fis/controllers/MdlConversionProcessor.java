package eu.ddmore.fis.controllers;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
/**
 * MDL conversion processor which will configure and execute groovy script for MDL conversion.
 * 
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