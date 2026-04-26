package org.camunda.consulting.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderProcessDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("processDefinitionId")
    private String processDefinitionId;

    @JsonProperty("processDefinitionKey")
    private String processDefinitionKey;

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("variables")
    private Map<String, Object> variables;

    public OrderProcessDTO() {
    }

    public OrderProcessDTO(String processDefinitionId, String processDefinitionKey, Integer version, String tenantId, Map<String, Object> variables) {
        this.processDefinitionId = processDefinitionId;
        this.processDefinitionKey = processDefinitionKey;
        this.version = version;
        this.tenantId = tenantId;
        this.variables = variables;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}
