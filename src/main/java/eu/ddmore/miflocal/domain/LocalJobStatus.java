/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.miflocal.domain;

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
