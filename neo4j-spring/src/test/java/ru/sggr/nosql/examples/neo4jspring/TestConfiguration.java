package ru.sggr.nosql.examples.neo4jspring;

import org.neo4j.ogm.session.SessionFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@EnableAutoConfiguration
@Configuration
@EnableNeo4jRepositories(basePackages = "ru.sggr.nosql.examples.neo4jspring")
public class TestConfiguration extends Neo4jConfiguration {

    @Bean
    public org.neo4j.ogm.config.Configuration getConfiguration() {
        org.neo4j.ogm.config.Configuration config = new org.neo4j.ogm.config.Configuration();
        config.driverConfiguration().setDriverClassName("org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver");
        return config;
    }

    @Override
    public SessionFactory getSessionFactory() {
        return new SessionFactory(getConfiguration(), "ru.sggr.nosql.examples.neo4jspring");
    }
}
