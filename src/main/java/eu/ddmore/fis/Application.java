/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

import eu.ddmore.fis.configuration.Languages;
import eu.ddmore.fis.service.cts.internal.CTSRestClientConfiguration;

@Configuration
@EnableAutoConfiguration
@Import(value = { RepositoryRestMvcConfiguration.class, CTSRestClientConfiguration.class, Languages.class})
@ImportResource("classpath:META-INF/application-context.xml")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
} 