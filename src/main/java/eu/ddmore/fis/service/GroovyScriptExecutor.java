/*******************************************************************************
 * Copyright (C) 2016 Mango Business Solutions Ltd, http://www.mango-solutions.com
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
 *******************************************************************************/
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