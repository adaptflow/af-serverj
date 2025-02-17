package com.adaptflow.af_serverj.configuration.db.adaptflow.repository.credential;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.adaptflow.af_serverj.configuration.db.adaptflow.entity.Credential;
import com.adaptflow.af_serverj.model.dto.CredentialResponseTO;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, UUID> {
    List<Credential> findByServicesId(UUID serviceId);
    
    @Query(value = "SELECT "
            + "c.id as id, "
            + "c.name AS credentialName, "
            + "p.id as providerId, "
            + "p.name as providerName, "
            + "s.id as serviceId, "
            + "s.name as serviceName "
            + "FROM af_global.credentials c "
            + "JOIN af_global.providers p ON c.provider_id = p.id "
            + "JOIN af_global.credential_services cs ON c.id = cs.credential_id "
            + "JOIN af_global.services s ON cs.service_id = s.id", nativeQuery = true)
    List<Object[]> findAllWithServices();
    
    @Query(value = "SELECT "
            + "c.id as id, "
            + "c.name AS credentialName, "
            + "p.id as providerId, "
            + "p.name as providerName, "
            + "s.id as serviceId, "
            + "s.name as serviceName "
            + "FROM af_global.credentials c "
            + "JOIN af_global.providers p ON c.provider_id = p.id "
            + "JOIN af_global.credential_services cs ON c.id = cs.credential_id "
            + "JOIN af_global.services s ON cs.service_id = s.id "
            + "where c.id = :id", nativeQuery = true)
    List<Object[]> findWithServicesById(UUID id);
}
