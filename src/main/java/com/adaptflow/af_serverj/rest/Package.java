package com.adaptflow.af_serverj.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Package {
	
	@PostMapping("/export")
	String exportPackage(@RequestBody String pid) {
		return null;
	}
}
