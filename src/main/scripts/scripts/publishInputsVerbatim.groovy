/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
import static groovy.io.FileType.*
import static groovy.io.FileVisitResult.*

import org.apache.commons.io.FilenameUtils
import org.apache.log4j.Logger

import eu.ddmore.archive.Archive
import eu.ddmore.fis.configuration.Fileshare
import eu.ddmore.fis.controllers.utils.ArchiveCreator
import eu.ddmore.fis.domain.LocalJob
import eu.ddmore.fis.domain.LocalJobStatus
import eu.ddmore.fis.service.processors.internal.JobArchiveProvisioner
import groovy.transform.Field

/**
 * This script is responsible for publishing inputs to MIF without performing conversion.
 */

@Field final Logger LOG = Logger.getLogger("eu.ddmore.fis.scripts.PublishInputsVerbatim") // getClass() doesn't return what you might expect, for a Groovy script

/**
 * Parameters
 */
final LocalJob job = binding.getVariable("job");

/**
 * Variables
 */
final File scriptFile = binding.getVariable("scriptFile");
final JobArchiveProvisioner jobArchiveProvisioner = binding.getVariable("jobArchiveProvisioner");
final String outputArchiveName = binding.getVariable("fis.cts.output.archive");
final Fileshare fileshare = binding.getVariable("fileshare");
final String FIS_METADATA_DIR = binding.getVariable("fis.metadata.dir");
final ArchiveCreator archiveCreator = binding.getVariable("archiveCreator");

/**
 * Script
 */

final File fisJobWorkingDir = new File(job.getWorkingDirectory())
final File mifJobWorkingDir = new File(fileshare.getFisHostPath(), job.getId());

LOG.debug("Job ${job.getId()}, FIS working dir: ${fisJobWorkingDir}, MIF working dir: ${mifJobWorkingDir}");

// Ensure that the FIS metadata directory is created
File fisMetadataDir = new File(fisJobWorkingDir,FIS_METADATA_DIR);
fisMetadataDir.mkdir();

File origControlFile = new File(FilenameUtils.normalize(job.getExecutionFile()));

// TODO: Once TEL is changed to pass in model files as absolute paths, this 'if' statement becomes redundant
if (!origControlFile.isAbsolute()) {
    // Resolve the model file against the working directory
    origControlFile = fisJobWorkingDir.toPath().resolve(origControlFile.toPath()).toFile();
}

File archiveFile = new File(fisMetadataDir, outputArchiveName);

// Create and populate the Archive with the model file and any associated data file(s) and any extra input files

final Archive archive = archiveCreator.buildArchive(archiveFile, origControlFile,
    job.getExtraInputFiles() == null ? null : job.getExtraInputFiles().collect { new File(it) }) // Have to convert file paths into File objects for the extraInputFiles

try {
    jobArchiveProvisioner.provision(job, archive, mifJobWorkingDir)
} catch(Exception ex) {
    LOG.error("Failed to provision files to MIF for job ${job.getId()}", ex);
    job.setStatus(LocalJobStatus.FAILED)
    return
}

