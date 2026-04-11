package org.camunda.consulting.service;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceResult;
import org.camunda.consulting.dto.OrderProcessDTO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class BusinessProcessService {

    private final CamundaClient camundaClient;

    public BusinessProcessService(CamundaClient camundaClient) {
        this.camundaClient = camundaClient;
    }

    public ProcessInstanceResult createOrderProcessInstance(OrderProcessDTO orderProcessDTO) {
        if (orderProcessDTO == null) {
            throw new IllegalArgumentException("Request body must contain processDefinitionId or processDefinitionKey.");
        }

        String processDefinitionId = orderProcessDTO.getProcessDefinitionId();
        String processDefinitionKey = orderProcessDTO.getProcessDefinitionKey();

        if ((processDefinitionId == null || processDefinitionId.isBlank())
                && (processDefinitionKey == null || processDefinitionKey.isBlank())) {
            throw new IllegalArgumentException("Either processDefinitionId or processDefinitionKey must be provided.");
        }

        Map<String, Object> variables = new HashMap<>();
        if (orderProcessDTO.getVariables() != null) {
            orderProcessDTO.getVariables().forEach((k, v) -> variables.put(String.valueOf(k), v));
        }

        var command = camundaClient.newCreateInstanceCommand();

        var commandStep3 = (processDefinitionKey != null && !processDefinitionKey.isBlank())
                ? command.processDefinitionKey(Long.parseLong(processDefinitionKey))
                : (orderProcessDTO.getVersion() != null
                        ? command.bpmnProcessId(processDefinitionId).version(orderProcessDTO.getVersion())
                        : command.bpmnProcessId(processDefinitionId).latestVersion());

        if (!variables.isEmpty()) {
            commandStep3 = commandStep3.variables(variables);
        }

        if (orderProcessDTO.getTenantId() != null && !orderProcessDTO.getTenantId().isBlank()) {
            commandStep3 = commandStep3.tenantId(orderProcessDTO.getTenantId());
        }

        return commandStep3.withResult().send().join();
    }
}
