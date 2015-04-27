/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
import org.apache.log4j.Logger

import com.google.common.base.Preconditions

import eu.ddmore.archive.Archive
import eu.ddmore.archive.ArchiveFactory
import eu.ddmore.archive.Entry
import eu.ddmore.convertertoolbox.domain.ConversionReport
import eu.ddmore.convertertoolbox.domain.ConversionReportOutcomeCode
import eu.ddmore.convertertoolbox.domain.LanguageVersion
import eu.ddmore.fis.service.cts.ConverterToolboxService
import groovy.json.JsonOutput

/**
 * This script will prepare and invoke converter toolbox command to convert MDL to pharmML.
 * It will return result file path on success or empty string on failure along with errors logged to error log. 
 */

/**
 * Parameters
 */
final String mdlFilePath = binding.getVariable("filePath");
final String outputDir = binding.getVariable("outputDir");

/**
 * Variables
 */
final Logger LOG = Logger.getLogger(getClass())
final File scriptFile = binding.getVariable("scriptFile");
final ArchiveFactory archiveFactory = binding.getVariable("archiveFactory");
final ConverterToolboxService converterToolboxService = binding.getVariable("converterToolboxService");
final LanguageVersion mdlLanguage = binding.getVariable("mdlLanguage");
final LanguageVersion pharmmlLanguage = binding.getVariable("pharmmlLanguage");
final String outputArchiveName = binding.getVariable("fis.cts.output.archive");
final String outputConversionReport = binding.getVariable("fis.cts.output.conversionReport");


/**
 * Script
 */
File mdlFile = new File(mdlFilePath)
File workingDir = mdlFile.parentFile
// Ensure that the FIS metadata directory is created
File fisMetadataDir = new File(workingDir,".fis");
fisMetadataDir.mkdir();

File outputDirectory = new File(outputDir);

File archiveFile = new File(outputDirectory, outputArchiveName);
Archive archive = archiveFactory.createArchive(archiveFile);

archive.open();
Entry en = archive.addFileToArchive(mdlFile,"/");
archive.getMainEntries().add(en);
archive.close();

if( converterToolboxService.isConversionSupported(mdlLanguage,pharmmlLanguage) ) {
    throw new IllegalStateException("Requested conversion from ${mdlLanguage} to ${pharmmlLanguage} is not supported by Converter Toolbox Service.")
}

ConversionReport conversionReport = null;

try {
    conversionReport = converterToolboxService.convert(archive,mdlLanguage,pharmmlLanguage);
    if(ConversionReportOutcomeCode.FAILURE.equals(conversionReport.getReturnCode())) {
        return ""
    }
    archive.open();
    Preconditions.checkState(archive.getMainEntries().size()>0, "Archive with the result of conversion had no main entries.");
    Entry resultEntry = archive.getMainEntries().get(0);
    File resultFile = new File(outputDirectory,resultEntry.getFileName());
    resultFile = resultEntry.extractFile(resultFile);
    Preconditions.checkNotNull(resultFile, "Extracted Archive entry ${resultEntry} was null.");
    return resultFile.getAbsolutePath();
} finally {
    archive.close();
    String conversionReportText = "No conversion report was generated for ${mdlFile}";
    if(conversionReport!=null) {
        conversionReportText = JsonOutput.toJson(conversionReport);
    }
    new File(outputDirectory, outputConversionReport) << conversionReportText;
}
