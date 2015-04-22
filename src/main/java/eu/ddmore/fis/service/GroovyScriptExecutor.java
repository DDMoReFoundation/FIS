package eu.ddmore.fis.service;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;

import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Preconditions;

/**
 * Configures and executes a Groovy script
 */
public abstract class GroovyScriptExecutor {
    private File scriptFile;
    private Binding binding;

    public GroovyScriptExecutor() {
        binding = new Binding();
    }

    /**
     * Creates an instance which will pass the binding object to the Groovy
     * script execution context.
     * 
     * @param binding
     */
    public GroovyScriptExecutor(Binding binding) {
        Preconditions.checkNotNull(binding, "Binding can't be null");
        this.binding = binding;
    }

    private GroovyShell createShell(Binding binding) {
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
    protected Object execute(Binding binding) {
        Preconditions.checkNotNull(binding, "Binding can't be null");
        try {
            return createShell(binding).evaluate(scriptFile);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Couldn't execute script %s.", scriptFile), e);
        }
    }

    @Required
    public void setScriptFile(File scriptFile) {
        Preconditions.checkNotNull(scriptFile, "Script file must not be null");
        Preconditions.checkState(scriptFile.exists(), String.format("File '%s' must exist", scriptFile));
        this.scriptFile = scriptFile;
    }

    public File getScriptFile() {
        return scriptFile;
    }
}