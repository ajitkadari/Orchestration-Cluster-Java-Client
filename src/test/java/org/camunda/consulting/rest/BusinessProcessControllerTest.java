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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
                                  "variables": {
                                    "order": {
                                      "customerType": "VIP",
                                      "total": 1000.00,
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bpmnProcessId").value("order-process"))
                .andExpect(jsonPath("$.processDefinitionKey").isNumber())
                .andExpect(jsonPath("$.processInstanceKey").isNumber())
                .andExpect(jsonPath("$.tenantId").isString())
                .andExpect(jsonPath("$.version").isNumber())
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.variables").value("{\"discount\":0.15,\"order\":{\"customerType\":\"VIP\",\"total\":1000.0,\"items\":[{\"category\":\"ELECTRONICS\",\"quantity\":1},{\"category\":\"ELECTRONICS\",\"quantity\":1}]}}"))
                .andExpect(jsonPath("$.variablesAsMap.discount").value(0.15))
                .andExpect(jsonPath("$.variablesAsMap.order.customerType").value("VIP"))
                .andExpect(jsonPath("$.variablesAsMap.order.total").value(1000))
                .andExpect(jsonPath("$.variablesAsMap.order.items[0].category").value("ELECTRONICS"))
                .andExpect(jsonPath("$.variablesAsMap.order.items[0].quantity").value(1))
                .andExpect(jsonPath("$.variablesAsMap.order.items[1].category").value("ELECTRONICS"))
                .andExpect(jsonPath("$.variablesAsMap.order.items[1].quantity").value(1));

        Mockito.verify(businessProcessService, Mockito.times(1))
                .createOrderProcessInstance(Mockito.any(OrderProcessDTO.class));
    }

    private ProcessInstanceResult sampleProcessInstanceResult() {
        Map<String, Object> firstItem = new LinkedHashMap<>();
        firstItem.put("category", "ELECTRONICS");
        firstItem.put("quantity", 1);

        Map<String, Object> secondItem = new LinkedHashMap<>();
        secondItem.put("category", "ELECTRONICS");
        secondItem.put("quantity", 1);

        Map<String, Object> order = new LinkedHashMap<>();
        order.put("customerType", "VIP");
        order.put("total", 1000);
        order.put("items", List.of(firstItem, secondItem));

        Map<String, Object> variablesAsMap = new LinkedHashMap<>();
        variablesAsMap.put("discount", 0.15);
        variablesAsMap.put("order", order);

        return new ProcessInstanceResult() {
            @Override
            public long getProcessDefinitionKey() { return 2251799816070543L; }

            @Override
            public String getBpmnProcessId() { return "order-process"; }

            @Override
            public int getVersion() { return 8; }

            @Override
            public long getProcessInstanceKey() { return 2251799816077136L; }

            @Override
            public String getVariables() {
                return "{\"discount\":0.15,\"order\":{\"customerType\":\"VIP\",\"total\":1000.0,\"items\":[{\"category\":\"ELECTRONICS\",\"quantity\":1},{\"category\":\"ELECTRONICS\",\"quantity\":1}]}}";
            }

            @Override
            public Map<String, Object> getVariablesAsMap() { return variablesAsMap; }

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
                                  "processDefinitionKey": "2251799813685249",
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
