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
	private FileProcessor readProcessor;

	@Autowired
	private MdlFileWriter writeProcessor;

	@RequestMapping(value = "readmdl", method=RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody String readMdl(@RequestParam(value="filePath") String filePath) {
        return readProcessor.process(filePath);
    }

    @RequestMapping(value = "writemdl", method=RequestMethod.POST, produces={MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody WriteMdlResponse writeMdl(@RequestParam(value="writeRequest") WriteMdlRequest writeRequest) {
    	return writeProcessor.process(writeRequest);
    }

	public FileProcessor getReadProcessor() {
		return readProcessor;
	}

	public void setReadProcessor(FileProcessor readProcessor) {
		this.readProcessor = readProcessor;
	}

    public MdlFileWriter getWriteProcessor() {
		return writeProcessor;
	}

	public void setWriteProcessor(MdlFileWriter writeProcessor) {
		this.writeProcessor = writeProcessor;
	}
}