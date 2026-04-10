package org.camunda.consulting.rest;

import io.camunda.client.api.response.ProcessInstanceResult;
import org.camunda.consulting.service.BusinessProcessService;
import org.camunda.consulting.dto.OrderProcessDTO;
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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BusinessProcessControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BusinessProcessService businessProcessService;

    @InjectMocks
    private BusinessProcessController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createOrderProcessInstanceReturnsOkWhenRequestIsValid() throws Exception {
        ProcessInstanceResult result = sampleProcessInstanceResult();
        Mockito.when(businessProcessService.createOrderProcessInstance(Mockito.any(OrderProcessDTO.class)))
                .thenReturn(result);

        mockMvc.perform(post("/api/camunda/process-instances/order-process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "processDefinitionId": "order-process",
                                  "processDefinitionKey": null,
                                  "version": 1,
                                  "tenantId": "customer-service",
                                  "variables": {
                                    "order": {
                                      "customerType": "VIP",
                                      "total": 250.50,
                                      "items": [
                                        {
                                          "category": "ELECTRONICS",
                                          "quantity": 1
                                        },
                                        {
                                          "category": "ELECTRONICS",
                                          "quantity": 1
                                        }
                                      ]
                                    }
                                  }
                                }
                                """))
                .andExpect(status().isOk());

        Mockito.verify(businessProcessService, Mockito.times(1))
                .createOrderProcessInstance(Mockito.any(OrderProcessDTO.class));
    }

    private ProcessInstanceResult sampleProcessInstanceResult() {
        return new ProcessInstanceResult() {
            @Override
            public long getProcessDefinitionKey() { return 2251799813686749L; }

            @Override
            public String getBpmnProcessId() { return "order-process"; }

            @Override
            public int getVersion() { return 1; }

            @Override
            public long getProcessInstanceKey() { return 2251799813690746L; }

            @Override
            public String getVariables() { return "{}"; }

            @Override
            public Map<String, Object> getVariablesAsMap() { return Collections.emptyMap(); }

            @Override
            public <T> T getVariablesAsType(Class<T> ignored) { return null; }

            @Override
            public Object getVariable(String ignored) { return null; }

            @Override
            public String getTenantId() { return "<default>"; }

            @Override
            public Set<String> getTags() { return Collections.emptySet(); }

        };
    }

    @Test
    void createOrderProcessInstanceReturnsBadRequestWhenInputIsInvalid() throws Exception {
        Mockito.when(businessProcessService.createOrderProcessInstance(Mockito.any(OrderProcessDTO.class)))
                .thenThrow(new IllegalArgumentException("Either processDefinitionId or processDefinitionKey must be provided."));

        mockMvc.perform(post("/api/camunda/process-instances/order-process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Either processDefinitionId or processDefinitionKey must be provided."));
    }

    @Test
    void createOrderProcessInstanceReturnsInternalServerErrorWhenServiceFails() throws Exception {
        Mockito.when(businessProcessService.createOrderProcessInstance(Mockito.any(OrderProcessDTO.class)))
                .thenThrow(new RuntimeException("Camunda unavailable"));

        mockMvc.perform(post("/api/camunda/process-instances/order-process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "processDefinitionId": null,
                                  "processDefinitionKey": "2251799813685249",
                                  "version": null,
                                  "tenantId": null,
                                  "variables": {
                                    "order": {
                                      "customerType": "REGULAR",
                                      "total": 125.75,
                                      "items": [
                                        {
                                          "category": "ELECTRONICS",
                                          "quantity": 3
                                        }
                                      ]
                                    }
                                  }
                                }
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error creating process instance: Camunda unavailable")));
    }
}
