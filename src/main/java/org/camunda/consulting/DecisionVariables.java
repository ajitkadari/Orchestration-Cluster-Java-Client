package org.camunda.consulting;

import java.io.Serializable;

public class DecisionVariables implements Serializable {

    private String team;
    private String state;

    public DecisionVariables() {
    }

    public DecisionVariables(String team, String state) {
        this.team = team;
        this.state = state;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

}
