package org.camunda.consulting;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

public class DecisionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String decisionDefinitionId;
    private String decisionDefinitionKey;
    private Map<String, String> variables;

    public DecisionDTO() {
    }

    public DecisionDTO(String decisionDefinitionId, String decisionDefinitionKey, Map<String, String> variables) {
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

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }
}
