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

@Controller
public class ReadWriteController {

	@Autowired
	private ReadWriteProcessor readWriteProcessor;
	
//	@RequestMapping(method=RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
//    public @ResponseBody Set<LocalJob> getJobs() {
//		Set<LocalJob> jobs = Sets.newHashSet();
//		for(LocalJob job : localJobService.getAll() ) {
//			jobs.add(job);
//		}
//		return jobs;
//    }


	@RequestMapping(value = "readmdl", method=RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody String readMdl(@RequestParam(value="fileName") String fileName) {
        // Invoke the readResource Groovy script
        return readWriteProcessor.process(fileName);
    }

    @RequestMapping(value = "writemdl", method=RequestMethod.POST, produces={MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody String writeMdl(@RequestParam(value="outputStream") String outputStream) {
    	return "Write successful";
    }

    public ReadWriteProcessor getReadWriteProcessor() {
		return readWriteProcessor;
	}

	public void setReadWriteProcessor(ReadWriteProcessor readWriteProcessor) {
		this.readWriteProcessor = readWriteProcessor;
	}
}