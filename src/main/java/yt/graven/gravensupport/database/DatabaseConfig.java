package yt.graven.gravensupport.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.springframework.context.annotation.Bean;

public class DatabaseConfig {

    @Bean
    HikariConfig hikariConfig(YamlConfiguration botConfiguration) {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:postgresql://%s:%s/%s"
                .formatted(
                        botConfiguration.getString("database.host"),
                        botConfiguration.getString("database.port"),
                        botConfiguration.getString("database.database")));
        config.setUsername(botConfiguration.getString("database.username"));
        config.setPassword(botConfiguration.getString("database.password"));
        config.setDriverClassName("org.postgresql.Driver");

        return config;
    }

    @Bean
    HikariDataSource hikariDataSource(HikariConfig config) {
        return new HikariDataSource(config);
    }

    @Bean
    Connection connection(HikariDataSource dataSource) throws SQLException {
        return dataSource.getConnection();
    }
}
