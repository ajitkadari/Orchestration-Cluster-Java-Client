package org.camunda.consulting.soap;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoapUtilsTest {

    @Test
    void toSoapSuccessWrapsXmlPayloadInSoapEnvelope() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("decisionId", "discount-decision");
        payload.put("tenantId", "<default>");
        payload.put("message", "A&B");

        String soap = SoapUtils.toSoapSuccess(new ObjectMapper(), payload);

        assertTrue(soap.contains("<SOAP-ENV:Envelope"));
        assertTrue(soap.contains("<ns2:evaluateDecisionResponse"));
        assertTrue(soap.contains("<ns2:success>true</ns2:success>"));
        assertTrue(soap.contains("<ns2:result>"));
        assertTrue(soap.contains("<decisionId>discount-decision</decisionId>"));
        assertTrue(soap.contains("<tenantId>&lt;default&gt;</tenantId>"));
        assertTrue(soap.contains("<message>A&amp;B</message>"));
        assertTrue(soap.contains("</ns2:result>"));
    }

    @Test
    void toSoapErrorWrapsMessageInSoapEnvelopeAndEscapesXmlCharacters() {
        String soap = SoapUtils.toSoapError("invalid <input> & missing value");

        assertTrue(soap.contains("<SOAP-ENV:Envelope"));
        assertTrue(soap.contains("<ns2:evaluateDecisionResponse"));
        assertTrue(soap.contains("<ns2:success>false</ns2:success>"));
        assertTrue(soap.contains("<ns2:errorMessage>invalid &lt;input&gt; &amp; missing value</ns2:errorMessage>"));
    }

    @Test
    void toSoapDelegatesToSuccessMethodForBackwardCompatibility() {
        Map<String, Object> payload = Map.of("decisionId", "discount-decision");
        ObjectMapper objectMapper = new ObjectMapper();

        String oldMethod = SoapUtils.toSoap(objectMapper, payload);
        String newMethod = SoapUtils.toSoapSuccess(objectMapper, payload);

        assertEquals(newMethod, oldMethod);
    }

    @Test
    void toSoapResultConvertsObjectPayloadToXmlElements() {
        Map<String, Object> payload = Map.of("decision", "approved");

        var soapResult = SoapUtils.toSoapResult(new ObjectMapper(), payload);

        assertEquals(1, soapResult.getAny().size());
        assertEquals("decision", soapResult.getAny().getFirst().getTagName());
        assertEquals("approved", soapResult.getAny().getFirst().getTextContent());
    }
}


