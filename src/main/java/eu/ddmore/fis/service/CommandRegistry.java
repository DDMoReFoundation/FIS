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
