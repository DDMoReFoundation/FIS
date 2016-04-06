/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis;

import java.util.Collection;

import org.apache.commons.lang.builder.ToStringBuilder;

import eu.ddmore.fis.domain.LocalJobStatus;
import eu.ddmore.fis.domain.UserInfo;


/**
 * 'External' Job representation that doesn't include JPA nor JSON annotations.
 */
public class ExternalJob {

    private String id;
    
    private long version;

    private String executionType;

    private String commandParameters;

    private String workingDirectory;

    private String executionFile;

    private Collection<String> extraInputFiles;

    private UserInfo userInfo;

    private String submitTime;

    private LocalJobStatus status;

    private String resultsIncludeRegex;

    private String resultsExcludeRegex;

    public UserInfo getUserInfo() {
        return userInfo;
    }
    
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
    
    
    public String getExecutionType() {
        return executionType;
    }

    public void setExecutionType(final String executionType) {
        this.executionType = executionType;
    }

    public void setCommandParameters(String commandParameters) {
        this.commandParameters = commandParameters;
    }

    public String getCommandParameters() {
        return commandParameters;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getExecutionFile() {
        return executionFile;
    }

    public void setExecutionFile(String executionFile) {
        this.executionFile = executionFile;
    }

    public Collection<String> getExtraInputFiles() {
        return extraInputFiles;
    }

    public void setExtraInputFiles(Collection<String> extraInputFiles) {
        this.extraInputFiles = extraInputFiles;
    }

    public String getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(String submitTime) {
        this.submitTime = submitTime;
    }

    public LocalJobStatus getStatus() {
        return status;
    }

    public void setStatus(LocalJobStatus status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setResultsExcludeRegex(String resultsExcludeRegex) {
        this.resultsExcludeRegex = resultsExcludeRegex;
    }

    public void setResultsIncludeRegex(String resultsIncludeRegex) {
        this.resultsIncludeRegex = resultsIncludeRegex;
    }

    /**
     * @return the resultsExcludeRegex the regular expression matching the files that should NOT be copied to FIS client's workspace.
     */
    public String getResultsExcludeRegex() {
        return resultsExcludeRegex;
    }

    /**
     * @return the resultsIncludeRegex the regular expression matching the files that should be copied to FIS client's workspace.
     */
    public String getResultsIncludeRegex() {
        return resultsIncludeRegex;
    }
    
    public long getVersion() {
        return version;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
