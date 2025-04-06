package com.adaptflow.af_serverj.services.process;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adaptflow.af_serverj.common.exception.ErrorCode;
import com.adaptflow.af_serverj.common.exception.ServiceException;
import com.adaptflow.af_serverj.configuration.db.adaptflow.repository.credential.ProcessDefinitionRepository;
import com.adaptflow.af_serverj.jwt.UserContextHolder;
import com.adaptflow.af_serverj.model.dto.ProcessDefinitionDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProcessService {

	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	ProcessDefinitionRepository processDefinitionRepository;
	
	@Autowired
	private RuntimeService runtimeService;
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	private Logger logger = LoggerFactory.getLogger(ProcessService.class);

	public String save(String payload) {
		try {
			JsonNode rootNode = objectMapper.readTree(payload);
			JsonNode bpmnXmlNode = rootNode.get("bpmnXml");
			JsonNode generalPropertiesNode = rootNode.get("generalProperties");
			JsonNode fieldsNode = rootNode.get("fields");
			if (bpmnXmlNode == null) {
				throw new IllegalArgumentException("Payload does not contain 'bpmnXml' field.");
			}
			String bpmnXml = bpmnXmlNode.asText();

			JsonNode processNameNode = generalPropertiesNode.get("processName");
			String processName = processNameNode.asText();
			
			InputStream bpmnStream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8));

			Deployment deployment = repositoryService.createDeployment()
					.addInputStream(processName + ".bpmn20.xml", bpmnStream)
					.name(processName)
					.deploy();

			ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
					.deploymentId(deployment.getId())
					.singleResult();
			
			if (processDefinition == null) {
				throw new RuntimeException("Process definition not found after deployment.");
			}
			this.saveProcessData(processDefinition.getId(), generalPropertiesNode, fieldsNode);
			return processDefinition.getId();

		} catch (JsonProcessingException e) {
			throw new RuntimeException("Error parsing JSON payload.", e);
		} catch (Exception e) {
			throw new RuntimeException("Error deploying process.", e);
		}
	}
	
	private void saveProcessData(String processId, JsonNode generalPropertiesNode, JsonNode fieldsNode) throws JsonProcessingException {
		com.adaptflow.af_serverj.configuration.db.adaptflow.entity.ProcessDefinition process = new com.adaptflow.af_serverj.configuration.db.adaptflow.entity.ProcessDefinition();
		process.setId(UUID.fromString(processId));
		process.setName(generalPropertiesNode.get("name").asText());
		process.setCreatedAt(Instant.now().toEpochMilli());
		process.setModifiedAt(Instant.now().toEpochMilli());
		process.setFields(objectMapper.writeValueAsString(fieldsNode));
		process.setGeneralProperties(objectMapper.writeValueAsString(generalPropertiesNode));
		process.setCreatedBy(UserContextHolder.get().getUsername());
		processDefinitionRepository.save(process);
	}
	
	public List<ProcessDefinitionDTO> getAllProcessDefinitionsList() throws ServiceException {
		List<com.adaptflow.af_serverj.configuration.db.adaptflow.entity.ProcessDefinition> processes = processDefinitionRepository.findAll();
		List<ProcessDefinitionDTO> processList = new ArrayList<ProcessDefinitionDTO>();
		for(com.adaptflow.af_serverj.configuration.db.adaptflow.entity.ProcessDefinition process : processes) {
			ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(process.getId().toString()).singleResult();
			if(processDefinition == null) {
				throw new ServiceException(ErrorCode.BAD_REQUEST, "Process definition not found");
			}
			ProcessDefinitionDTO processDto = new ProcessDefinitionDTO();
			processDto.setId(process.getId());
			processDto.setName(process.getName());
			processDto.setCreatedBy(process.getName());
			processDto.setGeneralProperties(process.getGeneralProperties());
			processDto.setFields(process.getFields());
			processDto.setCreatedAt(process.getCreatedAt());
			processDto.setModifiedAt(process.getModifiedAt());
			processList.add(processDto)
		}
		return processList;
		
	}
	
	public ProcessDefinitionDTO getProcessDefinitionById(String processId) throws ServiceException {
		com.adaptflow.af_serverj.configuration.db.adaptflow.entity.ProcessDefinition process = processDefinitionRepository.findById(UUID.fromString(processId)).orElse(null);
		if(process == null || !UserContextHolder.get().getUsername().equals(process.getCreatedBy())) {
			throw new ServiceException(ErrorCode.BAD_REQUEST, "Process definition not found");
		}
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processId).singleResult();
		if(processDefinition == null) {
			throw new ServiceException(ErrorCode.BAD_REQUEST, "Process definition not found");
		}
		ProcessDefinitionDTO processDto = new ProcessDefinitionDTO();
		processDto.setId(process.getId());
		processDto.setName(process.getName());
		processDto.setCreatedBy(process.getName());
		processDto.setGeneralProperties(process.getGeneralProperties());
		processDto.setFields(process.getFields());
		processDto.setCreatedAt(process.getCreatedAt());
		processDto.setModifiedAt(process.getModifiedAt());
		return processDto;
	}

	public String executeProcessByProcessId(String processId) throws ServiceException {
		try {
			ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
					.processDefinitionId(processId)
					.singleResult();

			if (processDefinition == null) {
				throw new ServiceException(ErrorCode.BAD_REQUEST, "Process definition not found: " + processId);
			}
			ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
			logger.info("Process instance started successfully. Process Instance ID: " + processInstance.getId());
			return processInstance.getId();
		} catch (Exception e) {
			throw new ServiceException(ErrorCode.SERVER_ERROR, "Error executing process.");
		}
	}
}
