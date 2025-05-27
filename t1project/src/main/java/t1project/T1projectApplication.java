package t1project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class T1projectApplication {

	public static void main(String[] args) {
		SpringApplication.run(T1projectApplication.class, args);
	}

}
