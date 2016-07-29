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
