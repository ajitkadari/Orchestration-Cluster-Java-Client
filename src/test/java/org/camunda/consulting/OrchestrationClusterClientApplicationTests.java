package org.camunda.consulting;

import io.camunda.client.CamundaClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrchestrationClusterClientApplicationTests {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DecisionDefinitionController controller = new DecisionDefinitionController();
        ReflectionTestUtils.setField(controller, "camundaClient", Mockito.mock(CamundaClient.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void healthEndpointReturnsStatusOk() throws Exception {
        mockMvc.perform(get("/v2/decision-definitions/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("DecisionDefinitionController is up"));
    }

    @Test
    void evaluateDecisionReturnsBadRequestWhenNoDecisionIdentifierProvided() throws Exception {
        mockMvc.perform(post("/v2/decision-definitions/evaluation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Either decisionDefinitionId or decisionDefinitionKey must be provided."));
    }


}
