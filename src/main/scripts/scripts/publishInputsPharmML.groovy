/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
import static groovy.io.FileType.*
import static groovy.io.FileVisitResult.*

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.log4j.Logger

import eu.ddmore.archive.Archive
import eu.ddmore.archive.ArchiveFactory
import eu.ddmore.archive.Entry
import eu.ddmore.fis.domain.LocalJob
import eu.ddmore.fis.domain.LocalJobStatus
import eu.ddmore.fis.service.processors.internal.JobArchiveProvisioner
import eu.ddmore.fis.controllers.utils.ArchiveUtilsLocal
import groovy.transform.Field

/**
 * This script is responsible for publishing inputs to MIF without performing conversion.
 */

/**
 * Parameters
 */
final LocalJob job = binding.getVariable("job");

/**
 * Variables
 */
@Field final Logger LOG = Logger.getLogger(getClass())
final File scriptFile = binding.getVariable("scriptFile");
final ArchiveFactory archiveFactory = binding.getVariable("archiveFactory");
final JobArchiveProvisioner jobArchiveProvisioner = binding.getVariable("jobArchiveProvisioner");
final String outputArchiveName = binding.getVariable("fis.cts.output.archive");
final String executionHostFileshareLocal = binding.getVariable("execution.host.fileshare.local");
final String FIS_METADATA_DIR = binding.getVariable("fis.metadata.dir");
final File fisJobWorkingDir = new File(job.getWorkingDirectory())
final File mifJobWorkingDir = new File(executionHostFileshareLocal, job.getId());

/**
 * Script
 */

LOG.debug("Job ${job.getId()}, FIS working dir: ${fisJobWorkingDir}, MIF working dir: ${mifJobWorkingDir}");

// Ensure that the FIS metadata directory is created
File fisMetadataDir = new File(fisJobWorkingDir,FIS_METADATA_DIR);
fisMetadataDir.mkdir();

File origControlFile = new File(FilenameUtils.normalize(job.getControlFile()));

// TODO: Once TEL is changed to pass in model files as absolute paths, this 'if' statement becomes redundant
if (!origControlFile.isAbsolute()) {
    // Resolve the control file against the working directory
    origControlFile = fisJobWorkingDir.toPath().resolve(origControlFile.toPath()).toFile();
}

File archiveFile = new File(fisMetadataDir, outputArchiveName);

final Archive archive = ArchiveUtilsLocal.buildPharmMLArchive(archiveFactory, archiveFile, origControlFile, fisJobWorkingDir)

try {
    jobArchiveProvisioner.provision(job, archive, mifJobWorkingDir)
} catch(Exception ex) {
    LOG.error("Failed to provision files to MIF for job ${job.getId()}", ex);
    job.setStatus(LocalJobStatus.FAILED)
    return
}

