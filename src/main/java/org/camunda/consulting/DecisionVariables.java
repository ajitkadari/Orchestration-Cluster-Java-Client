package org.camunda.consulting;

import java.io.Serializable;
import java.util.Map;

public class DecisionVariables implements Serializable {

    private Map<String, String> variables;

    public DecisionVariables() {
    }

    public DecisionVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }
}
