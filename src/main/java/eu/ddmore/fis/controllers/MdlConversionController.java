/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MdlConversionController {

	@Autowired
	private MdlConversionProcessor conversionProcessor;

	@RequestMapping(value = "convertmdl", method=RequestMethod.POST, produces={MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody String convertMdlToPharmML(@RequestParam(value="fileName") String fileName, @RequestParam(value="outputDir") String outputDir) {

		// Invoke the mdlConverter Groovy script
        return conversionProcessor.process(fileName, outputDir);
    }

	public MdlConversionProcessor getConversionProcessor() {
		return conversionProcessor;
	}

	public void setConversionProcessor(MdlConversionProcessor conversionProcessor) {
		this.conversionProcessor = conversionProcessor;
	}
}