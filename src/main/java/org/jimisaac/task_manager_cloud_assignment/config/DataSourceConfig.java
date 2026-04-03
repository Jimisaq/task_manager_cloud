package org.jimisaac.task_manager_cloud_assignment.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

@Configuration
public class DataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    private final Environment environment;

    public DataSourceConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public DataSource dataSource() throws URISyntaxException, IOException {
        logger.info("Configuring DataSource...");

        // Try to load .env file properties
        Properties envProps = new Properties();
        try {
            FileSystemResource envFile = new FileSystemResource(".env");
            if (envFile.exists()) {
                // Load .env file manually since it's not standard properties format
                java.util.List<String> lines = java.nio.file.Files.readAllLines(envFile.getFile().toPath());
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int equalsIndex = line.indexOf('=');
                    if (equalsIndex > 0) {
                        String key = line.substring(0, equalsIndex).trim();
                        String value = line.substring(equalsIndex + 1).trim();
                        // Remove quotes if present
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                        }
                        envProps.setProperty(key, value);
                    }
                }
                logger.info("Loaded .env file with {} properties", envProps.size());
            } else {
                logger.info(".env file not found");
            }
        } catch (Exception e) {
            logger.warn("Could not load .env file: {}", e.getMessage());
        }

        // Check for Railway DATABASE_URL first (from environment or .env)
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null) {
            databaseUrl = envProps.getProperty("DATABASE_URL");
        }
        logger.info("DATABASE_URL: {}", databaseUrl);

        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            logger.info("Using Railway DATABASE_URL configuration");
            logger.info("Full DATABASE_URL: {}", databaseUrl);
            // Production: Parse Railway DATABASE_URL
            URI dbUri = new URI(databaseUrl);

            logger.info("URI components - Scheme: {}, UserInfo: {}, Host: {}, Port: {}, Path: {}",
                       dbUri.getScheme(), dbUri.getUserInfo(), dbUri.getHost(), dbUri.getPort(), dbUri.getPath());

            if (dbUri.getUserInfo() != null) {
                String username = dbUri.getUserInfo().split(":")[0];
                String password = dbUri.getUserInfo().split(":")[1];
                String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

                logger.info("Parsed Railway URL - Host: {}, Port: {}, Database: {}", dbUri.getHost(), dbUri.getPort(), dbUri.getPath());

                DriverManagerDataSource dataSource = new DriverManagerDataSource();
                dataSource.setDriverClassName("org.postgresql.Driver");
                dataSource.setUrl(dbUrl);
                dataSource.setUsername(username);
                dataSource.setPassword(password);

                return dataSource;
            } else {
                logger.error("DATABASE_URL found but userInfo is null! This indicates Railway DATABASE_URL format is unexpected.");
                logger.error("Expected format: postgresql://username:password@host:port/database");
                logger.error("Actual DATABASE_URL: {}", databaseUrl);
                throw new RuntimeException("Invalid DATABASE_URL format from Railway - no user credentials found");
            }
        }

        // Check for local development SPRING_DATASOURCE_* variables (from environment or .env)
        String springUrl = environment.getProperty("SPRING_DATASOURCE_URL");
        if (springUrl == null) {
            springUrl = envProps.getProperty("SPRING_DATASOURCE_URL");
        }

        String springUsername = environment.getProperty("SPRING_DATASOURCE_USERNAME");
        if (springUsername == null) {
            springUsername = envProps.getProperty("SPRING_DATASOURCE_USERNAME");
        }

        String springPassword = environment.getProperty("SPRING_DATASOURCE_PASSWORD");
        if (springPassword == null) {
            springPassword = envProps.getProperty("SPRING_DATASOURCE_PASSWORD");
        }

        logger.info("SPRING_DATASOURCE_URL: {}", springUrl);
        logger.info("SPRING_DATASOURCE_USERNAME: {}", springUsername);

        if (springUrl != null && !springUrl.isEmpty()) {
            logger.info("Using local SPRING_DATASOURCE configuration");
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setUrl(springUrl);
            dataSource.setUsername(springUsername != null ? springUsername : "");
            dataSource.setPassword(springPassword != null ? springPassword : "");

            return dataSource;
        }

        // Test environment: Use H2
        logger.info("Using H2 test database");
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:testdb");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        return dataSource;
    }
}
