package com.germogli.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {
		"com.germogli.backend.community.post.infrastructure.crud",
		"com.germogli.backend.community.post.domain.repository",
		"com.germogli.backend.authentication.infrastructure.crud" // si es necesario
})
@ComponentScan(basePackages = {
		"com.germogli.backend.community",
		"com.germogli.backend.common",
		"com.germogli.backend.user",
		"com.germogli.backend.authentication" // Se tiene que agregar para escanear el módulo de autenticacion
})
public class BackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
}
