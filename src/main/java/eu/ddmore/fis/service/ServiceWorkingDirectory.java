/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

/**
 * Represents service' working directory
 */
@Component
public class ServiceWorkingDirectory {
    private static final Logger LOG = Logger.getLogger(ServiceWorkingDirectory.class);
    
    @Value("${fis.workingDirectory.path}")
    private File workingDirectory;

    @Value("${fis.workingDirectory.identityFile:.fis_wd}")
    private String identityFileName;
    
    /**
     * Initialises working directory, if it exists it cleans up its contents
     */
    @PostConstruct
    public void initialise() {
        Preconditions.checkNotNull(workingDirectory, "Working directory can't be null, use 'fis.workingDirectory.path' property to set it.");
        Preconditions.checkState(StringUtils.isNotBlank(workingDirectory.getPath()),"Working directory can't be blank, use 'fis.workingDirectory.path' property to set it");
        Preconditions.checkState(StringUtils.isNotBlank(identityFileName),"Identity file can't be blank");
        File identityFile = new File(workingDirectory,identityFileName);
        if(workingDirectory.exists()) {
            LOG.info(String.format("Cleaning up working directory %s", workingDirectory));
            cleanUpWorkingDirectory(workingDirectory, identityFile);
        } else {
            LOG.info(String.format("Working directory %s does not exist, attempting to create it.",workingDirectory));
            workingDirectory.mkdirs();
            if(!workingDirectory.exists()) {
                throw new RuntimeException(String.format("Could not create working directory %s", workingDirectory));
            }
            createIdentityFile(identityFile);
        }
    }

    private void createIdentityFile(File identityFile) {
        try {
            FileUtils.writeStringToFile(identityFile, "FIS identify file");
        } catch (IOException e) {
            throw new RuntimeException(String.format("Can't write to %s", workingDirectory));
        }
    }

    private void cleanUpWorkingDirectory(File workingDirectory, File identityFile) {
        if(!workingDirectory.isDirectory()) {
            throw new IllegalStateException(String.format("The specified working directory path %s is not a directory!",workingDirectory.getAbsolutePath()));
        }
        File[] files = workingDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !(".".equals(name)||"..".equals(name));
            }
            
        });
        if(files==null) {
            throw new IllegalStateException(String.format("Could not list files in %s, please check permissions on directory.", workingDirectory));
        }
        if(!identityFile.exists() && files.length>1) {
            throw new IllegalStateException(String.format("Invalid working directory structure, the %s file was not found in %s. "+
                                            "Make sure that the directory you specified is empty or doesn't exist.",identityFile,workingDirectory));
        } else {
            createIdentityFile(identityFile);
        }
        for(File f : files) {
            if(f.getName().equals(identityFileName)) {
                continue;
            }
            FileUtils.deleteQuietly(f);
        }
    }
    
    public File getWorkingDirectory() {
		return workingDirectory;
	}

    /**
     * Creates a directory in service working directory
     * @param name the name for the new directory
     * @return a new directory
     */
    public File newDirectory(String name) {
        Preconditions.checkNotNull(name, "Directory name can't be null");
        File result = new File(workingDirectory,name);
        if(result.exists()) {
        	throw new IllegalStateException(String.format("Working directory with name %s already exists.",name));
        }
        result.mkdir();
        return result;
    }

    
    /**
     * Creates a directory in service working directory with random name
     * @return a new directory
     */
    public File newDirectory() {
        return newDirectory(UUID.randomUUID().toString());
    }
    
    public void setIdentityFileName(String identityFileName) {
        this.identityFileName = identityFileName;
    }
    
    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
}
