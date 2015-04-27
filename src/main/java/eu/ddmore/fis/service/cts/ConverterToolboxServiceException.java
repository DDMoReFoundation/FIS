/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.cts;


/**
 * Exception thrown by {@link ConverterToolboxService } 
 */
public class ConverterToolboxServiceException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * @param message the error message
     */
    public ConverterToolboxServiceException(String message) {
        super(message);
    }

    /**
     * @param message the error message
     * @param cause the cause
     */
    public ConverterToolboxServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
