/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service;

import eu.ddmore.fis.domain.LocalJob;

/**
 * Responsible for processing job resources.
 * 
 */
public interface JobResourceProcessor {
    /**
     * Processes job's resources 
     * @param job which resources should be processed
     * @return a modified job (should always be different than the parameter)
     */
    LocalJob process(LocalJob job);

}