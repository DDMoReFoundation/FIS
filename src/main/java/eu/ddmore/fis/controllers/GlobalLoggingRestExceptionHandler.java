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

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;


/**
 * A controller advice that captures any exception thrown by any of the controllers, ensures
 * that it is being logged and then re-throws it so the exception can be handled by more specific handlers.
 */
@ControllerAdvice
public class GlobalLoggingRestExceptionHandler {
        private static final Logger LOG = Logger.getLogger(GlobalLoggingRestExceptionHandler.class);
        @ExceptionHandler(value = Exception.class)
        public ModelAndView defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
            // we don't do anything but logging and re-throwing.
            LOG.error(String.format("Processing of the request %s resulted in error.", req),e);
            throw e;
        }
}
