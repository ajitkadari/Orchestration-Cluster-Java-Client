package org.camunda.consulting.soap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.consulting.DecisionEvaluationService;
import org.camunda.consulting.DecisionVariables;
import org.camunda.consulting.NTdecisionDTO;
import org.camunda.consulting.soap.model.EvaluateDecisionRequest;
import org.camunda.consulting.soap.model.EvaluateDecisionResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class DecisionEvaluationSoapEndpoint {

    private static final String NAMESPACE_URI = "http://camunda.org/consulting/decision-evaluation";

    private final DecisionEvaluationService decisionEvaluationService;
    private final ObjectMapper objectMapper;

    public DecisionEvaluationSoapEndpoint(DecisionEvaluationService decisionEvaluationService, ObjectMapper objectMapper) {
        this.decisionEvaluationService = decisionEvaluationService;
        this.objectMapper = objectMapper;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "evaluateDecisionRequest")
    @ResponsePayload
    public EvaluateDecisionResponse evaluateDecision(@RequestPayload EvaluateDecisionRequest request) {
        EvaluateDecisionResponse response = new EvaluateDecisionResponse();

        NTdecisionDTO dto = new NTdecisionDTO();
        dto.setDecisionDefinitionId(request.getDecisionDefinitionId());
        dto.setDecisionDefinitionKey(request.getDecisionDefinitionKey());

        if (request.getDecisionVariables() != null) {
            DecisionVariables variables = new DecisionVariables();
            variables.setTeam(request.getDecisionVariables().getTeam());
            variables.setState(request.getDecisionVariables().getState());
            dto.setDecisionVariables(variables);
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

    private String asJson(Object result) throws JsonProcessingException {
        return objectMapper.writeValueAsString(result);
    }
}

