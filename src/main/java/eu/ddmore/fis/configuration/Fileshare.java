/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.configuration;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Represents execution host file share
 */
@Component("fileshare")
@ConfigurationProperties(prefix="fis.fileshare") 
public class Fileshare {
    
    @NotBlank
    private String fisHostPath;

    @NotBlank
    private String mifHostPath;

    @NotBlank
    private String executionHostPath;

    public String getFisHostPath() {
        return fisHostPath;
    }

    public void setFisHostPath(String fisHostPath) {
        this.fisHostPath = fisHostPath;
    }

    public String getMifHostPath() {
        return mifHostPath;
    }

    public void setMifHostPath(String mifHostPath) {
        this.mifHostPath = mifHostPath;
    }

    public String getExecutionHostPath() {
        return executionHostPath;
    }

    public void setExecutionHostPath(String executionHostPath) {
        this.executionHostPath = executionHostPath;
    }

}
