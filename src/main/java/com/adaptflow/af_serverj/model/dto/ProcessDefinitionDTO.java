package com.adaptflow.af_serverj.model.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class ProcessDefinitionDTO {
    private UUID id;
    private String name;
    private String createdBy;
    private Object generalProperties;
    private Object fields;
    private Long createdAt;
    private Long modifiedAt;
    private String bpmnXml;
}
