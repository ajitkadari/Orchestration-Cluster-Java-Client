package org.camunda.consulting.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.camunda.consulting.DecisionEvaluationService;
import org.camunda.consulting.NTdecisionDTO;
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

@RestController
@RequestMapping({"/decision-definitions"})
@Tag(name = "Decision Definitions", description = "APIs for searching and evaluating Camunda 8 Decision Definitions (DMN)")
public class DecisionDefinitionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionDefinitionController.class);

    @Autowired
    private DecisionEvaluationService decisionEvaluationService;

    @Operation(summary = "Search all decision definitions", description = "Returns a list of all deployed decision definitions from the Camunda cluster.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved decision definitions"),
            @ApiResponse(responseCode = "500", description = "Error communicating with the Camunda cluster", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/search")
    public Object searchAllDecisionDefinitions() {
        try {
            return decisionEvaluationService.searchAll();
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
            return decisionEvaluationService.searchByName(name);
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
            return decisionEvaluationService.searchById(id);
        } catch (Exception e) {
            return "Error searching decision definitions by ID: " + e.getMessage();
        }
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
            return ResponseEntity.ok(decisionEvaluationService.evaluate(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error evaluating decision definition: " + e.getMessage());
        }
    }
}
