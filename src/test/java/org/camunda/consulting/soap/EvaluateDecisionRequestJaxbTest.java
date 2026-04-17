package org.camunda.consulting.soap;

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

import tools.jackson.databind.ObjectMapper;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        Mockito.when(decisionService.evaluate(Mockito.any())).thenReturn(Map.of("decision", "approved"));

        DecisionEvaluationSoapEndpoint endpoint = new DecisionEvaluationSoapEndpoint(decisionService, new ObjectMapper());

        endpoint.evaluateDecision(unmarshallRequest());

        ArgumentCaptor<DecisionDTO> captor = ArgumentCaptor.forClass(DecisionDTO.class);
        Mockito.verify(decisionService).evaluate(captor.capture());

        DecisionDTO dto = captor.getValue();
        assertEquals("nt-decision", dto.getDecisionDefinitionId());
        assertEquals("East Regional", dto.getVariables().get("team"));
        assertEquals("Alabama", dto.getVariables().get("state"));
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
}


