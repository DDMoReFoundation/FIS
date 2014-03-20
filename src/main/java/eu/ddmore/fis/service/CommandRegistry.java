package eu.ddmore.fis.service;

import java.util.NoSuchElementException;

/**
 * Holds information on capabilities of the system in respect to handled commands and environments that are supported by the framework
 */
public interface CommandRegistry {
    /**
     * Resolves a command execution target
     * @param command (e.g. 'scm', 'execute', etc)
     * @return an execution target if one exists
     * @throws NoSuchElementException
     */
    CommandExecutionTarget resolveExecutionTargetFor(String command);
}
