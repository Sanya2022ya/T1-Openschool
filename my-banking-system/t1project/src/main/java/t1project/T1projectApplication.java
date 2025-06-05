package t1project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@ComponentScan(basePackages = {
		"t1project",
		"com.mybank.common.kafka.service"
})
@EnableJpaRepositories(basePackages = {
		"t1project.repository"
})
@EntityScan(basePackages = {
		"t1project.model"
})
@EnableKafka
public class T1projectApplication {

	public static void main(String[] args) {
		SpringApplication.run(T1projectApplication.class, args);
	}
	@Bean
	public WebClient.Builder webClientBuilder() {
		return WebClient.builder();
	}
}