package com.adaptflow.af_serverj.configuration.db.adaptflow.repository.credential;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.adaptflow.af_serverj.configuration.db.adaptflow.entity.ProcessDefinition;

@Repository
public interface ProcessDefinitionRepository extends JpaRepository<ProcessDefinition, UUID> {
}
