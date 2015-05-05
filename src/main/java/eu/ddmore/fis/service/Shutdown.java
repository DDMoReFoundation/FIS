/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service;


/**
 * Components implementing this interface are responsible for invoking shutdown on a resource/component in question
 */
public interface Shutdown {
    /**
     * Executes shutdown
     */
    void invoke();
}
