/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.domain;

public class SubmissionRequest {

	public String workingDirectory;
	public String command;
	public String executionFile;
	
	public String getWorkingDirectory() {
		return workingDirectory;
	}
	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
    
    public String getExecutionFile() {
        return executionFile;
    }
    
    public void setExecutionFile(String executionFile) {
        this.executionFile = executionFile;
    }
	
	
}
