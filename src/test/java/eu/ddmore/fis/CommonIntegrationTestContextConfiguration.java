package eu.ddmore.fis;

import static org.mockito.Mockito.mock;

import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.endpoint.ShutdownEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import eu.ddmore.fis.service.cts.internal.CTSRestClientConfiguration;

@Configuration
@ImportResource({"classpath:META-INF/application-context.xml"})
@Import(CTSRestClientConfiguration.class)
public class CommonIntegrationTestContextConfiguration {
        @Bean
        public ShutdownEndpoint shutdownEndpoint() {
            return mock(ShutdownEndpoint.class);
        }
        @Bean
        public HealthEndpoint healthEndpoint() {
            return mock(HealthEndpoint.class);
        }
}
