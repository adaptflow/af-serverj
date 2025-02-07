package com.adaptflow.af_serverj.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adaptflow.af_serverj.services.bpmn.BpmnModelParser;

@RestController
@RequestMapping("/export")
public class Package {
	
	@Autowired
	BpmnModelParser parser;
	
	@GetMapping("/{deploymentId}")
	ResponseEntity<String> exportPackage(@PathVariable String deploymentId) {
		parser.parse(deploymentId);
		return null;
	}
}
