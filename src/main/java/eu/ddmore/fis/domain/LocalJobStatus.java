/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.domain;

/**
 * Status of the job
 */
public enum LocalJobStatus {
    NEW,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED;
}
