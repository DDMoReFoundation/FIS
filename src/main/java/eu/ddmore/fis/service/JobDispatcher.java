package eu.ddmore.fis.service;

import eu.ddmore.fis.domain.LocalJob;

/**
 * Component responsible for dispatching job to appropriate execution environment
 */
public interface JobDispatcher {
    /**
     * dispatches a job to a given execution environment (TES)
     * @param localJob a job that should be executed
     * @return a copy of the passed parameter with updated state if needed
     */
    LocalJob dispatch(LocalJob localJob);
}
