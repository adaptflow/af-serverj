package com.adaptflow.af_serverj.configuration.db.login;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.adaptflow.af_serverj.configuration.db.login.repository", entityManagerFactoryRef = "loginEntityManagerFactory", transactionManagerRef = "loginTransactionManager")
public class LoginDataSourceConfiguration {
    @Bean
    @ConfigurationProperties("spring.datasource.login")
    public DataSourceProperties loginDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "loginDataSource")
    @ConfigurationProperties("spring.datasource.login.hikari")
    public DataSource loginDataSource() {
        return loginDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean(name = "loginEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean loginEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("loginDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.adaptflow.af_serverj.configuration.db.login.entity")
                .persistenceUnit("login")
                .build();
    }

    @Bean(name = "loginTransactionManager")
    public PlatformTransactionManager loginTransactionManager(
            @Qualifier("loginEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
