/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.mif;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import eu.ddmore.fis.service.integration.RemoteServiceSettings;


/**
 * Configuration object holding MIF integration settings
 */
@Component
@ConfigurationProperties(prefix="fis.mif")  
public class MIFServiceSettings extends RemoteServiceSettings {
}
