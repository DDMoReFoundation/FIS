package eu.ddmore.fis;

import static org.mockito.Mockito.mock;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import com.mango.mif.MIFHttpRestClient;

import eu.ddmore.fis.configuration.Languages;
import eu.ddmore.fis.service.cts.internal.CTSRestClientConfiguration;

@Configuration
@EnableAutoConfiguration
@ImportResource({"classpath:META-INF/application-context.xml"})
@Import( value = { Languages.class, CTSRestClientConfiguration.class })
public class CommonIntegrationTestContextConfiguration {
	@Bean
	public MIFHttpRestClient mifRestClient() {
		return mock(MIFHttpRestClient.class);
	}
}
