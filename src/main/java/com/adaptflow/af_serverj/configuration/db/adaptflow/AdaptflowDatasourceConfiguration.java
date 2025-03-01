package com.adaptflow.af_serverj.configuration.db.adaptflow;

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
@EnableJpaRepositories(
    basePackages = "com.adaptflow.af_serverj.configuration.db.adaptflow.repository",
    entityManagerFactoryRef = "adaptflowEntityManagerFactory",
    transactionManagerRef = "adaptflowTransactionManager"
)
public class AdaptflowDatasourceConfiguration {
    @Bean
    @ConfigurationProperties("spring.datasource.adaptflow")
    public DataSourceProperties adaptflowDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "adaptflowDataSource")
    @ConfigurationProperties("spring.datasource.adaptflow.hikari")
    public DataSource adaptflowDataSource() {
        return adaptflowDataSourceProperties()
          .initializeDataSourceBuilder()
          .build();
    }
    
    @Bean(name = "adaptflowEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean adaptflowEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("adaptflowDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.adaptflow.af_serverj.configuration.db.adaptflow.entity")
                .persistenceUnit("adaptflow")
                .build();
    }

    @Bean(name = "adaptflowTransactionManager")
    public PlatformTransactionManager adaptflowTransactionManager(
            @Qualifier("adaptflowEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
