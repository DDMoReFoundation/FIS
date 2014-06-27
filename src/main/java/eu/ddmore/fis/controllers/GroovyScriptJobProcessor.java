package eu.ddmore.fis.controllers;

import eu.ddmore.fis.domain.LocalJob;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.io.File;
import org.springframework.beans.factory.annotation.Required;

/**
 * Uses a groovy script to process job
 */
public class GroovyScriptJobProcessor implements JobResourceProcessor {

    private File scriptFile;

    private Binding binding;

    public GroovyScriptJobProcessor() {
        binding = new Binding();
    }

    /**
     * Creates an instance which will pass the binding object to the groovy script execution context.
     * @param binding
     */
    public GroovyScriptJobProcessor(Binding binding) {
        this.binding = binding;
    }

    private GroovyShell createShell(Binding binding) {
        return new GroovyShell(this.getClass().getClassLoader(), binding);
    }

    private Binding getBinding() {
        return new Binding(binding.getVariables());
    }

    @Override
    public LocalJob process(LocalJob job) {
        Binding binding = getBinding();
        binding.setVariable("job", job);
        binding.setVariable("scriptFile", scriptFile);
        GroovyShell shell = createShell(binding);
        execute(shell, scriptFile);
        return (LocalJob) binding.getVariable("job");
    }

    /**
     * Executes a script in the given shell
     * @param shell
     * @param scriptFile
     * @return result of the script evaluation
     */
    private Object execute(GroovyShell shell, File scriptFile) {
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
