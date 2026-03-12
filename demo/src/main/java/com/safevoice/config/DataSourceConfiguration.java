package com.safevoice.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

/**
 * DataSourceConfiguration for Render deployment
 * Automatically parses DATABASE_URL environment variable into JDBC format
 * Only activates when DATABASE_URL environment variable is present
 */
@Configuration
public class DataSourceConfiguration {

    @Bean
    @Primary
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            // Let Spring use default datasource configuration
            return null;
        }

        try {
            // Parse Render's DATABASE_URL format: postgresql://user:password@host:port/database
            URI dbUri = new URI(databaseUrl);
            
            String host = dbUri.getHost();
            int port = dbUri.getPort() != -1 ? dbUri.getPort() : 5432;
            String database = dbUri.getPath().replaceFirst("^/", ""); // Remove leading slash
            
            String userInfo = dbUri.getUserInfo();
            if (userInfo == null) {
                throw new IllegalArgumentException("DATABASE_URL must contain user:password");
            }
            
            String[] credentials = userInfo.split(":", 2);
            String username = credentials[0];
            String password = credentials.length > 1 ? credentials[1] : "";

            // Create proper JDBC URL for PostgreSQL
            String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database + "?sslmode=require";

            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(jdbcUrl);
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            hikariConfig.setMaximumPoolSize(5);
            hikariConfig.setMinimumIdle(2);
            hikariConfig.setConnectionTimeout(10000);
            hikariConfig.setIdleTimeout(600000);
            hikariConfig.setMaxLifetime(1800000);
            hikariConfig.setAutoCommit(true);
            hikariConfig.setLeakDetectionThreshold(60000);

            return new HikariDataSource(hikariConfig);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse DATABASE_URL environment variable: " + e.getMessage(), e);
        }
    }
}
