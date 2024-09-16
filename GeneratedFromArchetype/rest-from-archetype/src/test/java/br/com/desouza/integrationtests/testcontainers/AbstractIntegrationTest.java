package br.com.desouza.integrationtests.testcontainers;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.ClassRule;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.lifecycle.Startables;

@ContextConfiguration(initializers = AbstractIntegrationTest.Initializer.class)
public class AbstractIntegrationTest {

	static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		
		@ClassRule
		static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.33")
		.withStartupTimeoutSeconds(180);
				
		
		private static void startContainers() {
			Startables.deepStart(Stream.of(mysql)).join();
		}

		private static Map<String, String> createConnectionConfiguration() {
			return Map.of(
				"spring.datasource.url", mysql.getJdbcUrl(),
				"spring.datasource.username", mysql.getUsername(),
				"spring.datasource.password", mysql.getPassword()
			);
		}
		
		@SuppressWarnings({"unchecked", "rawtypes"})
		@Override
		public void initialize(ConfigurableApplicationContext applicationContext) {
			startContainers();
			ConfigurableEnvironment environment = applicationContext.getEnvironment();
			MapPropertySource testcontainers = new MapPropertySource("testcontainers",(Map) createConnectionConfiguration());
			environment.getPropertySources().addFirst(testcontainers);
		}
	}
}
