/*******************************************************************************
 * Copyright (C) 2016 Mango Business Solutions Ltd, http://www.mango-solutions.com
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
 *******************************************************************************/
import static org.junit.Assert.assertEquals;

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
import eu.ddmore.fis.domain.WriteMdlResponse
import eu.ddmore.fis.service.cts.ConverterToolboxService
import groovy.json.JsonOutput

import groovy.transform.Field

/**
 * This script invokes converter toolbox service to convert JSON to MDL.
 * It returns {@link WriteMdlResponse} with outcome of the processing.
 * Conversion Report is dumped to json in the output directory.
 */

@Field final Logger LOG = Logger.getLogger("eu.ddmore.fis.scripts.WriteMdlResource") // getClass() doesn't return what you might expect, for a Groovy script

/**
 * Parameters
 */
final String outputFilePath = binding.getVariable("filePath");
final String fileContent = binding.getVariable("fileContent")

/**
 * Variables
 */
final File scriptFile = binding.getVariable("scriptFile");
final ArchiveFactory archiveFactory = binding.getVariable("archiveFactory");
final ConverterToolboxService converterToolboxService = binding.getVariable("converterToolboxService");
final LanguageVersion from = binding.getVariable("jsonLanguage");
final LanguageVersion to = binding.getVariable("mdlLanguage");
final String outputArchiveName = binding.getVariable("fis.cts.output.archive");
final String outputConversionReport = binding.getVariable("fis.cts.output.conversionReport");
final String FIS_METADATA_DIR = binding.getVariable("fis.metadata.dir");

/**
 * Script
 */

File outputFile = new File(outputFilePath)
File workingDir = outputFile.parentFile
// Ensure that the FIS metadata directory is created
File fisMetadataDir = new File(workingDir,FIS_METADATA_DIR);
fisMetadataDir.mkdir();

LOG.debug("Working directory: ${workingDir}");

File jsonFileName = new File(FilenameUtils.removeExtension(outputFilePath) + ".json")
FileUtils.writeStringToFile(jsonFileName, fileContent)

File outputDirectory = fisMetadataDir;

File archiveFile = new File(fisMetadataDir, outputArchiveName);

if(archiveFile.exists()) {
    LOG.warn("Archive file ${archiveFile} already exists, removed.");
    FileUtils.deleteQuietly(archiveFile)
}

Archive archive = archiveFactory.createArchive(archiveFile);

try {
    archive.open();
    Entry en = archive.addFile(jsonFileName,"/");
    archive.addMainEntry(en);
} finally {
    archive.close();
}


ConversionReport conversionReport = null;

try {
    if(!converterToolboxService.isConversionSupported(from,to) ) {
        throw new IllegalStateException("Requested conversion from ${from} to ${to} is not supported by Converter Toolbox Service.")
    }
    
    conversionReport = converterToolboxService.convert(archive,from,to);
    if(ConversionReportOutcomeCode.FAILURE.equals(conversionReport.getReturnCode())) {
        LOG.debug("Conversion of ${jsonFileName} failed.");
        return new WriteMdlResponse("Failed: conversion of ${jsonFileName} failed.")
    }
    archive.open();
    Preconditions.checkState(!archive.getMainEntries().isEmpty(), "Archive with the result of conversion had no main entries.");
    Entry resultEntry = archive.getMainEntries().iterator().next();
    File resultFile = outputFile;
    resultFile = resultEntry.extractFile(resultFile);
    Preconditions.checkNotNull(resultFile, "Extracted Archive entry ${resultEntry} was null.");
    LOG.debug("Output file is ${resultFile}.");
    
    FileUtils.deleteQuietly(jsonFileName)
    
    WriteMdlResponse response = new WriteMdlResponse("Successful")
    return response
} catch(Exception ex) {
    WriteMdlResponse response = new WriteMdlResponse("Failed: ${ex.getMessage()}");
    return response
} finally {
    String conversionReportText = "No conversion report was generated for ${jsonFileName}";
    if(conversionReport!=null) {
        conversionReportText = JsonOutput.toJson(conversionReport);
    }
    File conversionReportFile = new File(fisMetadataDir, outputConversionReport);
    LOG.debug("Writing conversion report to ${conversionReportFile}.");
    FileUtils.deleteQuietly(conversionReportFile)
    conversionReportFile << conversionReportText;
    archive.close();
}
