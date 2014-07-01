/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.ddmore.fis.domain.WriteMdlRequest;
import eu.ddmore.fis.domain.WriteMdlResponse;

@Controller
public class ReadWriteController {

	@Autowired
	private ReadProcessor readProcessor;

	@Autowired
	private WriteProcessor writeProcessor;

	@RequestMapping(value = "readmdl", method=RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody String readMdl(@RequestParam(value="fileName") String fileName) {

		// Invoke the readResource Groovy script
        return readProcessor.process(fileName);
    }

    @RequestMapping(value = "writemdl", method=RequestMethod.POST, produces={MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody WriteMdlResponse writeMdl(@RequestParam(value="writeRequest") WriteMdlRequest writeRequest) {

    	// Invoke the writeResource Groovy script
    	return writeProcessor.process(writeRequest);
    }

	public ReadProcessor getReadProcessor() {
		return readProcessor;
	}

	public void setReadProcessor(ReadProcessor readProcessor) {
		this.readProcessor = readProcessor;
	}

    public WriteProcessor getWriteProcessor() {
		return writeProcessor;
	}

	public void setWriteProcessor(WriteProcessor writeProcessor) {
		this.writeProcessor = writeProcessor;
	}
}