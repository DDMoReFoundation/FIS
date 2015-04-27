/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller exposing HTTP endpoint for performing MDL to PharmML conversion
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
        try {
            return conversionProcessor.process(filePath, outputDir);
        } catch (Exception ex) {
            LOG.error(String.format("Error when performing conversion from MDL to PharmML of %s, see %s for details.", filePath, outputDir),ex);
        }
        return ""; //FIXME RCurl used by TEL.R doesn't handle the HTTP statuses, we return empty string to indicate failure.
    }

    public MdlConversionProcessor getConversionProcessor() {
        return conversionProcessor;
    }

    public void setConversionProcessor(MdlConversionProcessor conversionProcessor) {
        this.conversionProcessor = conversionProcessor;
    }
}
