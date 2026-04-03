package org.jimisaac.task_manager_cloud_assignment.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
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
                envProps = PropertiesLoaderUtils.loadProperties(envFile);
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
            // Production: Parse Railway DATABASE_URL
            URI dbUri = new URI(databaseUrl);

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
                logger.warn("DATABASE_URL found but userInfo is null: {}", databaseUrl);
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
