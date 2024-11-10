package com.adaptflow.af_serverj.configuration.db.adaptflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.adaptflow.af_serverj.configuration.db.adaptflow.entity.ModelEntity;

@Repository
public interface AdaptflowModelEntityRepository extends JpaRepository<ModelEntity, Long> {
	
}
