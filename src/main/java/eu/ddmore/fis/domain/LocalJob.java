/*******************************************************************************
 * Copyright (C) 2014-2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.domain;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

@Entity
public class LocalJob implements Serializable {

	@Id
	@Column(nullable = false)
	private String id;

	@Column(nullable = false)
	@NotNull
	private String executionType;

	@Column(nullable = true)
	private String commandParameters;

	@Column(nullable = false)
	@NotNull
	private String workingDirectory;

	@Column(nullable = false)
	private String executionFile;
	
	@Column(nullable = true)
	@ElementCollection(fetch=FetchType.EAGER)
	private Collection<String> extraInputFiles;

	@Column(nullable = false)
	private String submitTime;

	@Column(nullable = false)
	@NotNull
	private LocalJobStatus status;

    @Column(nullable = true)
    private String resultsIncludeRegex;

    @Column(nullable = true)
    private String resultsExcludeRegex;
	
	@Version
	@Column
	private long version;

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
	
	public void setVersion(long version) {
		this.version = version;
	}

	public long getVersion() {
		return version;
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

	@Override
	public String toString() {
		return String
				.format("LocalJob [id=%s, executionType=%s, commandParameters=%s, workingDirectory=%s, executionFile=%s, extraInputFiles=%s, submitTime=%s, status=%s, resultsIncludeRegex=%s, resultsExcludeRegex=%s, version=%s]",
						id, executionType, commandParameters, workingDirectory,
						executionFile, extraInputFiles, submitTime, status,
						resultsIncludeRegex, resultsExcludeRegex, version);
	}
    
    
}
