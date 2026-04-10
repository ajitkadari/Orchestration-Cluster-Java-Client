package org.camunda.consulting.soap;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.camunda.consulting.service.DecisionService;
import org.camunda.consulting.dto.DecisionDTO;
import org.camunda.consulting.soap.model.EvaluateDecisionRequest;
import org.camunda.consulting.soap.model.EvaluateDecisionResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.HashMap;
import java.util.Map;

@Endpoint
public class DecisionEvaluationSoapEndpoint {

    private static final String NAMESPACE_URI = "http://camunda.org/consulting/decision-evaluation";

    private final DecisionService decisionEvaluationService;
    private final ObjectMapper objectMapper;

    public DecisionEvaluationSoapEndpoint(DecisionService decisionEvaluationService, ObjectMapper objectMapper) {
        this.decisionEvaluationService = decisionEvaluationService;
        this.objectMapper = objectMapper;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "evaluateDecisionRequest")
    @ResponsePayload
    public EvaluateDecisionResponse evaluateDecision(@RequestPayload EvaluateDecisionRequest request) {
        EvaluateDecisionResponse response = new EvaluateDecisionResponse();

        DecisionDTO dto = new DecisionDTO();
        dto.setDecisionDefinitionId(request.getDecisionDefinitionId());
        dto.setDecisionDefinitionKey(request.getDecisionDefinitionKey());

        if (request.getVariables() != null) {
            Map<String, Object> variableMap = new HashMap<>();
            request.getVariables().getEntries()
                    .forEach(entry -> variableMap.put(entry.getKey(), entry.getValue()));
            dto.setVariables(variableMap);
        }

        try {
            Object result = decisionEvaluationService.evaluate(dto);
            response.setSuccess(true);
            response.setResult(asJson(result));
        } catch (Exception e) {
            response.setSuccess(false);
            response.setErrorMessage(e.getMessage());
        }

        return response;
    }

    private String asJson(Object result) throws JacksonException {
        return objectMapper.writeValueAsString(result);
    }
}
