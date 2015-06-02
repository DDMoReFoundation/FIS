/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
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
import eu.ddmore.fis.controllers.utils.ArchiveUtilsLocal
import eu.ddmore.fis.controllers.utils.MdlUtils
import eu.ddmore.fis.service.cts.ConverterToolboxService
import groovy.json.JsonOutput

/**
 * This script invokes converter toolbox service to convert MDL to PharmML.
 * It will return result file path on success or empty string.
 * Any Exception is wrapped by Groovy interpreter in Runtime Exception and should be handled by the clients.
 * Conversion Report is dumped to json in the output directory.
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

// Create and populate the Archive with the MDL file and any associated data file(s)

final Archive archive = ArchiveUtilsLocal.buildMDLArchive(mdlUtils, archiveFactory, archiveFile, inputFile)

// Perform the conversion from MDL to PharmML

if (!converterToolboxService.isConversionSupported(from, to)) {
    throw new IllegalStateException("Requested conversion from ${from} to ${to} is not supported by Converter Toolbox Service.")
}

ConversionReport conversionReport = null;

try {
    conversionReport = converterToolboxService.convert(archive,from,to);
    archive.open();
    if (ConversionReportOutcomeCode.FAILURE.equals(conversionReport.getReturnCode())) {
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

