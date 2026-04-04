package org.camunda.consulting.rest;

import org.camunda.consulting.DecisionEvaluationService;
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

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for DecisionDefinitionController REST endpoints.
 * Covers search and evaluate endpoints including success and error scenarios.
 */
@ExtendWith(MockitoExtension.class)
class DecisionDefinitionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DecisionEvaluationService decisionEvaluationService;

    @InjectMocks
    private DecisionDefinitionController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void searchAllDecisionDefinitions_ReturnsOkWithResults() throws Exception {
        List<String> mockResults = Arrays.asList("decision-1", "decision-2");
        Mockito.when(decisionEvaluationService.searchAll()).thenReturn(mockResults);

        mockMvc.perform(get("/decision-definitions/search"))
                .andExpect(status().isOk());

        Mockito.verify(decisionEvaluationService, Mockito.times(1)).searchAll();
    }

    @Test
    void searchDecisionDefinitionsByName_ReturnsOkWithResults() throws Exception {
        String name = "approvalDecision";
        List<String> mockResults = Arrays.asList("approval-1");
        Mockito.when(decisionEvaluationService.searchByName(name)).thenReturn(mockResults);

        mockMvc.perform(get("/decision-definitions/search/by-name/{name}", name))
                .andExpect(status().isOk());

        Mockito.verify(decisionEvaluationService, Mockito.times(1)).searchByName(name);
    }

    @Test
    void searchDecisionDefinitionsById_ReturnsOkWithResults() throws Exception {
        String id = "decision-id-123";
        List<String> mockResults = Arrays.asList("decision-123");
        Mockito.when(decisionEvaluationService.searchById(id)).thenReturn(mockResults);

        mockMvc.perform(get("/decision-definitions/search/by-id/{id}", id))
                .andExpect(status().isOk());

        Mockito.verify(decisionEvaluationService, Mockito.times(1)).searchById(id);
    }

    @Test
    void searchAllDecisionDefinitions_HandleError() throws Exception {
        Mockito.when(decisionEvaluationService.searchAll())
                .thenThrow(new RuntimeException("Connection error"));

        mockMvc.perform(get("/decision-definitions/search"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Connection error")));
    }

    @Test
    void evaluateDecisionReturnsBadRequestWhenNoDecisionIdentifierProvided() throws Exception {
        Mockito.when(decisionEvaluationService.evaluate(Mockito.argThat(request ->
                        request.getDecisionDefinitionId() == null && request.getDecisionDefinitionKey() == null)))
                .thenThrow(new IllegalArgumentException("Either decisionDefinitionId or decisionDefinitionKey must be provided."));

        mockMvc.perform(post("/decision-definitions/evaluation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Either decisionDefinitionId or decisionDefinitionKey must be provided."));
    }

    @Test
    void evaluateDecisionReturnsOkWhenRequestIsValid() throws Exception {
        Mockito.when(decisionEvaluationService.evaluate(Mockito.any()))
                .thenReturn("{\"result\": \"approved\"}");

        mockMvc.perform(post("/decision-definitions/evaluation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decisionDefinitionId\": \"decision-123\", \"variables\": {\"team\": \"eng\", \"state\": \"active\"}}"))
                .andExpect(status().isOk());
    }

    @Test
    void evaluateDecisionReturnsErrorOnException() throws Exception {
        Mockito.when(decisionEvaluationService.evaluate(Mockito.any()))
                .thenThrow(new RuntimeException("Evaluation failed"));

        mockMvc.perform(post("/decision-definitions/evaluation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decisionDefinitionId\": \"decision-123\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Evaluation failed")));
    }
}

