package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// include safevoice package in component scan and JPA repositories (legacy demo package excluded)
@SpringBootApplication(scanBasePackages = {"com.safevoice"})
@EnableJpaRepositories(basePackages = {"com.safevoice"})
public class Project1Application {

    public static void main(String[] args) {
        SpringApplication.run(Project1Application.class, args);
    }

}
