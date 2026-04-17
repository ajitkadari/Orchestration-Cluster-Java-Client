package org.camunda.consulting.soap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.w3c.dom.Node;

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

        LOGGER.info("SOAP Request: {}", objectMapper.writeValueAsString(request));

        DecisionDTO dto = new DecisionDTO();
        dto.setDecisionDefinitionId(request.getDecisionDefinitionId());
        dto.setDecisionDefinitionKey(request.getDecisionDefinitionKey());

        if (request.getVariables() != null) {
            Map<String, Object> variableMap = new HashMap<>();
            request.getVariables().getEntries()
                    .forEach(entry -> variableMap.put(entry.getKey(), normalizeSoapValue(entry.getValue())));
            dto.setVariables(variableMap);
        }

        LOGGER.info("DTO request to decisionEvaluationService: {}", objectMapper.writeValueAsString(dto));

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

    private Object normalizeSoapValue(Object value) {
        if (value instanceof Node node) {
            // If it's a complex DOM element, convert it to a Map/List structure
            // to preserve hierarchy for nested objects like OrderDTO
            return convertDomNodeToObject(node);
        }
        return value;
    }

    private Object convertDomNodeToObject(Node node) {
        // Handle text nodes
        if (node.getNodeType() == Node.TEXT_NODE) {
            String text = node.getTextContent().trim();
            return text.isEmpty() ? null : parseValue(text);
        }

        // Handle element nodes
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Map<String, Object> map = new HashMap<>();
            var children = node.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);

                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    String nodeName = child.getLocalName();
                    Object nodeValue = convertDomNodeToObject(child);

                    // If the key already exists, convert to a list
                    if (map.containsKey(nodeName)) {
                        Object existing = map.get(nodeName);
                        if (existing instanceof java.util.List) {
                            @SuppressWarnings("unchecked")
                            java.util.List<Object> list = (java.util.List<Object>) existing;
                            list.add(nodeValue);
                        } else {
                            java.util.List<Object> list = new java.util.ArrayList<>();
                            list.add(existing);
                            list.add(nodeValue);
                            map.put(nodeName, list);
                        }
                    } else {
                        map.put(nodeName, nodeValue);
                    }
                }
            }

            return map.isEmpty() ? parseValue(node.getTextContent().trim()) : map;
        }

        return null;
    }

    private Object parseValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        // Try to parse as integer
        try {
            if (!value.contains(".")) {
                return Long.parseLong(value);
            }
        } catch (NumberFormatException e) {
            // Not an integer, try double
        }

        // Try to parse as double
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            // Not a number, return as string
        }

        // Try to parse as boolean
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }

        // Return as string
        return value;
    }
}
