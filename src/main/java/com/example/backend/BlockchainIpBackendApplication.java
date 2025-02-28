package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.example.backend")
public class BlockchainIpBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlockchainIpBackendApplication.class, args);
    }


}
