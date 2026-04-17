package org.camunda.consulting.soap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionEvaluationSoapEndpoint.class);

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

        LOGGER.info("Received decision evaluation request for definitionId: {}, definitionKey: {}",
                request.getDecisionDefinitionId(), request.getDecisionDefinitionKey());

        DecisionDTO dto = new DecisionDTO();
        dto.setDecisionDefinitionId(request.getDecisionDefinitionId());
        dto.setDecisionDefinitionKey(request.getDecisionDefinitionKey());

        if (request.getVariables() != null) {
            Map<String, Object> variableMap = new HashMap<>();
            request.getVariables().getEntries()
                    .forEach(entry -> variableMap.put(entry.getKey(), SoapUtils.normalizeSoapValue(entry.getValue())));
            dto.setVariables(variableMap);
        }

        try {
            Object result = decisionEvaluationService.evaluate(dto);
            response.setSuccess(true);
            response.setResult(SoapUtils.toSoapResult(objectMapper, result));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("SOAP success envelope preview: {}", SoapUtils.toSoapSuccess(objectMapper, result));
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setErrorMessage(e.getMessage() != null ? e.getMessage() : "Unexpected SOAP processing error.");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("SOAP error envelope preview: {}", SoapUtils.toSoapError(e.getMessage()));
            }
        }

        return response;
    }
}
