package com.adaptflow.af_serverj.process.services;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AfProcessDefinition {
    @Autowired
    private RepositoryService repositoryService;
    
	@Autowired
	private RuntimeService runtimeService;
	
	@Autowired
	private HistoryService historyService;
    
    private Logger logger = LoggerFactory.getLogger(AfProcessDefinition.class);
    
    public String getProcessDefinitionById(String deploymentId) {
    	String processDefinitionId = this.getProcessDefinitionIdByDeploymentId(deploymentId);
    	BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
    	byte[] xmlBytes = (new BpmnXMLConverter()).convertToXML(bpmnModel, StandardCharsets.UTF_8.name());
        return new String(xmlBytes, StandardCharsets.UTF_8);
    }
    
    public BpmnModel getBpmnModelByDeploymentId(String deploymentId) {
    	String processDefinitionId = this.getProcessDefinitionIdByDeploymentId(deploymentId);
    	return repositoryService.getBpmnModel(processDefinitionId);
    }

	public String executeProcessByDeploymentId(String deploymentId) {
		String processDefinitionId = this.getProcessDefinitionIdByDeploymentId(deploymentId);
		ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinitionId);
//		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dynamicProcess");
//		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
//				.processDefinitionId(processDefinitionId).singleResult();
		HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
				.processInstanceId(processInstance.getId()).singleResult();

		if (historicProcessInstance != null) {
			HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery()
					.processInstanceId(processInstance.getId()).variableName("message").singleResult();

			logger.info("Message is: {}", historicVariable.getValue());
		} else {
			logger.error("Process is not executed!");
		}
		return processInstance.getId();
	}
	
    public void listAllCompletedProcessInstances() {
     // List all active process instances
        List<ProcessInstance> activeProcessInstances = runtimeService.createProcessInstanceQuery().list();
        for (ProcessInstance processInstance : activeProcessInstances) {
            System.out.println("Active Process Instance ID: " + processInstance.getId() +
                               ", Process Definition ID: " + processInstance.getProcessDefinitionId());
        }
        
        List<ProcessInstance> suspendedProcessInstances = runtimeService.createProcessInstanceQuery().suspended().list();
        for (ProcessInstance processInstance : suspendedProcessInstances) {
            System.out.println("Suspended Process Instance ID: " + processInstance.getId() +
                               ", Process Definition ID: " + processInstance.getProcessDefinitionId());
        }

        // List all completed process instances
        List<HistoricProcessInstance> completedProcessInstances = historyService.createHistoricProcessInstanceQuery().finished().list();
        for (HistoricProcessInstance historicProcessInstance : completedProcessInstances) {
            System.out.println("Completed Process Instance ID: " + historicProcessInstance.getId() +
                               ", Process Definition ID: " + historicProcessInstance.getProcessDefinitionId() +
                               ", End Time: " + historicProcessInstance.getEndTime());
        }
    }
	
	private String getProcessDefinitionIdByDeploymentId(String deploymentId) {
    	ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
    	.deploymentId(deploymentId)
    	.singleResult();
        if (processDefinition == null) {
            throw new IllegalArgumentException("Process definition not found with deploymentId " + deploymentId);
        }
        return processDefinition.getId();
	}
}
