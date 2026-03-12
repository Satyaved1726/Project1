package com.safevoice.config;

import java.net.URI;
import java.net.URISyntaxException;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * DataSource Configuration for Supabase PostgreSQL and Render deployment
 * Handles both DATABASE_URL format (postgresql://user:password@host:port/db)
 * and direct JDBC configuration
 */
@Configuration
public class DataSourceConfiguration {

    @Bean
    @Primary
    public DataSource dataSource() throws URISyntaxException {
        String databaseUrl = System.getenv("DATABASE_URL");
        String dbUsername = System.getenv("DB_USERNAME");
        String dbPassword = System.getenv("DB_PASSWORD");
        String dbHost = System.getenv("DATABASE_HOST");

        // RENDER DEPLOYMENT: DATABASE_URL must be set in Render environment
        // Format: postgresql://postgres:PASSWORD@db.XXXXX.supabase.co:5432/postgres
        if ((databaseUrl == null || databaseUrl.isEmpty()) && (dbHost == null || dbHost.isEmpty())) {
            throw new IllegalStateException(
                "\n========================================\n" +
                "CRITICAL: Database configuration missing!\n" +
                "========================================\n" +
                "The DATABASE_URL environment variable is not set.\n\n" +
                "For Render deployment:\n" +
                "1. Go to Render Dashboard > Your Service > Settings > Environment\n" +
                "2. Add environment variable:\n" +
                "   DATABASE_URL = postgresql://postgres:PASSWORD@db.XXXXX.supabase.co:5432/postgres\n\n" +
                "Replace:\n" +
                "- PASSWORD with your Supabase database password\n" +
                "- db.XXXXX.supabase.co with your Supabase host\n\n" +
                "For local development:\n" +
                "- Set DATABASE_URL in your .env file or system environment\n" +
                "- Or set DATABASE_HOST, DATABASE_PORT, DATABASE_NAME, DB_USERNAME, DB_PASSWORD\n" +
                "========================================\n"
            );
        }

        HikariConfig config = new HikariConfig();

        // If DATABASE_URL is provided and looks like postgres URI (not JDBC), parse it
        if (databaseUrl != null && !databaseUrl.isEmpty() && databaseUrl.startsWith("postgresql://")) {
            parsePostgresUrl(config, databaseUrl, dbUsername, dbPassword);
        } 
        // If DATABASE_URL looks like JDBC URL, use it directly
        else if (databaseUrl != null && !databaseUrl.isEmpty() && databaseUrl.startsWith("jdbc:")) {
            config.setJdbcUrl(databaseUrl);
            if (dbUsername != null) config.setUsername(dbUsername);
            if (dbPassword != null) config.setPassword(dbPassword);
        }
        // Fall back to component-based configuration (only if DATABASE_HOST is set)
        else if (dbHost != null && !dbHost.isEmpty()) {
            String port = System.getenv("DATABASE_PORT");
            String dbName = System.getenv("DATABASE_NAME");
            
            if (port == null || port.isEmpty()) port = "5432";
            if (dbName == null || dbName.isEmpty()) dbName = "postgres";
            if (dbUsername == null || dbUsername.isEmpty()) dbUsername = "postgres";

            // Supabase requires SSL
            String jdbcUrl = "jdbc:postgresql://" + dbHost + ":" + port + "/" + dbName + "?sslmode=require";
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(dbUsername);
            config.setPassword(dbPassword != null ? dbPassword : "");
        }

        // HikariCP configuration
        config.setMaximumPoolSize(Integer.parseInt(System.getenv().getOrDefault("DB_POOL_SIZE", "5")));
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setAutoCommit(true);
        config.setLeakDetectionThreshold(60000);
        config.setDriverClassName("org.postgresql.Driver");

        return new HikariDataSource(config);
    }

    private void parsePostgresUrl(HikariConfig config, String postgresUrl, String overrideUsername, String overridePassword) throws URISyntaxException {
        // Parse postgresql://user:password@host:port/database
        URI uri = new URI(postgresUrl);
        
        String host = uri.getHost();
        int port = uri.getPort() != -1 ? uri.getPort() : 5432;
        String database = uri.getPath() != null ? uri.getPath().replaceFirst("^/", "") : "postgres";
        
        String username = overrideUsername;
        String password = overridePassword;
        
        if (username == null || username.isEmpty()) {
            String userInfo = uri.getUserInfo();
            if (userInfo != null && !userInfo.isEmpty()) {
                String[] parts = userInfo.split(":", 2);
                username = parts[0];
                if (parts.length > 1) {
                    password = parts[1];
                }
            } else {
                username = "postgres";
            }
        }

        // Convert to JDBC URL with SSL for Supabase
        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database + "?sslmode=require";
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password != null ? password : "");
    }
}
