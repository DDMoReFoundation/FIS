/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
import static groovy.io.FileType.*
import static groovy.io.FileVisitResult.*

import java.io.File;

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.log4j.Logger

import com.google.common.base.Preconditions

import eu.ddmore.archive.Archive
import eu.ddmore.archive.ArchiveFactory
import eu.ddmore.archive.Entry
import eu.ddmore.convertertoolbox.domain.ConversionReport
import eu.ddmore.convertertoolbox.domain.ConversionReportOutcomeCode
import eu.ddmore.convertertoolbox.domain.LanguageVersion
import eu.ddmore.fis.domain.LocalJob
import eu.ddmore.fis.domain.LocalJobStatus
import eu.ddmore.fis.service.cts.ConverterToolboxService
import eu.ddmore.fis.service.processors.internal.JobArchiveProvisioner
import groovy.json.JsonOutput
import groovy.transform.Field
/**
 * This script is responsible for publishing inputs to MIF without performing conversion
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

File origControlFile = new File(job.getControlFile());

String modelName = FilenameUtils.getBaseName(origControlFile.getName());
String modelExt = FilenameUtils.getExtension(origControlFile.getName());

File archiveFile = new File(fisMetadataDir, outputArchiveName);
Archive archive = archiveFactory.createArchive(archiveFile);
try {
    archive.open();
    
    fisJobWorkingDir.traverse type:FILES, excludeFilter:~/.*\.fis.*/, visit:{ String relativePath = it.getParentFile().getPath().replace(fisJobWorkingDir.getPath(),""); LOG.debug("Processing Job input file: ${it.getName()}, which will be added into Archive at location: ${relativePath}"); archive.addFile(it, relativePath) }
    if(LOG.isDebugEnabled()) {
        LOG.debug("Control File Path ${origControlFile.getPath()}")
        archive.getEntries().each {LOG.debug("Archive Entry ${it.getFilePath()}") }
    }
    
    Entry controlFileEntry = archive.getEntry(origControlFile.getPath())
    archive.addMainEntry(controlFileEntry);
} finally {
    archive.close();
}

try {
    jobArchiveProvisioner.provision(job, archive, mifJobWorkingDir)
} catch(Exception ex) {
    LOG.error("Failed to provision files to MIF for job ${job.getId()}", ex);
    job.setStatus(LocalJobStatus.FAILED)
    return
}

