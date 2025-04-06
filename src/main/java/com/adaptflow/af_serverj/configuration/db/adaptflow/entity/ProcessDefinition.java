package com.adaptflow.af_serverj.configuration.db.adaptflow.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "processDefinition", schema = "af_global")
public class ProcessDefinition {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String generalProperties;

    @Column(nullable = false)
    private String fields;

    @Column(nullable = false)
    private long createdAt;

    @Column(nullable = false)
    private long modifiedAt;

    @Column(nullable = false)
    private String createdBy;
}
