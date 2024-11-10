package com.adaptflow.af_serverj.configuration.db.activiti;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.adaptflow.af_serverj.configuration.db.activiti.repository",
    entityManagerFactoryRef = "activitiEntityManagerFactory",
    transactionManagerRef = "activitiTransactionManager"
)
public class ActivitiDatasourceConfiguration {
    @Bean
    @ConfigurationProperties("spring.datasource.activiti")
    public DataSourceProperties activitiDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "activitiDataSource")
    @Primary
    @ConfigurationProperties("spring.datasource.activiti.hikari")
    public DataSource activitiDataSource() {
        return activitiDataSourceProperties()
          .initializeDataSourceBuilder()
          .build();
    }
    
    @Primary
    @Bean(name = "activitiEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean activitiEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("activitiDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.adaptflow.af_serverj.configuration.db.activiti.entity")
                .persistenceUnit("activiti")
                .build();
    }

    @Primary
    @Bean(name = "activitiTransactionManager")
    public PlatformTransactionManager activitiTransactionManager(
            @Qualifier("activitiEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
