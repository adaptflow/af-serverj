package com.adaptflow.af_serverj.model.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class ServiceDTO {
    private UUID id;
    private String name;

    public ServiceDTO(UUID id, String name) {
        this.id = id;
        this.name = name;
    }
}

