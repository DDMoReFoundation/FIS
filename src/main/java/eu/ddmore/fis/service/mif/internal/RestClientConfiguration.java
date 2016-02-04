/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.mif.internal;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import eu.ddmore.fis.service.integration.RemoteServiceShutdownHandler;
import eu.ddmore.fis.service.integration.ShutdownHandler;
import eu.ddmore.fis.service.integration.SimpleRemoteServiceHealthIndicator;

/**
 * Configuration which prepares MIF REST client components
 */
@Configuration
@ComponentScan("eu.ddmore.fis.service.mif")
public class RestClientConfiguration {
    
    @Bean
    public HealthIndicator mifHealth(@Qualifier("mifRestTemplate") RestTemplate restTemplate, @Value("${fis.mif.management.url}") String managementUrl, @Value("${fis.mif.management.healthcheck}") String healthcheckEndpoint) {
        return new SimpleRemoteServiceHealthIndicator(restTemplate, managementUrl, healthcheckEndpoint);
    }

    @Bean
    public ShutdownHandler mifShutdown(@Qualifier("mifRestTemplate") RestTemplate restTemplate, @Value("${fis.mif.management.url}") String managementUrl, @Value("${fis.mif.management.shutdown}") String shutdownEndpoint) {
        return new RemoteServiceShutdownHandler(restTemplate, managementUrl, shutdownEndpoint);
    }
    
    @Bean
    public RestTemplate mifRestTemplate() {
        List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
        converters.add(new FormHttpMessageConverter());
        converters.add(new ResourceHttpMessageConverter());
        converters.add(new StringHttpMessageConverter());
        converters.add(new MappingJackson2HttpMessageConverter());
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(converters);
        return restTemplate;
    }

}
