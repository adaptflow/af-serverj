package com.adaptflow.af_serverj.configuration.flyway;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class InitializeAdapflowFlywayMigratyion {
	@Autowired
	@Qualifier("adaptflowDataSource")
	private DataSource adaptflowDataSource;

    public Flyway adaptflowFlyway() {
        return Flyway.configure()
                .dataSource(adaptflowDataSource)
                .baselineOnMigrate(true)
                .locations("classpath:/flyway/scripts/adaptflow")
                .load();
    }

    @PostConstruct
    public void migrateAdaptflow() {
        adaptflowFlyway().migrate();
    }
}
