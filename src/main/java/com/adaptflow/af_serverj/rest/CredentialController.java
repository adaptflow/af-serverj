package com.adaptflow.af_serverj.rest;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adaptflow.af_serverj.common.exception.ServiceException;
import com.adaptflow.af_serverj.configuration.db.adaptflow.entity.Credential;
import com.adaptflow.af_serverj.configuration.db.adaptflow.entity.Service;
import com.adaptflow.af_serverj.configuration.db.adaptflow.service.credential.CredentialService;
import com.adaptflow.af_serverj.model.dto.CredentialDTO;
import com.adaptflow.af_serverj.model.dto.CredentialResponseTO;

@RestController
@RequestMapping("/api/credentials")
public class CredentialController {

    @Autowired
    private CredentialService credentialService;

    @GetMapping
    public List<CredentialResponseTO> getAllCredentials() {
        return credentialService.getAllCredentials();
    }

    @GetMapping("/{id}")
    public CredentialResponseTO getCredentialById(@PathVariable UUID id) {
        return credentialService.getCredentialByIdMapped(id);
    }

    @PostMapping
    public ResponseEntity<String> createCredential(@RequestBody CredentialDTO credential) throws ServiceException {
        return credentialService.createCredential(credential);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateCredential(@PathVariable UUID id, @RequestBody CredentialDTO updatedCredential) throws ServiceException {
        return credentialService.updateCredential(id, updatedCredential);
    }

    @DeleteMapping("/{id}")
    public void deleteCredential(@PathVariable UUID id) {
        credentialService.deleteCredential(id);
    }

    @GetMapping("/by-service/{serviceId}")
    public List<Credential> getCredentialsByService(@PathVariable UUID serviceId) {
        return credentialService.getCredentialsByService(serviceId);
    }

    @GetMapping("/services/by-provider/{providerId}")
    public Set<Service> getServicesByProvider(@PathVariable UUID providerId) {
        return credentialService.getServicesByProvider(providerId);
    }
}