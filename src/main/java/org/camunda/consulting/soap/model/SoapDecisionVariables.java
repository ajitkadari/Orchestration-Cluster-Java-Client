package org.camunda.consulting.soap.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SoapDecisionVariables", propOrder = {"entries"})
public class SoapDecisionVariables {

    @XmlElement(name = "entry")
    private List<SoapVariableEntry> entries = new ArrayList<>();

    public List<SoapVariableEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<SoapVariableEntry> entries) {
        this.entries = entries;
    }
}
