package org.camunda.consulting.service;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.EvaluateDecisionResponse;
import org.camunda.consulting.dto.DecisionDTO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DecisionService {

    private final CamundaClient camundaClient;

    public DecisionService(CamundaClient camundaClient) {
        this.camundaClient = camundaClient;
    }

    /**
     * Evaluates a DMN decision definition.
     *
     * @return the {@link EvaluateDecisionResponse} from Camunda.
     */
    public EvaluateDecisionResponse evaluate(DecisionDTO decisionDTO) {
        String decisionDefinitionId = decisionDTO.getDecisionDefinitionId();
        String decisionDefinitionKey = decisionDTO.getDecisionDefinitionKey();

        if ((decisionDefinitionId == null || decisionDefinitionId.isBlank()) && (decisionDefinitionKey == null || decisionDefinitionKey.isBlank())) {
            throw new IllegalArgumentException("Either decisionDefinitionId or decisionDefinitionKey must be provided.");
        }

        Map<String, Object> variables = new HashMap<>();
        if (decisionDTO.getVariables() != null) {
            variables.putAll(decisionDTO.getVariables());
        }

        var command = camundaClient.newEvaluateDecisionCommand();
        var commandStep2 = (decisionDefinitionId != null && !decisionDefinitionId.isBlank())
                ? command.decisionId(decisionDefinitionId)
                : decisionDefinitionKey.chars().allMatch(Character::isDigit)
                ? command.decisionKey(Long.parseLong(decisionDefinitionKey))
                : command.decisionId(decisionDefinitionKey);

        return commandStep2
                .variables(variables)
                .send()
                .join();
    }

    public Object topology() {
        return camundaClient.newTopologyRequest().send().join();
    }

    public Object getDecisionDefinition(long decisionDefinitionKey) {
        return camundaClient.newDecisionDefinitionGetRequest(decisionDefinitionKey).send().join();
    }

    public Object getDecisionDefinitionXml(long decisionDefinitionKey) {
        return camundaClient.newDecisionDefinitionGetXmlRequest(decisionDefinitionKey).send().join();
    }

    public Object searchDecisionDefinitions(Map<String, Object> requestBody) {
        var searchRequest = camundaClient.newDecisionDefinitionSearchRequest();

        if (requestBody == null || requestBody.isEmpty()) {
            return searchRequest.send().join().items();
        }

        Object filterValue = requestBody.get("filter");
        if (filterValue instanceof Map<?, ?> filter) {
            searchRequest = searchRequest.filter(f -> {
                Object decisionDefinitionId = filter.get("decisionDefinitionId");
                if (decisionDefinitionId != null) {
                    f.decisionDefinitionId(String.valueOf(decisionDefinitionId));
                }

                Object name = filter.get("name");
                if (name != null) {
                    f.name(String.valueOf(name));
                }

                Object decisionDefinitionKey = filter.get("decisionDefinitionKey");
                if (decisionDefinitionKey != null) {
                    f.decisionDefinitionKey(Long.parseLong(String.valueOf(decisionDefinitionKey)));
                }
            });
        }

        Object pageValue = requestBody.get("page");
        if (pageValue instanceof Map<?, ?> page) {
            searchRequest = searchRequest.page(p -> {
                Object from = page.get("from");
                if (from != null) {
                    p.from(Integer.parseInt(String.valueOf(from)));
                }

                Object limit = page.get("limit");
                if (limit != null) {
                    p.limit(Integer.parseInt(String.valueOf(limit)));
                }
            });
        }

        return searchRequest.send().join().items();
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
