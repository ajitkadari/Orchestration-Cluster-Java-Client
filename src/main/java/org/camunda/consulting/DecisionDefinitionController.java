package org.camunda.consulting;

import io.camunda.client.CamundaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/decision-definitions", "/v2/decision-definitions"})
public class DecisionDefinitionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionDefinitionController.class);

    @Autowired
    private CamundaClient camundaClient;

    /**
     * Example: Search all decision definitions
     * GET /decision-definitions/search
     * 
     * API Usage:
     * camundaClient.newDecisionDefinitionSearchRequest()
     *     .send()
     *     .join()
     *     .items()
     */
    @GetMapping("/search")
    public Object searchAllDecisionDefinitions() {
        try {
            // Search all decision definitions using the Camunda REST client API
            var result = camundaClient
                    .newDecisionDefinitionSearchRequest()
                    .send()
                    .join();

            return result.items();
        } catch (Exception e) {
            return "Error searching decision definitions: " + e.getMessage();
        }
    }

    /**
     * Example: Search decision definition by name
     * GET /decision-definitions/search/by-name/myDecisionName
     * 
     * API Usage:
     * camundaClient.newDecisionDefinitionSearchRequest()
     *     .filter(f -> f.name("myDecisionName"))
     *     .send()
     *     .join()
     *     .items()
     */
    @GetMapping("/search/by-name/{name}")
    public Object searchByName(@PathVariable String name) {
        try {
            // Search decision definitions by name using the filter API
            var result = camundaClient
                    .newDecisionDefinitionSearchRequest()
                    .filter(f -> f.name(name))
                    .send()
                    .join();

            return result.items();
        } catch (Exception e) {
            return "Error searching decision definitions by name: " + e.getMessage();
        }
    }

    /**
     * Example: Search decision definition by ID (DMN ID)
     * GET /decision-definitions/search/by-id/myDecisionId
     * 
     * API Usage:
     * camundaClient.newDecisionDefinitionSearchRequest()
     *     .filter(f -> f.decisionDefinitionId("myDecisionId"))
     *     .send()
     *     .join()
     *     .items()
     */
    @GetMapping("/search/by-id/{id}")
    public Object searchById(@PathVariable String id) {
        try {
            // Search decision definitions by decision definition ID
            var result = camundaClient
                    .newDecisionDefinitionSearchRequest()
                    .filter(f -> f.decisionDefinitionId(id))
                    .send()
                    .join();

            return result.items();
        } catch (Exception e) {
            return "Error searching decision definitions by ID: " + e.getMessage();
        }
    }

    @GetMapping("/health")
    public String health() {
        return "DecisionDefinitionController is up";
    }

    /**
     * Evaluate a decision definition.
     * POST /decision-definitions/evaluation
     */
    @PostMapping("/evaluation")
    public ResponseEntity<?> evaluateDecision(@RequestBody NTdecisionDTO request) {
        try {
            LOGGER.info("Received decision evaluation request for id='{}' key='{}'", request.getDecisionDefinitionId(), request.getDecisionDefinitionKey());

            var decisionId = request.getDecisionDefinitionId();
            var decisionKey = request.getDecisionDefinitionKey();

            if ((decisionId == null || decisionId.isBlank()) && (decisionKey == null || decisionKey.isBlank())) {
                return ResponseEntity.badRequest()
                        .body("Either decisionDefinitionId or decisionDefinitionKey must be provided.");
            }

            Map<String, Object> variables = new HashMap<>();
            if (request.getDecisionVariables() != null) {
                variables.put("team", request.getDecisionVariables().getTeam());
                variables.put("state", request.getDecisionVariables().getState());
            }

            var command = camundaClient.newEvaluateDecisionCommand();
            var commandStep2 = (decisionId != null && !decisionId.isBlank())
                    ? command.decisionId(decisionId)
                    // If key is numeric, use decisionKey API; otherwise treat as decisionId.
                    : decisionKey.chars().allMatch(Character::isDigit)
                    ? command.decisionKey(Long.parseLong(decisionKey))
                    : command.decisionId(decisionKey);

            var result = commandStep2
                    .variables(variables)
                    .send()
                    .join();

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error evaluating decision definition: " + e.getMessage());
        }
    }
}

