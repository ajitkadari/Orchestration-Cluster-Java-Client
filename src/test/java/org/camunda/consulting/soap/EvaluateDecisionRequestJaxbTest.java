package org.camunda.consulting.soap;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

import org.camunda.consulting.dto.DecisionDTO;
import org.camunda.consulting.service.DecisionService;
import org.camunda.consulting.soap.model.EvaluateDecisionRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import io.camunda.client.api.response.EvaluateDecisionResponse;
import io.camunda.client.api.response.EvaluatedDecision;

import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluateDecisionRequestJaxbTest {

    @Test
    void unmarshalsQualifiedSoapRequestElements() throws Exception {
        EvaluateDecisionRequest request = unmarshallRequest();

        assertEquals("nt-decision", request.getDecisionDefinitionId());
        assertNotNull(request.getVariables());
        assertEquals(2, request.getVariables().getEntries().size());
        assertEquals("team", request.getVariables().getEntries().getFirst().getKey());
        assertEquals("East Regional", valueAsText(request.getVariables().getEntries().getFirst().getValue()));
        assertEquals("state", request.getVariables().getEntries().get(1).getKey());
        assertEquals("Alabama", valueAsText(request.getVariables().getEntries().get(1).getValue()));
    }

    @Test
    void endpointNormalizesJaxbAnyTypeValuesBeforeCallingDecisionService() throws Exception {
        DecisionService decisionService = Mockito.mock(DecisionService.class);
        Mockito.when(decisionService.evaluate(Mockito.any())).thenReturn(minimalDecisionResponse());

        DecisionEvaluationSoapEndpoint endpoint = new DecisionEvaluationSoapEndpoint(decisionService, new ObjectMapper());

        endpoint.evaluateDecision(unmarshallRequest());

        ArgumentCaptor<DecisionDTO> captor = ArgumentCaptor.forClass(DecisionDTO.class);
        Mockito.verify(decisionService).evaluate(captor.capture());

        DecisionDTO dto = captor.getValue();
        assertEquals("nt-decision", dto.getDecisionDefinitionId());
        assertEquals("East Regional", dto.getVariables().get("team"));
        assertEquals("Alabama", dto.getVariables().get("state"));
    }

    @Test
    void endpointPreservesComplexNestedObjectStructure() throws Exception {
        String xml = """
                <dec:evaluateDecisionRequest xmlns:dec="http://camunda.org/consulting/decision-evaluation">
                  <dec:decisionDefinitionId>discount-decision</dec:decisionDefinitionId>
                  <dec:variables>
                    <dec:entry>
                      <dec:key>order</dec:key>
                      <dec:value>
                        <dec:customerType>VIP</dec:customerType>
                        <dec:total>1000</dec:total>
                        <dec:items>
                          <dec:category>ELECTRONICS</dec:category>
                          <dec:quantity>1</dec:quantity>
                        </dec:items>
                        <dec:items>
                          <dec:category>ELECTRONICS</dec:category>
                          <dec:quantity>1</dec:quantity>
                        </dec:items>
                      </dec:value>
                    </dec:entry>
                  </dec:variables>
                </dec:evaluateDecisionRequest>
                """;

        JAXBContext context = JAXBContext.newInstance(EvaluateDecisionRequest.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        var document = documentBuilderFactory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));

        EvaluateDecisionRequest request = unmarshaller
                .unmarshal(document.getDocumentElement(), EvaluateDecisionRequest.class)
                .getValue();

        DecisionService decisionService = Mockito.mock(DecisionService.class);
        Mockito.when(decisionService.evaluate(Mockito.any())).thenReturn(minimalDecisionResponse());

        DecisionEvaluationSoapEndpoint endpoint = new DecisionEvaluationSoapEndpoint(decisionService, new ObjectMapper());
        endpoint.evaluateDecision(request);

        ArgumentCaptor<DecisionDTO> captor = ArgumentCaptor.forClass(DecisionDTO.class);
        Mockito.verify(decisionService).evaluate(captor.capture());

        DecisionDTO dto = captor.getValue();
        assertEquals("discount-decision", dto.getDecisionDefinitionId());

        // Verify nested structure is preserved
        @SuppressWarnings("unchecked")
        Map<String, Object> orderMap = (Map<String, Object>) dto.getVariables().get("order");
        assertNotNull(orderMap);
        assertEquals("VIP", orderMap.get("customerType"));

        // Verify numeric type conversion for total
        Object totalObj = orderMap.get("total");
        assertNotNull(totalObj);
        assertTrue(totalObj instanceof Number, "total should be a Number, but got: " + totalObj.getClass().getSimpleName());
        assertEquals(1000.0, ((Number) totalObj).doubleValue());

        // Verify items array
        Object itemsObj = orderMap.get("items");
        assertNotNull(itemsObj);
        assertTrue(itemsObj instanceof java.util.List);
        @SuppressWarnings("unchecked")
        java.util.List<Object> items = (java.util.List<Object>) itemsObj;
        assertEquals(2, items.size());

        @SuppressWarnings("unchecked")
        Map<String, Object> firstItem = (Map<String, Object>) items.getFirst();
        assertEquals("ELECTRONICS", firstItem.get("category"));

        // Verify numeric type conversion for quantity
        Object quantityObj = firstItem.get("quantity");
        assertNotNull(quantityObj);
        assertTrue(quantityObj instanceof Number, "quantity should be a Number, but got: " + quantityObj.getClass().getSimpleName());
        assertEquals(1L, ((Number) quantityObj).longValue());
    }

    private EvaluateDecisionRequest unmarshallRequest() throws Exception {
        String xml = """
                <dec:evaluateDecisionRequest xmlns:dec="http://camunda.org/consulting/decision-evaluation">
                  <dec:decisionDefinitionId>nt-decision</dec:decisionDefinitionId>
                  <dec:variables>
                    <dec:entry>
                      <dec:key>team</dec:key>
                      <dec:value>East Regional</dec:value>
                    </dec:entry>
                    <dec:entry>
                      <dec:key>state</dec:key>
                      <dec:value>Alabama</dec:value>
                    </dec:entry>
                  </dec:variables>
                </dec:evaluateDecisionRequest>
                """;

        JAXBContext context = JAXBContext.newInstance(EvaluateDecisionRequest.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        var document = documentBuilderFactory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));

        return unmarshaller
                .unmarshal(document.getDocumentElement(), EvaluateDecisionRequest.class)
                .getValue();
    }

    private String valueAsText(Object value) {
        if (value instanceof Node node) {
            return node.getTextContent();
        }
        return String.valueOf(value);
    }

    private static EvaluateDecisionResponse minimalDecisionResponse() {
        return new EvaluateDecisionResponse() {
            @Override public String getDecisionId()             { return "test-decision"; }
            @Override public String getDecisionOutput()         { return "0.15"; }
            @Override public String getDecisionName()           { return null; }
            @Override public String getDecisionRequirementsId() { return null; }
            @Override public String getFailedDecisionId()       { return ""; }
            @Override public String getFailureMessage()         { return ""; }
            @Override public String getTenantId()               { return "<default>"; }
            @Override public int    getDecisionVersion()        { return 0; }
            @Override public long   getDecisionKey()            { return 0; }
            @Override public long   getDecisionRequirementsKey(){ return 0; }
            @Override public long   getDecisionInstanceKey()    { return 0; }
            @Override public long   getDecisionEvaluationKey()  { return 0; }
            @Override public List<EvaluatedDecision> getEvaluatedDecisions() { return List.of(); }
        };
    }
}


