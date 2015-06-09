/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.domain;

public class SubmissionRequest {

	private String workingDirectory;
	private String command; // TODO: To be renamed to executionType to be consistent throughout the workflow; needs changing in TEL.R too though
	private String executionFile;
	private String commandParameters;
	private String extraInputFilesRegex;
	
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
	
    public void setCommandParameters(String commandParameters) {
        this.commandParameters = commandParameters;
    }
    
    public String getCommandParameters() {
        return commandParameters;
    }
    
    public String getExtraInputFilesRegex() {
        return extraInputFilesRegex;
    }

    public void setExtraInputFilesRegex(String extraInputFilesRegex) {
        this.extraInputFilesRegex = extraInputFilesRegex;
    }
    
    @Override
    public String toString() {
        return "SubmissionRequest [workingDirectory=" + workingDirectory
                + ", command=" + command + ", executionFile=" + executionFile
                + ", commandParameters=" + commandParameters
                + ", extraInputFilesRegex=" + extraInputFilesRegex + "]";
    }
}
