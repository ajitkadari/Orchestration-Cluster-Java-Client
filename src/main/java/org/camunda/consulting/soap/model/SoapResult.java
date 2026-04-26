package org.camunda.consulting.soap.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlType;

import org.w3c.dom.Element;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SoapResult", propOrder = {"any"})
public class SoapResult {

    @XmlAnyElement
    private List<Element> any = new ArrayList<>();

    public List<Element> getAny() {
        return any;
    }

    public void setAny(List<Element> any) {
        this.any = any;
    }
}

