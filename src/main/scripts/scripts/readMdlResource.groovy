/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
import eu.ddmore.archive.Archive
import eu.ddmore.archive.ArchiveFactory
import eu.ddmore.archive.Entry
import eu.ddmore.convertertoolbox.domain.ConversionReport
import eu.ddmore.convertertoolbox.domain.ConversionReportOutcomeCode
import eu.ddmore.convertertoolbox.domain.LanguageVersion
import eu.ddmore.fis.service.cts.ConverterToolboxService
import groovy.json.JsonOutput

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.log4j.Logger

import com.google.common.base.Preconditions

/**
 * This script invokes converter toolbox service to convert MDL to JSON.
 * It will return result file path on success or empty string.
 * Any Exception is wrapped by Groovy interpreter in Runtime Exception and should be handled by the clients.
 * Conversion Report is dumped to json in the output directory 
 */

/**
 * Parameters
 */
final String inputFilePath = binding.getVariable("filePath");
//TODO This needs to be unified with mdlConverter.groovy final String outputDir = binding.getVariable("outputDir");

/**
 * Variables
 */
final Logger LOG = Logger.getLogger(getClass())
final File scriptFile = binding.getVariable("scriptFile");
final ArchiveFactory archiveFactory = binding.getVariable("archiveFactory");
final ConverterToolboxService converterToolboxService = binding.getVariable("converterToolboxService");
final LanguageVersion from = binding.getVariable("mdlLanguage");
final LanguageVersion to = binding.getVariable("jsonLanguage");
final String outputArchiveName = binding.getVariable("fis.cts.output.archive");
final String outputConversionReport = binding.getVariable("fis.cts.output.conversionReport");
final String FIS_METADATA_DIR = binding.getVariable("fis.metadata.dir");

/**
 * Script
 */
File inputFile = new File(inputFilePath)
File workingDir = inputFile.parentFile
// Ensure that the FIS metadata directory is created
File fisMetadataDir = new File(workingDir,FIS_METADATA_DIR);
fisMetadataDir.mkdir();

File outputDirectory = fisMetadataDir;// TODO see comment - outputDir);

File archiveFile = new File(fisMetadataDir, outputArchiveName);
Archive archive = archiveFactory.createArchive(archiveFile);

archive.open();
Entry en = archive.addFileToArchive(inputFile,"/");
archive.getMainEntries().add(en);
archive.close();

if( converterToolboxService.isConversionSupported(from,to) ) {
    throw new IllegalStateException("Requested conversion from ${from} to ${to} is not supported by Converter Toolbox Service.")
}

ConversionReport conversionReport = null;

try {
    conversionReport = converterToolboxService.convert(archive,from,to);
    if(ConversionReportOutcomeCode.FAILURE.equals(conversionReport.getReturnCode())) {
        return ""
    }
    archive.open();
    Preconditions.checkState(archive.getMainEntries().size()>0, "Archive with the result of conversion had no main entries.");
    Entry resultEntry = archive.getMainEntries().get(0);
    File resultFile = new File(outputDirectory,resultEntry.getFileName());
    resultFile = resultEntry.extractFile(resultFile);
    Preconditions.checkNotNull(resultFile, "Extracted Archive entry ${resultEntry} was null.");
    
    result = FileUtils.readFileToString(resultFile)
    FileUtils.deleteQuietly(resultFile)
    return result;
} finally {
    archive.close();
    String conversionReportText = "No conversion report was generated for ${inputFile}";
    if(conversionReport!=null) {
        conversionReportText = JsonOutput.toJson(conversionReport);
    }
    new File(fisMetadataDir, outputConversionReport) << conversionReportText;
    FileUtils.deleteQuietly(archive.getArchiveFile());
}
