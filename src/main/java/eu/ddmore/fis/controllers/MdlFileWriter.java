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
package eu.ddmore.fis.controllers;

import eu.ddmore.fis.domain.WriteMdlRequest;
import eu.ddmore.fis.domain.WriteMdlResponse;
import eu.ddmore.fis.service.GroovyScriptExecutor;
import groovy.lang.Binding;
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
        binding.setVariable("filePath", writeRequest.getFileName());
        return (WriteMdlResponse)execute(binding);
    }
}