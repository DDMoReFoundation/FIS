/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers;


/**
 * Exceptions representing client errors used by REST layer to produce informative error messages.
 */
public class ClientError extends Exception {
    private static final long serialVersionUID = 1L;
    /**
     * @param message - the message that should be displayed to the client.
     */
    public ClientError(String message) {
        super(message);
    }

    /**
     * If client requests a job that does not exist.
     */
    public static class JobNotFound extends ClientError {
        private static final long serialVersionUID = 1L;
        
        public JobNotFound(String message) {
            super(message);
        }
    }

    /**
     * Thrown when client tries to perform illegal operation on a job.
     */
    public static class JobStateConflict extends ClientError {
        private static final long serialVersionUID = 1L;
        
        public JobStateConflict(String message) {
            super(message);
        }
    }
}
