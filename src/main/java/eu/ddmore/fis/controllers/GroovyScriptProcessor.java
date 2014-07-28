package eu.ddmore.fis.controllers;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;

import org.springframework.beans.factory.annotation.Required;

/**
 * Configures and executes a Groovy script
 */
public abstract class GroovyScriptProcessor {
    private File scriptFile;
    private Binding binding;

    public GroovyScriptProcessor() {
        binding = new Binding();
    }

    /**
     * Creates an instance which will pass the binding object to the Groovy
     * script execution context.
     * 
     * @param binding
     */
    public GroovyScriptProcessor(Binding binding) {
        this.binding = binding;
    }

    protected GroovyShell createShell(Binding binding) {
        return new GroovyShell(this.getClass().getClassLoader(), binding);
    }

    protected Binding getBinding() {
        return new Binding(binding.getVariables());
    }

    /**
     * Executes a script in the given shell
     * @param shell
     * @return result of the script evaluation
     */
    protected Object execute(GroovyShell shell) {
        try {
            return shell.evaluate(scriptFile);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Couldn't execute script %s.", scriptFile), e);
        }
    }

    @Required
    public void setScriptFile(File scriptFile) {
        this.scriptFile = scriptFile;
    }

    public File getScriptFile() {
        return scriptFile;
    }
}