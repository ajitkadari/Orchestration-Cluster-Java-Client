package org.camunda.consulting.soap;

import io.camunda.client.api.response.EvaluateDecisionResponse;
import io.camunda.client.api.response.EvaluatedDecision;
import tools.jackson.databind.ObjectMapper;
import org.camunda.consulting.service.DecisionService;
import org.camunda.consulting.soap.model.EvaluateDecisionRequest;
import org.camunda.consulting.soap.model.SoapDecisionVariables;
import org.camunda.consulting.soap.model.SoapVariableEntry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecisionEvaluationSoapEndpointTest {

    @Test
    void evaluateDecisionReturnsSuccessPayloadWhenServiceSucceeds() {
        DecisionService service = Mockito.mock(DecisionService.class);
        Mockito.when(service.evaluate(Mockito.any())).thenReturn(minimalDecisionResponse("discount-decision", "0.15"));

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
        assertNotNull(response.getResult());
        assertFalse(response.getResult().getAny().isEmpty());
        assertTrue(response.getResult().getAny().stream()
                .anyMatch(el -> "decisionId".equals(el.getTagName()) && "discount-decision".equals(el.getTextContent())),
                "Expected <decisionId>discount-decision</decisionId> element in SOAP result");
        assertTrue(response.getResult().getAny().stream()
                .anyMatch(el -> "decisionOutput".equals(el.getTagName()) && "0.15".equals(el.getTextContent())),
                "Expected <decisionOutput>0.15</decisionOutput> element in SOAP result");
    }

    // -----------------------------------------------------------------------
    // Test helper
    // -----------------------------------------------------------------------

    private static EvaluateDecisionResponse minimalDecisionResponse(String decisionId, String decisionOutput) {
        return new EvaluateDecisionResponse() {
            @Override public String getDecisionId()             { return decisionId; }
            @Override public String getDecisionOutput()         { return decisionOutput; }
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

    @Test
    void evaluateDecisionReturnsErrorPayloadWhenServiceFails() {
        DecisionService service = Mockito.mock(DecisionService.class);
        Mockito.when(service.evaluate(Mockito.any()))
                .thenThrow(new IllegalArgumentException("Either decisionDefinitionId or decisionDefinitionKey must be provided."));

        DecisionEvaluationSoapEndpoint endpoint = new DecisionEvaluationSoapEndpoint(service, new ObjectMapper());

        EvaluateDecisionRequest request = new EvaluateDecisionRequest();
        var response = endpoint.evaluateDecision(request);

        assertFalse(response.isSuccess());
        assertEquals("Either decisionDefinitionId or decisionDefinitionKey must be provided.", response.getErrorMessage());
    }
}
