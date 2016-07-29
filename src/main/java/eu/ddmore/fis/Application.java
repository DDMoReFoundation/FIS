/*******************************************************************************
 * Copyright (C) 2016 Mango Business Solutions Ltd, http://www.mango-solutions.com
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
 *******************************************************************************/
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