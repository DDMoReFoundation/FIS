/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.InvokerResult;
import com.mango.mif.core.exec.ce.CeInvoker;

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.LocalJobStatus;

/**
 * Retrieves NM-TRAN results
 * 
 * The files are copied from {workingDirectory}\{job.id} directory
 */
public class NmTranJobResourceRetriever implements JobResourceProcessor {
    private static final Logger LOG = Logger.getLogger(NmTranJobResourceRetriever.class);
    private String retrieveOutputsScript;
    
    @Override
    public LocalJob process(LocalJob job) {
        final File jobDir = new File(job.getWorkingDirectory(), job.getId());
        try {
            FileUtils.copyDirectory(jobDir, new File(job.getWorkingDirectory()));
        } catch (IOException e) {
            throw new RuntimeException("Error when copying job files", e);
        }

        CeInvoker invoker = new CeInvoker();
        InvokerResult invokerResult;
        try {
            invokerResult = invoker.execute(String.format("%s \"%s\" \"%s\" \"%s\"", retrieveOutputsScript, job.getWorkingDirectory(), jobDir, job.getStatus()));
            
            LOG.debug(String.format("InvokerResult: [ exit-code: %s, std-out: %s, std-err: %s ]",invokerResult.getExitCode(),invokerResult.getStdout(),invokerResult.getStderr()));
            
            if(invokerResult.getExitStatus()!=0) {
                LOG.error(String.format("Couldn't copy files. Cause %s, %s", invokerResult.getOutputStream(), invokerResult.getOutputStream()));
                job.setStatus(LocalJobStatus.FAILED);
            }
        } catch (ExecutionException e) {
            throw new RuntimeException("Error when copying job files",e);
        }
        
        return job;
    }

    
    @Required
    public void setRetrieveOutputsScript(String retrieveOutputsScript) {
        this.retrieveOutputsScript = retrieveOutputsScript;
    }
    
    public String getRetrieveOutputsScript() {
        return retrieveOutputsScript;
    }
}
