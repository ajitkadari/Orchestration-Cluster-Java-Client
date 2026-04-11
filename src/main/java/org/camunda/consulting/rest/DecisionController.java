package org.camunda.consulting.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.camunda.consulting.dto.DecisionDTO;
import org.camunda.consulting.service.DecisionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/camunda")
@Tag(name = "Camunda", description = "Camunda API proxy endpoints")
public class DecisionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionController.class);

    private final DecisionService decisionService;

    public DecisionController(DecisionService decisionEvaluationService) {
        this.decisionService = decisionEvaluationService;
    }

    @Operation(summary = "Get Camunda Cluster topology", description = "Returns Camunda Cluster topology payload from the configured Camunda base URL")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved topology"),
            @ApiResponse(responseCode = "500", description = "Error communicating with the Camunda cluster", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping(path = "/topology", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> topology() {
        try {
            return ResponseEntity.ok(decisionService.topology());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving topology: " + e.getMessage());
        }
    }

    @Operation(summary = "Get a decision definition", description = "Returns a Camunda decision definition by its key")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved decision definition"),
            @ApiResponse(responseCode = "500", description = "Error communicating with the Camunda cluster", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping(path = "/decision-definitions/{decisionDefinitionKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getDecisionDefinition(
            @Parameter(description = "Camunda decision definition key", required = true, example = "2251799813326547")
            @PathVariable long decisionDefinitionKey) {
        try {
            return ResponseEntity.ok(decisionService.getDecisionDefinition(decisionDefinitionKey));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting decision definition: " + e.getMessage());
        }
    }

    @Operation(summary = "Get decision definition XML", description = "Returns the XML representation of a Camunda decision definition by its key")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved decision definition XML"),
            @ApiResponse(responseCode = "500", description = "Error communicating with the Camunda cluster", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping(path = "/decision-definitions/{decisionDefinitionKey}/xml", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getDecisionDefinitionXml(
            @Parameter(description = "Camunda decision definition key", required = true, example = "2251799813326547")
            @PathVariable long decisionDefinitionKey) {
        try {
            return ResponseEntity.ok(decisionService.getDecisionDefinitionXml(decisionDefinitionKey));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting decision definition XML: " + e.getMessage());
        }
    }

    @Operation(summary = "Search decision definitions", description = "Searches Camunda decision definitions using the provided filter criteria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved decision definitions"),
            @ApiResponse(responseCode = "500", description = "Error communicating with the Camunda cluster", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping(path = "/decision-definitions/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchDecisionDefinitions(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = false,
                    description = "Search request containing page, sort, and filter criteria",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "object"),
                            examples = @ExampleObject(
                                    name = "DecisionDefinitionSearchRequest",
                                    value = """
                                            {
                                              "page": {
                                                "from": 0,
                                                "limit": 100
                                              },
                                              "sort": [
                                                {
                                                  "field": "decisionDefinitionKey",
                                                  "order": "ASC"
                                                }
                                              ],
                                              "filter": {
                                                "decisionDefinitionId": "new-hire-onboarding-workflow",
                                                "name": "string",
                                                "version": 0,
                                                "decisionRequirementsId": "string",
                                                "tenantId": "customer-service",
                                                "decisionDefinitionKey": "2251799813326547",
                                                "decisionRequirementsKey": "2251799813683346"
                                              }
                                            }
                                            """
                            )
                    )
            )
            @RequestBody(required = false) Map<String, Object> requestBody) {
        try {
            return ResponseEntity.ok(decisionService.searchDecisionDefinitions(requestBody));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error searching decision definitions: " + e.getMessage());
        }
    }

    @Operation(summary = "Evaluate a decision definition", description = "Evaluates a Camunda decision definition using the provided input variables")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Decision evaluated successfully"),
            @ApiResponse(responseCode = "400", description = "Missing required fields in request body", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Error evaluating decision", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping(path = "/decision-definitions/evaluation", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> evaluateDecisionDefinition(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Decision evaluation request containing the decision ID/key and input variables",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DecisionDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Evaluate using decisionDefinitionId",
                                            value = """
                                                    {
                                                      "decisionDefinitionId": "nt-decision",
                                                      "variables": {
                                                        "team": "East Regional",
                                                        "state": "Alabama"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Evaluate using decisionDefinitionKey",
                                            value = """
                                                    {
                                                      "decisionDefinitionKey": "2251799813685249",
                                                      "variables": {
                                                        "team": "East Regional",
                                                        "state": "Alabama"
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    ))
            @RequestBody DecisionDTO request) {
        try {
            LOGGER.info("Received decision evaluation request for id='{}' key='{}'", request.getDecisionDefinitionId(), request.getDecisionDefinitionKey());
            return ResponseEntity.ok(decisionService.evaluate(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error evaluating decision definition: " + e.getMessage());
        }
    }
}
