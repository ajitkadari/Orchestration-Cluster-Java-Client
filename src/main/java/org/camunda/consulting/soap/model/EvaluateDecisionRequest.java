package org.camunda.consulting.soap.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EvaluateDecisionRequest", propOrder = {
        "decisionDefinitionId",
        "decisionDefinitionKey",
        "variables"
})
@XmlRootElement(name = "evaluateDecisionRequest", namespace = "http://camunda.org/consulting/decision-evaluation")
public class EvaluateDecisionRequest {

    private String decisionDefinitionId;
    private String decisionDefinitionKey;

    @XmlElement(nillable = true)
    private SoapDecisionVariables variables;

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

    public SoapDecisionVariables getVariables() {
        return variables;
    }

    public void setVariables(SoapDecisionVariables variables) {
        this.variables = variables;
    }
}

