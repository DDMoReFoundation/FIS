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
package eu.ddmore.fis.service;

import com.mango.mif.domain.ClientAvailableConnectorDetails;

/**
 * Holds information on capabilities of the system in respect to handled commands and
 * environments that are supported by the framework.
 */
public interface CommandRegistry {

    /**
     * Takes an executionType uniquely identifying a MIFServer Connector
     * and resolves this server-side. Returns the client-exposed
     * {@link ClientAvailableConnectorDetails} attached to this Connector.
     * Client-Available Connector Details are the subset of the
     * Command Execution Target Details, that are relevant to the
     * client side.
     * <p>
     * @param executionType - uniquely identifying a server-side Connector
     * @return a {@link ClientAvailableConnectorDetails} object if a
     * 		   Connector matching the executionType was found
     * @throws NoSuchElementException if no matches are found
     * @throws IllegalStateException if more than one match is found
     */
    ClientAvailableConnectorDetails resolveClientAvailableConnectorDetailsFor(String executionType);
}
