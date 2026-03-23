package org.camunda.consulting;

import io.camunda.client.CamundaClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Decision Definitions", description = "APIs for searching and evaluating Camunda 8 Decision Definitions (DMN)")
public class DecisionDefinitionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionDefinitionController.class);

    @Autowired
    private CamundaClient camundaClient;

    @Operation(summary = "Search all decision definitions", description = "Returns a list of all deployed decision definitions from the Camunda cluster.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved decision definitions"),
            @ApiResponse(responseCode = "500", description = "Error communicating with the Camunda cluster", content = @Content(schema = @Schema(implementation = String.class)))
    })
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

    @Operation(summary = "Search decision definitions by name", description = "Returns decision definitions matching the given name.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved decision definitions"),
            @ApiResponse(responseCode = "500", description = "Error communicating with the Camunda cluster", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/search/by-name/{name}")
    public Object searchByName(
            @Parameter(description = "The name of the decision definition to search for", required = true, example = "myDecisionName")
            @PathVariable String name) {
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

    @Operation(summary = "Search decision definitions by DMN ID", description = "Returns decision definitions matching the given decision definition ID (DMN ID).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved decision definitions"),
            @ApiResponse(responseCode = "500", description = "Error communicating with the Camunda cluster", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/search/by-id/{id}")
    public Object searchById(
            @Parameter(description = "The DMN decision definition ID to search for", required = true, example = "myDecisionId")
            @PathVariable String id) {
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

    @Operation(summary = "Health check", description = "Returns a simple health status for this controller.")
    @ApiResponse(responseCode = "200", description = "Controller is healthy")
    @GetMapping("/health")
    public String health() {
        return "DecisionDefinitionController is up";
    }

    @Operation(
            summary = "Evaluate a decision definition",
            description = "Evaluates a DMN decision using either the decisionDefinitionId or decisionDefinitionKey, along with optional input variables.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Decision evaluated successfully"),
            @ApiResponse(responseCode = "400", description = "Missing required fields in request body", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Error evaluating decision", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/evaluation")
    public ResponseEntity<?> evaluateDecision(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Decision evaluation request containing the decision ID/key and input variables",
                    required = true)
            @RequestBody NTdecisionDTO request) {
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
