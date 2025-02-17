package com.adaptflow.af_serverj.model.dto;

import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class CredentialResponseTO {
    private UUID id;
    private String credentialName;
    private UUID providerId;
    private String providerName;
    private List<ServiceDTO> services;
}
