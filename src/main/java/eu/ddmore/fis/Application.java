/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

import com.mango.mif.MIFHttpRestClient;

import eu.ddmore.fis.configuration.Languages;
import eu.ddmore.fis.controllers.GlobalLoggingRestExceptionHandler;
import eu.ddmore.fis.service.cts.internal.CTSRestClientConfiguration;

@Configuration
@EnableAutoConfiguration
@Import(value = { RepositoryRestMvcConfiguration.class, CTSRestClientConfiguration.class, Languages.class})
@ImportResource("classpath:META-INF/application-context.xml")
@ComponentScan({"eu.ddmore.fis.service.mif"} )
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public GlobalLoggingRestExceptionHandler globalLoggingRestExceptionHandler() {
        return new GlobalLoggingRestExceptionHandler();
    }
    
    @Bean
    public MIFHttpRestClient mifRestClient(@Value("${mif.url}") String mifUrl) {
    	return new MIFHttpRestClient(mifUrl);
    }
} 