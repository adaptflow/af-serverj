package com.adaptflow.af_serverj.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adaptflow.af_serverj.common.exception.ServiceException;
import com.adaptflow.af_serverj.model.dto.ProcessDefinitionDTO;
import com.adaptflow.af_serverj.services.process.ProcessService;

@RestController
@RequestMapping("/api/process")
public class ProcessRepository {
	
	@Autowired
	ProcessService processService;
	
	@PostMapping("/save")
	String saveProcessDefinition(@RequestBody String payload) {
		return processService.save(payload);
	}
	
	@GetMapping("/")
	ResponseEntity<List<ProcessDefinitionDTO>> getAllProcessDefinitionsList() throws ServiceException {
		List<ProcessDefinitionDTO> response = processService.getAllProcessDefinitionsList();
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/{processId}")
	ResponseEntity<ProcessDefinitionDTO> getProcessByDeploymentId(@PathVariable String processId) throws ServiceException {
		ProcessDefinitionDTO response = processService.getProcessDefinitionById(processId);
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/execute/{processId}")
	ResponseEntity<String> executeProcessByDeploymentId(@PathVariable String processId) throws ServiceException {
		String response = processService.executeProcessByProcessId(processId);
		return ResponseEntity.ok(response);
	}
	
}
