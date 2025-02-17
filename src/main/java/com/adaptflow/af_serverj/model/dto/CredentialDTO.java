package com.adaptflow.af_serverj.model.dto;

import java.util.Set;
import java.util.UUID;

import lombok.Data;

@Data
public class CredentialDTO {
    private UUID id;
    private String name;
    private UUID providerId;
    private Set<UUID> serviceIds;
    private String apiKey;
}
