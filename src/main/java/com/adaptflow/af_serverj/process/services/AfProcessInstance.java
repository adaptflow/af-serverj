package com.adaptflow.af_serverj.process.services;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.api.process.model.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AfProcessInstance {
    @Autowired
    private RepositoryService repositoryService;
    
	@Autowired
	private ProcessRuntime processRuntime;
    
    private Logger logger = LoggerFactory.getLogger(AfProcessInstance.class);
    
    public String getProcessInstanceById(String id) {
    	BpmnModel bpmnModel = repositoryService.getBpmnModel(id);
    	logger.info("");
    	byte[] xmlBytes = (new BpmnXMLConverter()).convertToXML(bpmnModel, StandardCharsets.UTF_8.name());
        return new String(xmlBytes, StandardCharsets.UTF_8);
    }
    
    public void getAllProcessDefinitions() {
    	try {
            Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0, 10));
            logger.info("> Available Process definitions: " + processDefinitionPage.getTotalItems());
            for (ProcessDefinition pd : processDefinitionPage.getContent()) {
                logger.info("\t > Process definition: " + pd);
            }	
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
}
