/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.domain;

import java.util.List;

public class SubmissionRequest {

	private String workingDirectory;
	private String command; // TODO: To be renamed to executionType to be consistent throughout the workflow; needs changing in TEL.R too though
	private String executionFile;
	private String commandParameters;
	private List<String> extraInputFiles;
	
	public String getWorkingDirectory() {
		return workingDirectory;
	}
	
	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}
	
	/**
	 * @return the type of execution ("target" in TEL) to be invoked e.g. NONMEM, MONOLIX
	 */
	public String getCommand() {
		return command;
	}
	
	/**
	 * @param command - the type of execution ("target" in TEL) to be invoked e.g. NONMEM, MONOLIX
	 */
	public void setCommand(String command) {
		this.command = command;
	}

    /**
     * @return the model file to be executed, intended to be an absolute path
     */    
    public String getExecutionFile() {
        return executionFile;
    }
    
    /**
     * @param executionFile - the model file to be executed, intended to be an absolute path
     */
    public void setExecutionFile(String executionFile) {
        this.executionFile = executionFile;
    }
	
    public void setCommandParameters(String commandParameters) {
        this.commandParameters = commandParameters;
    }
    
    public String getCommandParameters() {
        return commandParameters;
    }
    
    /**
     * @return {@link List} of extra files to be used in the execution, intended to be
     *         absolute paths; these will be relativised against the model file
     */
    public List<String> getExtraInputFiles() {
        return extraInputFiles;
    }

    /**
     * @param extraInputFiles - {@link List} of extra files to be used in the execution, intended to
     *                          be absolute paths; these will be relativised against the model file
     */
    public void setExtraInputFiles(List<String> extraInputFiles) {
        this.extraInputFiles = extraInputFiles;
    }
    
    @Override
    public String toString() {
        return "SubmissionRequest [workingDirectory=" + workingDirectory
                + ", command=" + command + ", executionFile=" + executionFile
                + ", commandParameters=" + commandParameters
                + ", extraInputFiles=" + extraInputFiles + "]";
    }
}
