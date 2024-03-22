package com.jeju.nanaland;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class NanalandApplication {

	public static void main(String[] args) {
		SpringApplication.run(NanalandApplication.class, args);
	}

}
