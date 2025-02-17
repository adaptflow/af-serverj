package com.adaptflow.af_serverj.configuration.db.adaptflow.service.credential;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import com.adaptflow.af_serverj.common.exception.ErrorCode;
import com.adaptflow.af_serverj.common.exception.ServiceException;
import com.adaptflow.af_serverj.configuration.db.adaptflow.entity.Credential;
import com.adaptflow.af_serverj.configuration.db.adaptflow.entity.Provider;
import com.adaptflow.af_serverj.configuration.db.adaptflow.entity.Service;
import com.adaptflow.af_serverj.configuration.db.adaptflow.repository.credential.CredentialRepository;
import com.adaptflow.af_serverj.configuration.db.adaptflow.repository.credential.ProviderRepository;
import com.adaptflow.af_serverj.configuration.db.adaptflow.repository.credential.ServiceRepository;
import com.adaptflow.af_serverj.model.dto.CredentialDTO;
import com.adaptflow.af_serverj.model.dto.CredentialResponseTO;

@org.springframework.stereotype.Service
public class CredentialService {

	@Autowired
	private CredentialRepository credentialRepository;

	@Autowired
	private ProviderRepository providerRepository;

	@Autowired
	private ServiceRepository serviceRepository;
	
	public List<CredentialResponseTO> getAllCredentials() {
	    return ObjectMapper.toAllCredentialsResponse(credentialRepository.findAllWithServices());
	}


	public Credential getCredentialById(UUID id) {
		return credentialRepository.findById(id).orElseThrow(() -> new RuntimeException("Credential not found"));
	}
	
	public CredentialResponseTO getCredentialByIdMapped(UUID id) {
		return ObjectMapper.toCredentialResponseTO(credentialRepository.findWithServicesById(id));
	}

	@Transactional
	public ResponseEntity<String> createCredential(CredentialDTO request) throws ServiceException {
		// Fetch provider
		Optional<Provider> provider = providerRepository.findById(request.getProviderId());
		// Fetch services
		Set<Service> services = serviceRepository.findByIdIn(request.getServiceIds());
		if (!provider.isPresent()) {
			throw new ServiceException(ErrorCode.BAD_REQUEST);
		}
		this.validateRequestParameters(request, services);
		// Create credential
		Credential credential = new Credential();
		if(request.getId()!=null) {
			credential.setId(request.getId());
		}
		credential.setName(request.getName());
		credential.setApiKey(request.getApiKey());
		credential.setProvider(provider.get());
		credential.setServices(services);

		Credential saved = credentialRepository.save(credential);
		return ResponseEntity.ok(saved.getId().toString());
	}

	private void validateRequestParameters(CredentialDTO request, Set<Service> services)
			throws ServiceException {
		Set<UUID> serviceIdsFromRequest = request.getServiceIds();
		Set<UUID> serviceIdsFromDb = services.stream().map(Service::getId).collect(Collectors.toSet());
		if (serviceIdsFromRequest == null) {
			throw new ServiceException(ErrorCode.BAD_REQUEST);
		} else {
			for (UUID serviceId : serviceIdsFromRequest) {
				if (!serviceIdsFromDb.contains(serviceId)) {
					throw new ServiceException(ErrorCode.BAD_REQUEST);
				}
			}
		}
	}
	
	@Transactional
	public ResponseEntity<String> updateCredential(UUID id, CredentialDTO updatedCredential) throws ServiceException {
		credentialRepository.deleteById(id);
		updatedCredential.setId(id);
        return createCredential(updatedCredential);
	}

//	@Transactional
//	public ResponseEntity<String> updateCredential(UUID id, CredentialDTO updatedCredential) throws ServiceException {
//		Optional<Credential> credentialOpt = credentialRepository.findById(id);
//        if (!credentialOpt.isPresent()) {
//        	throw new ServiceException(ErrorCode.BAD_REQUEST);
//        }
//        Credential credential = credentialOpt.get();
//
//        // Update the credential name (and optionally API key)
//        credential.setName(updatedCredential.getName());
//        if (updatedCredential.getApiKey() != null) {
//            credential.setApiKey(updatedCredential.getApiKey());
//        }
//		// Fetch services
//        serviceRepository.findAllById(updatedCredential.getServiceIds());
//		Set<Service> services = new HashSet<Service>(serviceRepository.findAllById(updatedCredential.getServiceIds()));
//		this.validateRequestParameters(updatedCredential, services);
//        credential.setServices(services);
//
//        Credential updated = credentialRepository.save(credential);
//        return ResponseEntity.ok(updated.getId().toString());
//	}

	public void deleteCredential(UUID id) {
		credentialRepository.deleteById(id);
	}

	public List<Credential> getCredentialsByService(UUID serviceId) {
		return credentialRepository.findByServicesId(serviceId);
	}

	public Set<Service> getServicesByProvider(UUID providerId) {
		return serviceRepository.findByProviderId(providerId);
	}
}