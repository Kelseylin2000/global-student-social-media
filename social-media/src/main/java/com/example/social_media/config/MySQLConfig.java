package com.example.social_media.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.example.social_media.repository.mysql",
    transactionManagerRef = "mysqlTransactionManager"
)
public class MySQLConfig {

    @Bean(name = {"transactionManager", "mysqlTransactionManager"})
    public JpaTransactionManager mysqlTransactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}