package org.camunda.consulting.rest;

import java.util.List;

import org.camunda.consulting.service.DecisionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import io.camunda.client.api.response.EvaluateDecisionResponse;
import io.camunda.client.api.response.EvaluatedDecision;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DecisionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DecisionService decisionEvaluationService;

    @InjectMocks
    private DecisionController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void topologyReturnsOk() throws Exception {
        Mockito.when(decisionEvaluationService.topology()).thenReturn("{\"clusterSize\": 3}");

        mockMvc.perform(get("/api/camunda/topology"))
                .andExpect(status().isOk());

        Mockito.verify(decisionEvaluationService, Mockito.times(1)).topology();
    }

    @Test
    void getDecisionDefinitionReturnsOk() throws Exception {
        long decisionDefinitionKey = 2251799813326547L;
        Mockito.when(decisionEvaluationService.getDecisionDefinition(decisionDefinitionKey))
                .thenReturn("{\"decisionDefinitionKey\":2251799813326547}");

        mockMvc.perform(get("/api/camunda/decision-definitions/{decisionDefinitionKey}", decisionDefinitionKey))
                .andExpect(status().isOk());

        Mockito.verify(decisionEvaluationService, Mockito.times(1)).getDecisionDefinition(decisionDefinitionKey);
    }

    @Test
    void getDecisionDefinitionXmlReturnsOk() throws Exception {
        long decisionDefinitionKey = 2251799813326547L;
        Mockito.when(decisionEvaluationService.getDecisionDefinitionXml(decisionDefinitionKey))
                .thenReturn("{\"xml\":\"<definitions/>\"}");

        mockMvc.perform(get("/api/camunda/decision-definitions/{decisionDefinitionKey}/xml", decisionDefinitionKey))
                .andExpect(status().isOk());

        Mockito.verify(decisionEvaluationService, Mockito.times(1)).getDecisionDefinitionXml(decisionDefinitionKey);
    }

    @Test
    void searchDecisionDefinitionsReturnsOk() throws Exception {
        Mockito.when(decisionEvaluationService.searchDecisionDefinitions(Mockito.anyMap()))
                .thenReturn("[]");

        mockMvc.perform(post("/api/camunda/decision-definitions/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filter\":{\"name\":\"approvalDecision\"}}"))
                .andExpect(status().isOk());

        Mockito.verify(decisionEvaluationService, Mockito.times(1))
                .searchDecisionDefinitions(Mockito.anyMap());
    }

    @Test
    void searchDecisionDefinitionsHandlesError() throws Exception {
        Mockito.when(decisionEvaluationService.searchDecisionDefinitions(Mockito.anyMap()))
                .thenThrow(new RuntimeException("Connection error"));

        mockMvc.perform(post("/api/camunda/decision-definitions/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Connection error")));
    }

    @Test
    void evaluateDecisionReturnsBadRequestWhenNoDecisionIdentifierProvided() throws Exception {
        Mockito.when(decisionEvaluationService.evaluate(Mockito.argThat(request ->
                        request.getDecisionDefinitionId() == null && request.getDecisionDefinitionKey() == null)))
                .thenThrow(new IllegalArgumentException("Either decisionDefinitionId or decisionDefinitionKey must be provided."));

        mockMvc.perform(post("/api/camunda/decision-definitions/evaluation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Either decisionDefinitionId or decisionDefinitionKey must be provided."));
    }

    @Test
    void evaluateDecisionReturnsOkWhenRequestIsValid() throws Exception {
        Mockito.when(decisionEvaluationService.evaluate(Mockito.any()))
                .thenReturn(minimalDecisionResponse());

        mockMvc.perform(post("/api/camunda/decision-definitions/evaluation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decisionDefinitionId\": \"decision-123\", \"variables\": {\"inputA\": \"x\"}}"))
                .andExpect(status().isOk());
    }

    @Test
    void evaluateDecisionReturnsErrorOnException() throws Exception {
        Mockito.when(decisionEvaluationService.evaluate(Mockito.any()))
                .thenThrow(new RuntimeException("Evaluation failed"));

        mockMvc.perform(post("/api/camunda/decision-definitions/evaluation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decisionDefinitionId\": \"decision-123\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Evaluation failed")));
    }

    private static EvaluateDecisionResponse minimalDecisionResponse() {
        return new EvaluateDecisionResponse() {
            @Override public String getDecisionId()             { return "test-decision"; }
            @Override public String getDecisionOutput()         { return "approved"; }
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
