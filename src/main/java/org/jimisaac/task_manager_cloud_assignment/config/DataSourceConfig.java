package org.jimisaac.task_manager_cloud_assignment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DataSourceConfig {

    private final Environment environment;

    public DataSourceConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public DataSource dataSource() throws URISyntaxException {
        String databaseUrl = environment.getProperty("DATABASE_URL");

        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            // Production: Parse Railway DATABASE_URL
            URI dbUri = new URI(databaseUrl);

            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setUrl(dbUrl);
            dataSource.setUsername(username);
            dataSource.setPassword(password);

            return dataSource;
        } else {
            // Test environment: Use H2
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:mem:testdb");
            dataSource.setUsername("sa");
            dataSource.setPassword("");

            return dataSource;
        }
    }
}
