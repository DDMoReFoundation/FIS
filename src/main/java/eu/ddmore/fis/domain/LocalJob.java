/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.miflocal.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class LocalJob implements Serializable {

	@Id
    @Column(nullable = false)
	private String id;

	@Column(nullable = false)
	private String command;

	@Column(nullable = false)
	private String workingDirectory;
	
	@Column(nullable = false)
	private String controlFile;

	@Column(nullable = false)	
	private String submitTime;

    @Column(nullable = false)   
    private LocalJobStatus status;

    @Version
    @Column
    private long version;
    
    public String getCommand() {
        return command;
    }

    
    public void setCommand(String command) {
        this.command = command;
    }

    
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    
    public String getControlFile() {
        return controlFile;
    }

    
    public void setControlFile(String controlFile) {
        this.controlFile = controlFile;
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
}
