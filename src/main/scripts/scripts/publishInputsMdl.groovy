/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
import static groovy.io.FileType.*
import static groovy.io.FileVisitResult.*

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
 * This script is responsible for publishing inputs in MDL format to MIF
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
final ConverterToolboxService converterToolboxService = binding.getVariable("converterToolboxService");
final LanguageVersion from = binding.getVariable("mdlLanguage");
final LanguageVersion to = binding.getVariable("pharmmlLanguage");
final JobArchiveProvisioner jobArchiveProvisioner = binding.getVariable("jobArchiveProvisioner");
final String outputArchiveName = binding.getVariable("fis.cts.output.archive");
final String outputConversionReport = binding.getVariable("fis.cts.output.conversionReport");
final String MDL_FILE_EXT = binding.getVariable("fis.mdl.ext");
final String PHARMML_FILE_EXT = binding.getVariable("fis.pharmml.ext");
final String executionHostFileshareLocal = binding.getVariable("execution.host.fileshare.local");
final String FIS_METADATA_DIR = binding.getVariable("fis.metadata.dir");
final File mockDataDir = new File(scriptFile.getParentFile().getParentFile(),"mockData")
final File fisJobWorkingDir = new File(job.getWorkingDirectory())
final File mifJobWorkingDir = new File(executionHostFileshareLocal, job.getId());

/**
 * Script
 */
LOG.debug("Job ${job.getId()}, fis working dir: ${fisJobWorkingDir}, mif working dir: ${mifJobWorkingDir}");

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

if(!converterToolboxService.isConversionSupported(from,to) ) {
    throw new IllegalStateException("Requested conversion from ${from} to ${to} is not supported by Converter Toolbox Service.")
}

ConversionReport conversionReport = null;

try {
    LOG.debug("Performing Conversion");
    if (hasMockPharmml(mockDataDir, modelName, PHARMML_FILE_EXT)) {
        LOG.debug("Found mock conversion result, using it.");
        useMockPharmml(mockDataDir, modelName, PHARMML_FILE_EXT, archive);
    } else {
        LOG.debug("Running conversion on CTS.");
        conversionReport = converterToolboxService.convert(archive,from,to);
        if(ConversionReportOutcomeCode.FAILURE.equals(conversionReport.getReturnCode())) {
            LOG.error("Conversion failed on CTS for ${job.getId()}");
            job.setStatus(LocalJobStatus.FAILED)
            return;
        }
    }
} catch(Exception ex) {
    LOG.error("Failed to perform conversion for a job ${job.getId()}", ex);
    job.setStatus(LocalJobStatus.FAILED)
    return;
} finally {
    String conversionReportText = "No conversion report was generated for ${origControlFile}";
    if(conversionReport!=null) {
        conversionReportText = JsonOutput.toJson(conversionReport);
    }
    new File(fisMetadataDir, outputConversionReport) << conversionReportText;
}

try {
    jobArchiveProvisioner.provision(job, archive, mifJobWorkingDir)
} catch(Exception ex) {
    LOG.error("Failed to provision files to MIF for job ${job.getId()}", ex);
    job.setStatus(LocalJobStatus.FAILED)
    return
}



/**
 * Checks if there exist mock PharmML for given model name
 * @param mockDataDir - location of directory containing mock data
 * @param modelName name of the model
 * @param pharmmlFileExt - extension of PharmML file
 * @return true if there exists mock PharmML file for the given model name
 */
boolean hasMockPharmml(File mockDataDir, String modelName, String pharmmlFileExt) {
    File xmlVersionInMockDataDir = new File(mockDataDir, modelName + "." + pharmmlFileExt)
    return xmlVersionInMockDataDir.exists();
}

/**
 * Copies mock PharmML model file to Archive
 * @param mockDataDir - location of directory containing mock data
 * @param modelName name of the model
 * @param pharmmlFileExt - extension of PharmML file
 * @param archive - archive
 * @return
 */
void useMockPharmml(File mockDataDir, String modelName, String pharmmlFileExt, Archive archive) {
    try {
        archive.open();
        File xmlVersionInMockDataDir = new File(mockDataDir, modelName + "." + pharmmlFileExt)
        String newModelFileName = modelName + "." + pharmmlFileExt
        Entry modelEntry = archive.addFile( xmlVersionInMockDataDir, newModelFileName)
        File data = new File(mockDataDir, modelName + ".csv")
        if (data.exists()) {
            archive.addFile( data, modelName + "_data.csv" )
        } else {
            LOG.debug("Mock data file: ${data} doesn't exist.");
        }
        archive.setMainEntries([]);
        archive.addMainEntry(modelEntry);
    } finally {
        archive.close();
    }
}
