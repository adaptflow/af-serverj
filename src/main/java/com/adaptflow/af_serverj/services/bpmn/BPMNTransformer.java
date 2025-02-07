package com.adaptflow.af_serverj.services.bpmn;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class BPMNTransformer {
	
	@Autowired
    private RepositoryService repositoryService;
	
	@Autowired
	private RuntimeService runtimeService;
	
    private final ObjectMapper mapper = new ObjectMapper();
    
    private Logger logger = LoggerFactory.getLogger(BPMNTransformer.class);

    public String saveProcess() throws JsonMappingException, JsonProcessingException {
    	// Parse JSON
        String json = "{\"cells\":[{\"type\":\"standard.Start\",\"position\":{\"x\":20,\"y\":367},\"size\":{\"width\":50,\"height\":50},\"angle\":0,\"id\":\"d6d1a931-845d-4e1c-809e-8100b3d2f34d\",\"z\":1,\"attrs\":{\"label\":{\"text\":\"Start\"}}},{\"type\":\"standard.End\",\"position\":{\"x\":1192,\"y\":367},\"size\":{\"width\":50,\"height\":50},\"angle\":0,\"id\":\"60307c30-b978-47a3-ab43-242b7bd40623\",\"z\":2,\"attrs\":{\"label\":{\"text\":\"End\"}}},{\"position\":{\"x\":560.5,\"y\":43.21875},\"size\":{\"width\":150,\"height\":50},\"angle\":0,\"type\":\"af.llm.provider\",\"id\":\"13db8b2e-bd73-4bb2-adc0-fbe452d60ce2\",\"z\":3,\"attrs\":{\"label\":{\"text\":\"LLMProvider\"}}},{\"type\":\"standard.Link\",\"source\":{\"id\":\"d6d1a931-845d-4e1c-809e-8100b3d2f34d\"},\"target\":{\"id\":\"13db8b2e-bd73-4bb2-adc0-fbe452d60ce2\"},\"id\":\"9d9d74e6-659b-47e1-9745-fd589f478a10\",\"z\":4,\"attrs\":{}},{\"type\":\"standard.Link\",\"source\":{\"id\":\"13db8b2e-bd73-4bb2-adc0-fbe452d60ce2\"},\"target\":{\"id\":\"60307c30-b978-47a3-ab43-242b7bd40623\"},\"id\":\"a64f2d30-4ae3-43c6-84dd-e7fd1bcf4efc\",\"z\":5,\"attrs\":{}}]}";
        JsonNode rootNode = mapper.readTree(json);

        // Initialize BPMN Model
        BpmnModel bpmnModel = new BpmnModel();
        Process process = new Process();
        bpmnModel.addProcess(process);
        process.setId("dynamicProcess");
        process.setName("Process Name");

        // Mapping JSON nodes to BPMN elements	
        Map<String, FlowNode> nodesMap = new HashMap<>();
        Iterator<JsonNode> elements = rootNode.path("cells").elements();

        while (elements.hasNext()) {
            JsonNode element = elements.next();
            String type = element.path("type").asText();
            String id = "_" + element.path("id").asText();

            switch (type) {
                case "standard.Start":
                    StartEvent startEvent = new StartEvent();
                    startEvent.setId(id);
                    startEvent.setName(element.path("attrs").path("label").path("text").asText());
                    process.addFlowElement(startEvent);
                    nodesMap.put(id, startEvent);
                    break;

                case "standard.End":
                    EndEvent endEvent = new EndEvent();
                    endEvent.setId(id);
                    endEvent.setName(element.path("attrs").path("label").path("text").asText());
                    process.addFlowElement(endEvent);
                    nodesMap.put(id, endEvent);
                    break;

                case "af.llm.provider":
                    ServiceTask serviceTask = new ServiceTask();
                    serviceTask.setId(id);
                    serviceTask.setName(element.path("attrs").path("label").path("text").asText());

                    // Set the delegate class for actual logic
                    serviceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
                    serviceTask.setImplementation("com.adaptflow.af_serverj.features.llm.LLMProviderDelegate"); // Update with your package and class name

                    // Add input parameters
                    FieldExtension inputParam1 = new FieldExtension();
                    inputParam1.setFieldName("provider");
                    inputParam1.setStringValue("openai");
                    serviceTask.getFieldExtensions().add(inputParam1);

                    FieldExtension inputParam2 = new FieldExtension();
                    inputParam2.setFieldName("credentials");
                    inputParam2.setStringValue("openai-key");
                    serviceTask.getFieldExtensions().add(inputParam2);
                    
                    process.addFlowElement(serviceTask);
                    nodesMap.put(id, serviceTask);
                    break;

                case "standard.Link":
                    String sourceId = "_" + element.path("source").path("id").asText();
                    String targetId = "_" + element.path("target").path("id").asText();
                    FlowNode source = nodesMap.get(sourceId);
                    FlowNode target = nodesMap.get(targetId);
                    
                    if (source != null && target != null) {
                        SequenceFlow flow = new SequenceFlow();
                        flow.setId(id);
                        flow.setSourceRef(source.getId());
                        flow.setTargetRef(target.getId());
                        process.addFlowElement(flow);

                        source.getOutgoingFlows().add(flow);
                        target.getIncomingFlows().add(flow);
                    }
                    break;

                default:
                    System.out.println("Unknown type: " + type);
            }
        }

        // Convert BPMN model to XML
        BpmnXMLConverter converter = new BpmnXMLConverter();
        byte[] xmlBytes = converter.convertToXML(bpmnModel);

        InputStream is = new ByteArrayInputStream(xmlBytes);
        removeExistingProcessDeploymentByKey("dynamicProcess");
        
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().name("dynamicProcessDeployment");
        Deployment deployment = deploymentBuilder.addInputStream("dynamicProcess.bpmn20.xml", is).deploy();
        logger.info("Process deployment ID: " + deployment.getId());
        return deployment.getId();
    }

	private void removeExistingProcessDeploymentByKey(String key) {
		
		// Step 1: Get the current process definition ID (if any) to undeploy the previous version
		List<ProcessDefinition> existingDefinitions = repositoryService.createProcessDefinitionQuery()
		    .processDefinitionKey(key) // Make sure to use the same key for the process
		    .list();

		for (ProcessDefinition existingDefinition : existingDefinitions) {
		    // Step 2: Undeploy the previous process definition if one exists
		    repositoryService.deleteDeployment(existingDefinition.getDeploymentId(), true);  // true means delete cascade (deletes related process instances)
		}
	}
    
    
}
