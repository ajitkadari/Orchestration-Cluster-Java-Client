package org.camunda.consulting;

import java.io.Serializable;

public class DecisionDTO implements Serializable {

	private static final long serialVersionUID = 1L;

    private String decisionDefinitionId;
    private String decisionDefinitionKey;
    private DecisionVariables decisionVariables;

	public DecisionDTO() {
	}

    public DecisionDTO(String decisionDefinitionId, String decisionDefinitionKey, DecisionVariables decisionVariables) {
        this.decisionDefinitionId = decisionDefinitionId;
        this.decisionDefinitionKey = decisionDefinitionKey;
        this.decisionVariables = decisionVariables;
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

    public DecisionVariables getDecisionVariables() {
        return decisionVariables;
    }

    public void setDecisionVariables(DecisionVariables decisionVariables) {
        this.decisionVariables = decisionVariables;
    }

}
