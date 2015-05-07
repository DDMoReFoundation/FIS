/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.cts;

import java.io.File;
import java.util.Collection;

import eu.ddmore.archive.Archive;
import eu.ddmore.convertertoolbox.domain.ConversionCapability;
import eu.ddmore.convertertoolbox.domain.ConversionReport;
import eu.ddmore.convertertoolbox.domain.LanguageVersion;


/**
 * Represents Converter Toolbox Service instance.
 * 
 * Implementations of this interface are expected to throw ConverterToolboxServiceException in any case when processing failure (e.g. communication error, converter runtime failure etc.).
 * Any errors being a result of incorrect inputs should result in appropriate RuntimeException.
 * 
 */
public interface ConverterToolboxService {
    /**
     * Performs conversion of the resource specified by the first main entry in the input {@link Archive} and puts the resulting Archive
     * to outputFile.
     * 
     * Note the input archive should be closed and should not be modified in parallel.
     * 
     * @param archive - input archive
     * @param from - the source language
     * @param to - the target language
     * @param outputFile - file where the resulting archive should be placed
     * @return a conversion report
     * @throws ConverterToolboxServiceException if there is a problem performing the operation
     */
    ConversionReport convert(Archive archive, LanguageVersion from, LanguageVersion to, File outputFile) throws ConverterToolboxServiceException;

    /**
     * Performs conversion of the resource specified by first main entry in the input {@link Archive}. The input Archive
     * is replaced with the result.
     * 
     * Note the input archive should be closed and should not be modified in parallel.
     * 
     * @param archive - input archive
     * @param from - the source language
     * @param to - the target language
     * @return a conversion report
     * @throws ConverterToolboxServiceException if there is a problem performing the operation
     */
    ConversionReport convert(Archive archive, LanguageVersion from, LanguageVersion to) throws ConverterToolboxServiceException;

    /**
     * @return a list of supported conversions
     * @throws ConverterToolboxServiceException if there is a problem performing the operation
     */
    Collection<ConversionCapability> getConversionCapabilities() throws ConverterToolboxServiceException;
    
    /**
     * Check if the given conversion is supported.
     * 
     * @param from the source language
     * @param to the target language
     * @return true if conversion is supported
     * @throws ConverterToolboxServiceException if there is a problem performing the operation
     */
    boolean isConversionSupported(LanguageVersion from, LanguageVersion to) throws ConverterToolboxServiceException;
}
