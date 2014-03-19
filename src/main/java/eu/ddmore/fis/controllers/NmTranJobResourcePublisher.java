/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.InvokerResult;
import com.mango.mif.core.exec.ce.CeInvoker;

import eu.ddmore.fis.domain.LocalJob;

/**
 * Publishes NM-TRAN inputs
 * 
 * The files are copied to {workingDirectory}\{job.id} directory
 */
public class NmTranJobResourcePublisher implements JobResourceProcessor {
    private static final Logger LOG = Logger.getLogger(NmTranJobResourcePublisher.class);
    
    private String publishInputsScript;
    
    @Override
    public LocalJob process(LocalJob job) {
        final File jobDir = new File(job.getWorkingDirectory(), job.getId());
        jobDir.mkdir();
        try {
            FileUtils.copyDirectory(new File(job.getWorkingDirectory()), jobDir, new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return !jobDir.equals(pathname);
                }
            });
            
            executeScript(job.getWorkingDirectory(), jobDir);
            
        } catch (IOException e) {
            throw new RuntimeException("Error when copying job files", e);
        }
        return job;
    }

    private void executeScript(String workingDirectory, File jobDir) {
        CeInvoker invoker = new CeInvoker();
        InvokerResult invokerResult;
        try {
            invokerResult = invoker.execute(String.format("%s \"%s\" \"%s\"", publishInputsScript, workingDirectory, jobDir));
            
            LOG.debug(String.format("InvokerResult: [ exit-code: %s, std-out: %s, std-err: %s ]",invokerResult.getExitCode(),invokerResult.getStdout(),invokerResult.getStderr()));
            
            if(invokerResult.getExitStatus()!=0) {
                throw new RuntimeException(String.format("Error when copying job files %s, %s", invokerResult.getErrorStream(), invokerResult.getOutputStream()));
            }
        } catch (ExecutionException e) {
            throw new RuntimeException("Error when copying job files",e);
        }
    }

    @Required
    public void setPublishInputsScript(String publishInputsScript) {
        this.publishInputsScript = publishInputsScript;
    }
    
    public String getPublishInputsScript() {
        return publishInputsScript;
    }
}
