/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis;


import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.core.AnnotationRelProvider;
import org.springframework.hateoas.core.DefaultRelProvider;
import org.springframework.hateoas.core.DelegatingRelProvider;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.plugin.core.OrderAwarePluginRegistry;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mango.mif.MIFHttpRestClient;

import eu.ddmore.fis.controllers.GlobalLoggingRestExceptionHandler;
import eu.ddmore.fis.service.ServiceWorkingDirectory;
import eu.ddmore.fis.service.cts.internal.CTSRestClientConfiguration;
import eu.ddmore.fis.service.internal.WorkingDirectoriesReaper;


@Configuration
@EnableAutoConfiguration
@EnableJpaRepositories("eu.ddmore.fis.repository")
@Import(value = { RepositoryRestMvcConfiguration.class, CTSRestClientConfiguration.class})
@ComponentScan({"eu.ddmore.fis.service.mif", "eu.ddmore.fis.configuration"} )
@ImportResource("classpath:META-INF/application-context.xml")
public class Application {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public GlobalLoggingRestExceptionHandler globalLoggingRestExceptionHandler() {
        return new GlobalLoggingRestExceptionHandler();
    }

    @Bean
    public MIFHttpRestClient mifRestClient(@Value("${fis.mif.url}") String mifUrl, @Value("${fis.mif.management.url}") String managementUrl) {
        return new MIFHttpRestClient(mifUrl, managementUrl);
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        RelProvider defaultRelProvider = defaultRelProvider();
        RelProvider annotationRelProvider = annotationRelProvider();
 
        OrderAwarePluginRegistry<RelProvider, Class<?>> relProviderPluginRegistry = OrderAwarePluginRegistry
                .create(Arrays.asList(defaultRelProvider, annotationRelProvider));
 
        DelegatingRelProvider delegatingRelProvider = new DelegatingRelProvider(relProviderPluginRegistry);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new Jackson2HalModule());
        objectMapper
                .setHandlerInstantiator(new Jackson2HalModule.HalHandlerInstantiator(delegatingRelProvider, null, null));
        return objectMapper;
    }
 
    @Bean
    public DefaultRelProvider defaultRelProvider() {
        return new DefaultRelProvider();
    }
 
    @Bean
    public AnnotationRelProvider annotationRelProvider() {
        return new AnnotationRelProvider();
    }
    
    @Bean
    public ServiceWorkingDirectory serviceWorkingDirectory() {
    	return new ServiceWorkingDirectory();
    }
    
    @Bean
    public WorkingDirectoriesReaper workingDirectoriesReaper() {
    	return new WorkingDirectoriesReaper();
    }
} 