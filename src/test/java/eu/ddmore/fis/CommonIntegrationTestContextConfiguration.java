/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis;

import static org.mockito.Mockito.mock;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.mango.mif.MIFHttpRestClient;

import eu.ddmore.fis.configuration.Languages;
import eu.ddmore.fis.service.ServiceWorkingDirectory;
import eu.ddmore.fis.service.cts.internal.CTSRestClientConfiguration;
import eu.ddmore.fis.service.mif.internal.RestClientConfiguration;

@Configuration
@EnableAutoConfiguration
@ImportResource({"classpath:META-INF/application-context.xml"})
@Import( value = { CTSRestClientConfiguration.class, RestClientConfiguration.class})
@ComponentScan(basePackageClasses={Languages.class} )
@EnableJpaRepositories("eu.ddmore.fis.repository")
public class CommonIntegrationTestContextConfiguration {
	@Bean
	public MIFHttpRestClient mifRestClient() {
		return mock(MIFHttpRestClient.class);
	}

    @Bean
    public ServiceWorkingDirectory serviceWorkingDirectory() {
    	return new ServiceWorkingDirectory();
    }
}
