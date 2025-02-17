package com.adaptflow.af_serverj.configuration.db.adaptflow.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Data
@Table(name = "providers", schema = "af_global")
public class Provider {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id; 
    
    @Column(nullable = false, unique = true)
    private String name;

//    @JsonManagedReference
    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Service> services;

    // Getters and Setters
}