/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.internal;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.ddmore.fis.service.ServiceWorkingDirectory;

/**
 * Component responsible for cleaning up old FIS working directories data
 * 
 * TODO This implementation won't work once FIS starts to manage directories holding job files.
 */
@Component
public class WorkingDirectoriesReaper {

    private final static Logger LOG = Logger.getLogger(WorkingDirectoriesReaper.class);

    @Autowired(required = true)
    private ServiceWorkingDirectory serviceWorkingDirectory;

    @Value("${fis.workingDirectory.availabilityTimeout}")
    private long taskWorkingDirectoryAvailabilityTimeout;

    @Scheduled(fixedRateString = "${fis.workingDirectory.cleanupRate}")
    public void performCleanup() {
        LOG.debug("Performing old working directories cleanup...");
        File workingDirectory = serviceWorkingDirectory.getWorkingDirectory();
        File[] forDeletion = workingDirectory.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                boolean forDeletion = pathname.isDirectory()
                    && (new Date().getTime() - pathname.lastModified() > taskWorkingDirectoryAvailabilityTimeout);
                LOG.debug(String.format("Directory %s last modified at %s is %s scheduled for deletion.", pathname,
                    pathname.lastModified(), (forDeletion ? "" : "not")));
                return forDeletion;
            }
        });

        int deleted = 0;
        int failedToDelete = 0;
        for (File dir : forDeletion) {
            try {
                FileUtils.deleteDirectory(dir);
                deleted++;
            } catch (IOException e) {
                LOG.error(String.format("Could not delete %s.", dir), e);
                failedToDelete++;
            }
        }

        LOG.debug(String.format("Deleted %s working directories. Failed deletions = %s.", deleted, failedToDelete));
    }

}
