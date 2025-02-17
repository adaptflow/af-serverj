package com.adaptflow.af_serverj.configuration.db.adaptflow.repository.credential;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.adaptflow.af_serverj.configuration.db.adaptflow.entity.Service;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {
    Set<Service> findByProviderId(UUID providerId);
    
//    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.provider WHERE s.id = :id")
//    Service findServiceById(@Param("id") Long id);

    @Query("SELECT s FROM Service s WHERE s.id in :ids")
	Set<Service> findByIdIn(Set<UUID> ids);
    
//    @Query("SELECT s FROM Service s WHERE s.id in :ids")
//	Set<Service> findByProviderId(Long id);
    
}
