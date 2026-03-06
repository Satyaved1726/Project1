package com.safevoice.config;

import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

@Configuration
public class JpaConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                       DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.example.demo", "com.safevoice")
                .build();
    }
}