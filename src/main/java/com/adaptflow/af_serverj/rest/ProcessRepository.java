package com.adaptflow.af_serverj.rest;

import java.util.List;
import java.util.Map;

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
import com.adaptflow.af_serverj.model.dto.ProcessDefinitionDTO;
import com.adaptflow.af_serverj.services.process.ProcessService;

@RestController
@RequestMapping("/api/process")
public class ProcessRepository {
	
	@Autowired
	ProcessService processService;
	
	@GetMapping
	ResponseEntity<List<ProcessDefinitionDTO>> getAllProcessDefinitionsList() throws ServiceException {
		List<ProcessDefinitionDTO> response = processService.getAllProcessDefinitionsList();
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/save")
	ResponseEntity<Map<String, String>> saveProcessDefinition(@RequestBody String payload) {
		return ResponseEntity.ok(processService.save(payload));
	}
	
	@GetMapping("/{processId}")
	ResponseEntity<ProcessDefinitionDTO> getProcessById(@PathVariable String processId) throws ServiceException {
		ProcessDefinitionDTO response = processService.getProcessDefinitionById(processId);
		return ResponseEntity.ok(response);
	}
	
	@PutMapping("/{processId}")
	ResponseEntity<Map<String, String>> updateProcessById(@PathVariable String processId, @RequestBody String payload) throws ServiceException {
		Map<String, String> response = processService.update(processId, payload);
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/execute/{processId}")
	ResponseEntity<String> executeProcessById(@PathVariable String processId) throws ServiceException {
		String response = processService.executeProcessByProcessId(processId);
		return ResponseEntity.ok(response);
	}
	
	@DeleteMapping("/{processId}")
	ResponseEntity<ProcessDefinitionDTO> deleteProcessById(@PathVariable String processId) throws ServiceException {
		processService.deleteProcessByProcessId(processId);
		return ResponseEntity.ok().build();
	}
	
}
