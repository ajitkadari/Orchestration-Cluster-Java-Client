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

    public ProcessInstanceResult createOrderProcessInstance(OrderProcessDTO requestBody) {
        if (requestBody == null) {
            throw new IllegalArgumentException("Request body must contain processDefinitionId or processDefinitionKey.");
        }

        String processDefinitionId = requestBody.getProcessDefinitionId();
        String processDefinitionKey = requestBody.getProcessDefinitionKey();

        if ((processDefinitionId == null || processDefinitionId.isBlank())
                && (processDefinitionKey == null || processDefinitionKey.isBlank())) {
            throw new IllegalArgumentException("Either processDefinitionId or processDefinitionKey must be provided.");
        }

        Map<String, Object> variables = new HashMap<>();
        if (requestBody.getVariables() != null) {
            requestBody.getVariables().forEach((k, v) -> variables.put(String.valueOf(k), v));
        }

        var command = camundaClient.newCreateInstanceCommand();

        var commandStep3 = (processDefinitionKey != null && !processDefinitionKey.isBlank())
                ? command.processDefinitionKey(Long.parseLong(processDefinitionKey))
                : (requestBody.getVersion() != null
                        ? command.bpmnProcessId(processDefinitionId).version(requestBody.getVersion())
                        : command.bpmnProcessId(processDefinitionId).latestVersion());

        if (!variables.isEmpty()) {
            commandStep3 = commandStep3.variables(variables);
        }

        if (requestBody.getTenantId() != null && !requestBody.getTenantId().isBlank()) {
            commandStep3 = commandStep3.tenantId(requestBody.getTenantId());
        }

        return commandStep3.withResult().send().join();
    }
}
