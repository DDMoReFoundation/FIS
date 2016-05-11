/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.cts.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ddmore.fis.service.cts.ConverterToolboxServiceSettings;
import eu.ddmore.fis.service.integration.RemoteServiceShutdownHandler;
import eu.ddmore.fis.service.integration.ShutdownHandler;
import eu.ddmore.fis.service.integration.SimpleRemoteServiceHealthIndicator;

/**
 * Configuration which prepares Converter Toolbox Service REST client components
 */
@Configuration
@ComponentScan("eu.ddmore.fis.service.cts")
public class CTSRestClientConfiguration {
    private static final String CTS_LOCAL = "!remoteCTS";
    private static final String CTS_REMOTE = "remoteCTS";
    /**
     * Configuration enabled only when FIS integrates with remote CTS instance which life-cycle it should
     * not control.
     */
    @Configuration
    @Profile( {CTS_REMOTE} )
    public static class RemoteMIF {

        @Autowired(required=true)
        private ConverterToolboxServiceSettings ctsSettings;

        @Bean
        public HealthIndicator ctsHealth(@Qualifier("ctsRestTemplate") RestTemplate restTemplate) {
            return new SimpleRemoteServiceHealthIndicator(restTemplate, ctsSettings.getManagement().getUrl(), ctsSettings.getManagement().getHealthcheck());
        }
        
        @Bean
        public ShutdownHandler ctsShutdown() {
            return new ShutdownHandler() {

                @Override
                public void invoke() {
                    //Do nothing
                }
            };
        }
    }
    /**
     * Configuration enabled only if FIS integrates with a local CTS instance. In such scenario FIS manages life-cycle of CTS.
     */
    @Configuration
    @Profile( {CTS_LOCAL} )
    public static class LocalMIF {
        @Autowired(required=true)
        private ConverterToolboxServiceSettings ctsSettings;

        @Bean
        public HealthIndicator ctsHealth(@Qualifier("ctsRestTemplate") RestTemplate restTemplate) {
            return new SimpleRemoteServiceHealthIndicator(restTemplate, ctsSettings.getManagement().getUrl(), ctsSettings.getManagement().getHealthcheck());
        }

        @Bean
        public ShutdownHandler ctsShutdown(@Qualifier("ctsRestTemplate") RestTemplate restTemplate) {
            return new RemoteServiceShutdownHandler(restTemplate, ctsSettings.getManagement().getUrl(), ctsSettings.getManagement().getShutdown());
        }
    }
    
    @Bean
    public RestTemplate ctsRestTemplate(@Qualifier("halConverter") MappingJackson2HttpMessageConverter halConverter) {
        List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
        converters.add(new FormHttpMessageConverter());
        converters.add(new ResourceHttpMessageConverter());
        converters.add(halConverter);
        converters.add(new StringHttpMessageConverter());
        converters.add(new MappingJackson2HttpMessageConverter());
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(converters);
        return restTemplate;
    }

    /**
     * 
     * @return converter capable of handling HAL-enabled domain objects
     */
    @Bean
    public MappingJackson2HttpMessageConverter halConverter(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter halConverter = new MappingJackson2HttpMessageConverter();
        halConverter.setSupportedMediaTypes(Arrays.asList(MediaTypes.HAL_JSON));
        halConverter.setObjectMapper(objectMapper);
        return halConverter;
    }

}
