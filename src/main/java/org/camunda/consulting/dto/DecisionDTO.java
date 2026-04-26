package org.camunda.consulting.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DecisionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("decisionDefinitionId")
    private String decisionDefinitionId;

    @JsonProperty("decisionDefinitionKey")
    private String decisionDefinitionKey;

    @JsonProperty("variables")
    private Map<String, Object> variables;

    public DecisionDTO() {
    }

    public DecisionDTO(String decisionDefinitionId, String decisionDefinitionKey, Map<String, Object> variables) {
        this.decisionDefinitionId = decisionDefinitionId;
        this.decisionDefinitionKey = decisionDefinitionKey;
        this.variables = variables;
    }

    public String getDecisionDefinitionId() {
        return decisionDefinitionId;
    }

    public void setDecisionDefinitionId(String decisionDefinitionId) {
        this.decisionDefinitionId = decisionDefinitionId;
    }

    public String getDecisionDefinitionKey() {
        return decisionDefinitionKey;
    }

    public void setDecisionDefinitionKey(String decisionDefinitionKey) {
        this.decisionDefinitionKey = decisionDefinitionKey;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}
