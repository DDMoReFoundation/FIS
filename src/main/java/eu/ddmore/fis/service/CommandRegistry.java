package eu.ddmore.fis.service;

import com.mango.mif.domain.ClientAvailableConnectorDetails;

/**
 * Holds information on capabilities of the system in respect to handled commands and environments that are supported by the framework
 */
public interface CommandRegistry {

    /**
     * Resolves a command execution target server-side and returns a
     * {@link ClientAvailableConnectorDetails} object containing the details
     * relevant to the client-side.
     * <p>
     * @param command (e.g. 'scm', 'execute', etc)
     * @return a {@link ClientAvailableConnectorDetails} object if one exists
     * @throws NoSuchElementException if no matches are found
     * @throws IllegalStateException if more than one match is found
     */
     // TODO: Rename this method
    ClientAvailableConnectorDetails resolveExecutionTargetFor(String command);
}
