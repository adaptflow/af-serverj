package com.adaptflow.af_serverj.configuration.db.adaptflow.service.credential;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.adaptflow.af_serverj.model.dto.CredentialResponseTO;
import com.adaptflow.af_serverj.model.dto.ServiceDTO;

@org.springframework.stereotype.Service
public class ObjectMapper {
	
	public static List<CredentialResponseTO> toAllCredentialsResponse(List<Object[]> queryResults) {
		Map<UUID, CredentialResponseTO> credentialMap = getCredentialMap(queryResults);
	    return new ArrayList<>(credentialMap.values());
	}
	
	public static CredentialResponseTO toCredentialResponseTO(List<Object[]> queryResults) {
		Map<UUID, CredentialResponseTO> credentialMap = getCredentialMap(queryResults);
	    return credentialMap.values().iterator().next();
	}
	
	private static Map<UUID, CredentialResponseTO> getCredentialMap(List<Object[]> queryResults) {
		Map<UUID, CredentialResponseTO> credentialMap = new HashMap<>();
	    
	    for (Object[] result : queryResults) {
	    	UUID credentialId = (UUID) result[0];
	        String credentialName = (String) result[1];
	        UUID providerId = (UUID) result[2];
	        String providerName = (String) result[3];
	        UUID serviceId = (UUID) result[4];
	        String serviceName = (String) result[5];
	        
	        // Create or update CredentialResponseTO object in the map
	        CredentialResponseTO credentialResponse = credentialMap.computeIfAbsent(credentialId, id -> {
	            CredentialResponseTO newCredential = new CredentialResponseTO();
	            newCredential.setId(credentialId);
	            newCredential.setCredentialName(credentialName);
	            newCredential.setProviderId(providerId);
	            newCredential.setProviderName(providerName);
	            newCredential.setServices(new ArrayList<>());
	            return newCredential;
	        });
	        
	        // Add service to the services list
	        credentialResponse.getServices().add(new ServiceDTO(serviceId, serviceName));
	    }
	    return credentialMap;
	}
}
