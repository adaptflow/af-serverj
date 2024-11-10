package com.adaptflow.af_serverj.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.adaptflow.af_serverj.process.services.AfProcessInstance;
import com.adaptflow.af_serverj.services.bpmn.BPMNTransformer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@RestController
@RequestMapping("/process")
public class ManageProcess {

	@Autowired
	BPMNTransformer bpmnTransformer;
	
	@Autowired
	AfProcessInstance afProcessInstance;
	
	@PostMapping("/save")
	String saveProcessInstance(@RequestBody String payload) {
		try {
			return bpmnTransformer.saveProcess();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@GetMapping("/")
	ResponseEntity<String> getAllProcessDefinitions() {
		afProcessInstance.getAllProcessDefinitions();
		return ResponseEntity.ok().build();
	}
	
	@GetMapping("/{id}")
	ResponseEntity<String> getProcessInstanceById(@PathVariable String id) {
//		String formattedProcessId = "dynamicProcess" + ":1:" + id;
		return ResponseEntity.ok(afProcessInstance.getProcessInstanceById(id));
	}
}
