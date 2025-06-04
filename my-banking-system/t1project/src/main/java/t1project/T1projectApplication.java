package t1project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.web.reactive.function.client.WebClient;

@EnableKafka
@SpringBootApplication
public class T1projectApplication {

	public static void main(String[] args) {
		SpringApplication.run(T1projectApplication.class, args);
	}
	@Bean
	public WebClient.Builder webClientBuilder() {
		return WebClient.builder();
	}
}
