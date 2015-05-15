/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
import org.apache.commons.io.FileUtils
import org.apache.log4j.Logger

import com.google.common.base.Preconditions
import org.apache.commons.io.FilenameUtils;
import eu.ddmore.archive.Archive
import eu.ddmore.archive.ArchiveFactory
import eu.ddmore.archive.Entry
import eu.ddmore.convertertoolbox.domain.ConversionReport
import eu.ddmore.convertertoolbox.domain.ConversionReportOutcomeCode
import eu.ddmore.convertertoolbox.domain.LanguageVersion
import eu.ddmore.fis.service.cts.ConverterToolboxService
import groovy.json.JsonOutput
import groovy.transform.Field
import java.nio.file.Path

import eu.ddmore.fis.controllers.utils.MdlUtils;
/**
 * This script invokes converter toolbox service to convert MDL to PharmML.
 * It will return result file path on success or empty string.
 * Any Exception is wrapped by Groovy interpreter in Runtime Exception and should be handled by the clients.
 * Conversion Report is dumped to json in the output directory 
 */

/**
 * Parameters
 */
final String inputFilePath = binding.getVariable("filePath");
final String outputDir = binding.getVariable("outputDir");

/**
 * Variables
 */
final Logger LOG = Logger.getLogger(getClass())
final File scriptFile = binding.getVariable("scriptFile");
final ArchiveFactory archiveFactory = binding.getVariable("archiveFactory");
final ConverterToolboxService converterToolboxService = binding.getVariable("converterToolboxService");
final LanguageVersion from = binding.getVariable("mdlLanguage");
final LanguageVersion to = binding.getVariable("pharmmlLanguage");
final String outputArchiveName = binding.getVariable("fis.cts.output.archive");
final String outputConversionReport = binding.getVariable("fis.cts.output.conversionReport");
final String FIS_METADATA_DIR = binding.getVariable("fis.metadata.dir");
final MdlUtils mdlUtils = binding.getVariable("mdlUtils");

/**
 * Script
 */
File inputFile = new File(FilenameUtils.normalize(inputFilePath))
File workingDir = inputFile.parentFile
// Ensure that the FIS metadata directory is created
File fisMetadataDir = new File(workingDir,FIS_METADATA_DIR);
fisMetadataDir.mkdir();

File outputDirectory = new File(outputDir);

File archiveFile = new File(fisMetadataDir, outputArchiveName);

if(archiveFile.exists()) {
    LOG.warn("Archive file ${archiveFile} already exists, removed.");
    FileUtils.deleteQuietly(archiveFile)
}

Archive archive = archiveFactory.createArchive(archiveFile);
try {
    archive.open();
    Collection<File> dataFiles = mdlUtils.getDataFileFromMDL(inputFile)
    dataFiles = dataFiles.collect { new File(FilenameUtils.normalize(it.getAbsolutePath())) }
    Path commonBasePath = getCommonBasePath(dataFiles, inputFile.getParentFile())
    LOG.debug("Input file references ${dataFiles}")
    LOG.debug("Common base path for all inputs is ${commonBasePath}")
    Entry en = archive.addFile(inputFile, "/" + commonBasePath.relativize(inputFile.getParentFile().toPath()))
    archive.addMainEntry(en);
    dataFiles.each { 
        String location = "/" + commonBasePath.relativize(it.getParentFile().toPath())
        LOG.debug("Adding ${it} at ${location}");
        archive.addFile(it,location) 
        }
} finally {
    archive.close();
}

if(!converterToolboxService.isConversionSupported(from,to) ) {
    throw new IllegalStateException("Requested conversion from ${from} to ${to} is not supported by Converter Toolbox Service.")
}

ConversionReport conversionReport = null;

try {
    conversionReport = converterToolboxService.convert(archive,from,to);
    archive.open();
    if(ConversionReportOutcomeCode.FAILURE.equals(conversionReport.getReturnCode())) {
        LOG.debug("Conversion of ${inputFile} failed.");
        return ""
    }
    Preconditions.checkState(!archive.getMainEntries().isEmpty(), "Archive with the result of conversion had no main entries.");
    Entry resultEntry = archive.getMainEntries().iterator().next();
    File resultFile = new File(outputDirectory,resultEntry.getFileName());
    resultFile = resultEntry.extractFile(resultFile);
    Preconditions.checkNotNull(resultFile, "Extracted Archive entry ${resultEntry} was null.");
    return resultFile.getAbsolutePath();
} finally {
    String conversionReportText = "No conversion report was generated for ${inputFile}";
    if(conversionReport!=null) {
        conversionReportText = JsonOutput.toJson(conversionReport);
    }
    File conversionReportFile = new File(fisMetadataDir, outputConversionReport);
    LOG.debug("Writing conversion report to ${conversionReportFile}.");
    FileUtils.deleteQuietly(conversionReportFile)
    conversionReportFile << conversionReportText;
    archive.close();
}

/**
 * Finds a common base path for a list of files starting from the given candidate path.
 * @param files
 * @param candidate
 * @return the common base path
 */
Path getCommonBasePath(Collection<File> files, File candidate) {
    int size = files.size();
    if(files.findAll { it.toPath().startsWith(candidate.toPath()) }.size()==size) {
        return candidate.toPath();
    } else {
        return getCommonBasePath(files,candidate.getParentFile())
    }
}

