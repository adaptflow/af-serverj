package com.adaptflow.af_serverj.services.bpmn;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.StartEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adaptflow.af_serverj.process.services.AfProcessDefinition;

@Service
public class BpmnModelParser {

	@Autowired
	AfProcessDefinition afProcessDefinition;
	
	public void parse(String deploymentId) {
		List<String> taskSequence = new ArrayList<>();
		BpmnModel bpmnModel = this.afProcessDefinition.getBpmnModelByDeploymentId(deploymentId);
		List<FlowElement> flowElements = (List<FlowElement>) bpmnModel.getMainProcess().getFlowElements();
        StartEvent startEvent = null;
        EndEvent endEvent = null;
        for (FlowElement flowElement : flowElements) {
            if (flowElement instanceof StartEvent) {
                startEvent = (StartEvent) flowElement;
                continue;
            }
            if (flowElement instanceof EndEvent) {
                endEvent = (EndEvent) flowElement;
                continue;
            }
        }

        // Step 4: Ensure we found both Start and End events
        if (startEvent == null || endEvent == null) {
            throw new RuntimeException("StartEvent or EndEvent not found in the BPMN model");
        }
        // Step 5: Iterate over sequence flows starting from the StartEvent
        processSequenceFlows(startEvent, flowElements, taskSequence);

//        return taskSequence;
    }

    private static void processSequenceFlows(FlowElement flowElement, List<FlowElement> flowElements, List<String> taskSequence) {
        if (flowElement instanceof ServiceTask) {
            taskSequence.add(((ServiceTask) flowElement).getName());
        }
        // Get the outgoing sequence flows for the current flow element
        List<SequenceFlow> outgoingFlows = getOutgoingSequenceFlows(flowElement, flowElements);
        for (SequenceFlow sequenceFlow : outgoingFlows) {
            FlowElement targetElement = flowElements.stream()
                    .filter(f -> f.getId().equals(sequenceFlow.getTargetRef()))
                    .findFirst()
                    .orElse(null);

            if (targetElement != null) {
                if (targetElement instanceof EndEvent) {
                    taskSequence.add("EndEvent");
                    
                    return;
                }

                // Recursively process the next element
                processSequenceFlows(targetElement, flowElements, taskSequence);
            }
        }
    }

    private static List<SequenceFlow> getOutgoingSequenceFlows(FlowElement flowElement, List<FlowElement> flowElements) {
        List<SequenceFlow> outgoingFlows = new ArrayList<>();
        String elementType = flowElement.getClass().getName();
        switch(elementType) {
        case "StartEvent":
        	outgoingFlows.addAll(((StartEvent) flowElement).getOutgoingFlows());
        	break;
        case "ServiceTask":
        	outgoingFlows.addAll(((ServiceTask) flowElement).getOutgoingFlows());
        	break;
        }
        // Add other flow element types (like service tasks, script tasks) as needed
        return outgoingFlows;
    }
}
