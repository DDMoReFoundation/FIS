/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Preconditions;

/**
 * Controller exposing HTTP endpoint for performing MDL to PharmML conversion.
 */
@Controller
public class MdlConversionController {
    private static final Logger LOG = Logger.getLogger(MdlConversionController.class);
    @Autowired
    private MdlConversionProcessor conversionProcessor;

    /**
     * Performs conversion of a resource from MDL to PharmML
     * 
     * @param filePath - full path to MDL file
     * @param outputDir - a directory where output of the conversion should be placed
     * @return a full path to the result file or empty string in case of error
     */
    @RequestMapping(value = "convertmdl", method=RequestMethod.POST, produces={MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody String convertMdlToPharmML(@RequestParam(value="fileName") String filePath, @RequestParam(value="outputDir") String outputDir) {
        Preconditions.checkArgument(StringUtils.isNotBlank(filePath), "fileName %s can't be blank.");
        Preconditions.checkArgument(StringUtils.isNotBlank(outputDir), "outputDir %s can't be blank.");
        Preconditions.checkArgument(new File(filePath).exists(), "File %s must exist.", filePath);
        try {
            return conversionProcessor.process(filePath, outputDir);
        } catch (Exception ex) {
            String errorMsg = String.format("Error when performing conversion from MDL to PharmML of %s.", filePath);
            LOG.error(errorMsg, ex);
            Throwable cause = ex;
            if(ex.getCause()!=null) {
                // Groovy wraps all exceptions, we need to unwrap the caught exception to get the actual error message 
                // specified in the executed script.
                cause = ex.getCause();
            }
            throw new RuntimeException(String.format(errorMsg + " Cause [%s].", cause.getMessage()));
        }
    }

    public MdlConversionProcessor getConversionProcessor() {
        return conversionProcessor;
    }

    public void setConversionProcessor(MdlConversionProcessor conversionProcessor) {
        this.conversionProcessor = conversionProcessor;
    }
    
}
