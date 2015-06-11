/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
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
