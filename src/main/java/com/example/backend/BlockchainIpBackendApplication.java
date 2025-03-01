package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.backend")
@EntityScan("com.example.backend.model")
@EnableJpaRepositories("com.example.backend.repository")
public class BlockchainIpBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlockchainIpBackendApplication.class, args);
    }
}