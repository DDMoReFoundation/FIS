/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.processors.internal;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.base.Preconditions;

import eu.ddmore.archive.Archive;
import eu.ddmore.archive.Entry;
import eu.ddmore.archive.exception.ArchiveException;
import eu.ddmore.fis.domain.LocalJob;


/**
 * Provisions Archive to MIF job working directory
 */
public class JobArchiveProvisioner {
    private static final Logger LOG = Logger.getLogger(JobArchiveProvisioner.class);
    
    @Value("${fis.mif.archive.support:true}")
    private boolean mifArchiveSupport;
    /**
     * Provisions files to MIF
     * 
     * @param job FIS local job entity
     * @param archive the archive
     * @param mifJobWorkingDir MIF job working directory
     * @param mifArchiveSupport flag indicating if MIF supports Archive
     * @throws ArchiveException - if there was problem reading an archive
     * @throws IOException - if there was problem with copying files
     */
    public void provision(LocalJob job, Archive archive, File mifJobWorkingDir) throws ArchiveException, IOException {
        Preconditions.checkNotNull(job, "Job can't be null.");
        Preconditions.checkNotNull(archive, "Archive can't be null.");
        Preconditions.checkNotNull(mifJobWorkingDir, "MIF Job working directory can't be null.");
        LOG.debug(String.format("Provisioning FIS job inputs to %s.", mifJobWorkingDir));
        try {
            archive.open();
            Preconditions.checkState(!archive.getMainEntries().isEmpty(), "Archive with the result of conversion had no main entries.");
            if(mifArchiveSupport) {
                LOG.debug("MIF supports Archive. Copying archive...");
                mifJobWorkingDir.mkdirs();
                job.setControlFile(archive.getArchiveFile().getName());
                FileUtils.copyFileToDirectory(archive.getArchiveFile(),mifJobWorkingDir);
            } else {
                LOG.debug("MIF does not support Archive. Extracting archive contents...");
                archive.extractArchiveTo(mifJobWorkingDir);
                Entry resultEntry = archive.getMainEntries().iterator().next();
                job.setControlFile(/* we have to remove the leading '/' so the path is relative */toExternalPath(resultEntry.getFilePath()));
            }
        } finally {
            archive.close();
        }
    }
    
    private String toExternalPath(String filePath) {
        return filePath.substring(1);
    }

    public void setMifArchiveSupport(boolean mifArchiveSupport) {
        this.mifArchiveSupport = mifArchiveSupport;
    }
}
