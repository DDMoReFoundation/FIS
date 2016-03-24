/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.mif.internal;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.mango.mif.MIFHttpRestClient;

import eu.ddmore.fis.service.integration.RemoteServiceShutdownHandler;
import eu.ddmore.fis.service.integration.ShutdownHandler;
import eu.ddmore.fis.service.integration.SimpleRemoteServiceHealthIndicator;
import eu.ddmore.fis.service.mif.MIFServiceSettings;

/**
 * Configuration which prepares MIF REST client components
 */
@Configuration
@ComponentScan("eu.ddmore.fis.service.mif")
public class RestClientConfiguration {
    private static final String MIF_LOCAL = "!remoteMIF";
    private static final String MIF_REMOTE = "remoteMIF";
    /**
     * Configuration enabled only when FIS integrates with MIF remote instance which life-cycle it should
     * not control and which does not expose standard Spring Boot management HTTP endpoints.
     */
    @Configuration
    @Profile( {MIF_REMOTE} )
    public static class RemoteMIF {
        @Bean
        public HealthIndicator mifHealth(@Qualifier("mifRestClient") final MIFHttpRestClient mifRestClient) {
            return new HealthIndicator() {
                @Override
                public Health health() {
                    try {
                        mifRestClient.getClientAvailableConnectorDetails();
                        return Health.up().build();
                    } catch (Exception ex) {
                        return Health.down(ex).build();
                    }
                }
            };
        }
        
        @Bean
        public ShutdownHandler mifShutdown() {
            return new ShutdownHandler() {

                @Override
                public void invoke() {
                    //Do nothing
                }
            };
        }
    }
    /**
     * Configuration enabled only if FIS integrates with a local Spring Boot based
     * MIF instance. In such scenario FIS manages life-cycle of MIF.
     */
    @Configuration
    @Profile( {MIF_LOCAL} )
    public static class LocalMIF {
        @Autowired(required=true)
        private MIFServiceSettings mifServiceSettings;
        
        @Bean
        public HealthIndicator mifHealth(@Qualifier("mifRestTemplate") RestTemplate restTemplate) {
            return new SimpleRemoteServiceHealthIndicator(restTemplate, mifServiceSettings.getManagement().getUrl(), mifServiceSettings.getManagement().getHealthcheck());
        }

        @Bean
        public ShutdownHandler mifShutdown(@Qualifier("mifRestTemplate") RestTemplate restTemplate) {
            return new RemoteServiceShutdownHandler(restTemplate, mifServiceSettings.getManagement().getUrl(), mifServiceSettings.getManagement().getShutdown());
        }
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
