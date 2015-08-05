/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.domain;

/**
 * Status of the job
 */
public enum LocalJobStatus {
    NEW(false),
    RUNNING(false),
    CANCELLING(false),
    COMPLETED(true),
    FAILED(true),
    CANCELLED(true);

    private boolean isFinal;
    private LocalJobStatus(boolean isFinal) {
        this.isFinal = isFinal;
    }
    
    public boolean isFinal() {
        return isFinal;
    }
}
