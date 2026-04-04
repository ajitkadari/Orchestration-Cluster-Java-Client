package org.camunda.consulting;

import io.camunda.client.CamundaClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DecisionEvaluationService {

    private final CamundaClient camundaClient;

    public DecisionEvaluationService(CamundaClient camundaClient) {
        this.camundaClient = camundaClient;
    }

    public Object evaluate(DecisionDTO request) {
        String decisionId = request.getDecisionDefinitionId();
        String decisionKey = request.getDecisionDefinitionKey();

        if ((decisionId == null || decisionId.isBlank()) && (decisionKey == null || decisionKey.isBlank())) {
            throw new IllegalArgumentException("Either decisionDefinitionId or decisionDefinitionKey must be provided.");
        }

        Map<String, Object> variables = new HashMap<>();
        if (request.getVariables() != null) {
            variables.putAll(request.getVariables());
        }

        var command = camundaClient.newEvaluateDecisionCommand();
        var commandStep2 = (decisionId != null && !decisionId.isBlank())
                ? command.decisionId(decisionId)
                : decisionKey.chars().allMatch(Character::isDigit)
                ? command.decisionKey(Long.parseLong(decisionKey))
                : command.decisionId(decisionKey);

        return commandStep2
                .variables(variables)
                .send()
                .join();
    }

    public Object searchAll() {
        return camundaClient
                .newDecisionDefinitionSearchRequest()
                .send()
                .join()
                .items();
    }

    public Object searchByName(String name) {
        return camundaClient
                .newDecisionDefinitionSearchRequest()
                .filter(f -> f.name(name))
                .send()
                .join()
                .items();
    }

    public Object searchById(String id) {
        return camundaClient
                .newDecisionDefinitionSearchRequest()
                .filter(f -> f.decisionDefinitionId(id))
                .send()
                .join()
                .items();
    }
}
