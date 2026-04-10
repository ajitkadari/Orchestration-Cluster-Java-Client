package org.camunda.consulting.rest;

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
        Mockito.when(businessProcessService.createOrderProcessInstance(Mockito.any(OrderProcessDTO.class)))
                .thenReturn("""
                        {
                          "processDefinitionId": "my-process-model-1",
                          "processDefinitionVersion": 3,
                          "tenantId": "<default>",
                          "variables": {},
                          "processDefinitionKey": "2251799813686749",
                          "processInstanceKey": "2251799813690746",
                          "tags": [
                            "high-touch",
                            "remediation"
                          ]
                        }
                        """);

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
