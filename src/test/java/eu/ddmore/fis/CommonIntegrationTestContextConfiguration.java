package eu.ddmore.fis;

import static org.mockito.Mockito.mock;

import java.util.Map;

import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.endpoint.ShutdownEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({"classpath:META-INF/application-context.xml"})
public class CommonIntegrationTestContextConfiguration {
        @Bean
        public ShutdownEndpoint shutdownEndpoint() {
            return mock(ShutdownEndpoint.class);
        }
        @Bean
        public HealthEndpoint<Map<String,Object>> healthEndpoint() {
            return mock(HealthEndpoint.class);
        }
        
}
