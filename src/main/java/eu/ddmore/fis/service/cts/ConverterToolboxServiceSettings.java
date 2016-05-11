/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.cts;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import eu.ddmore.fis.service.integration.RemoteServiceSettings;


/**
 * Configuration object holding CTS integration settings
 */
@Component
@ConfigurationProperties(prefix="fis.cts")  
public class ConverterToolboxServiceSettings extends RemoteServiceSettings {
}
