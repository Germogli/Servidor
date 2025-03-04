package com.germogli.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"com.germogli.backend.authentication.infrastructure.crud", "com.germogli.backend.authentication.domain.repository"})
@ComponentScan(basePackages = {"com.germogli.backend.authentication", "com.germogli.backend.common"})
public class BackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
}

