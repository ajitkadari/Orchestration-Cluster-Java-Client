package org.camunda.consulting.soap;

import tools.jackson.databind.ObjectMapper;
import org.camunda.consulting.DecisionEvaluationService;
import org.camunda.consulting.soap.model.EvaluateDecisionRequest;
import org.camunda.consulting.soap.model.SoapDecisionVariables;
import org.camunda.consulting.soap.model.SoapVariableEntry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecisionEvaluationSoapEndpointTest {

    @Test
    void evaluateDecisionReturnsSuccessPayloadWhenServiceSucceeds() {
        DecisionEvaluationService service = Mockito.mock(DecisionEvaluationService.class);
        Mockito.when(service.evaluate(Mockito.any())).thenReturn(Map.of("decision", "approved"));

        DecisionEvaluationSoapEndpoint endpoint = new DecisionEvaluationSoapEndpoint(service, new ObjectMapper());

        EvaluateDecisionRequest request = new EvaluateDecisionRequest();
        request.setDecisionDefinitionId("decision-id");
        SoapDecisionVariables variables = new SoapDecisionVariables();
        variables.setEntries(List.of(
                new SoapVariableEntry("team", "East Regional"),
                new SoapVariableEntry("state", "Alabama")
        ));
        request.setVariables(variables);

        var response = endpoint.evaluateDecision(request);

        assertTrue(response.isSuccess());
        assertEquals("{\"decision\":\"approved\"}", response.getResult());
    }

    @Test
    void evaluateDecisionReturnsErrorPayloadWhenServiceFails() {
        DecisionEvaluationService service = Mockito.mock(DecisionEvaluationService.class);
        Mockito.when(service.evaluate(Mockito.any()))
                .thenThrow(new IllegalArgumentException("Either decisionDefinitionId or decisionDefinitionKey must be provided."));

        DecisionEvaluationSoapEndpoint endpoint = new DecisionEvaluationSoapEndpoint(service, new ObjectMapper());

        EvaluateDecisionRequest request = new EvaluateDecisionRequest();
        var response = endpoint.evaluateDecision(request);

        assertFalse(response.isSuccess());
        assertEquals("Either decisionDefinitionId or decisionDefinitionKey must be provided.", response.getErrorMessage());
    }
}
