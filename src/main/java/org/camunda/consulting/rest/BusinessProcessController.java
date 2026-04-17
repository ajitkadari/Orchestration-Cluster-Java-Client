package org.camunda.consulting.rest;

import io.camunda.client.api.response.ProcessInstanceResult;
import io.swagger.v3.oas.annotations.Operation;
import tools.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.camunda.consulting.service.BusinessProcessService;
import org.camunda.consulting.dto.OrderDTO;
import java.util.Map;
import org.camunda.consulting.dto.OrderProcessDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/camunda")
@Tag(name = "Camunda", description = "Camunda API proxy endpoints")
public class BusinessProcessController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessProcessController.class);

    private final BusinessProcessService businessProcessService;
    private final ObjectMapper objectMapper;

    public BusinessProcessController(BusinessProcessService businessProcessService, ObjectMapper objectMapper) {
        this.businessProcessService = businessProcessService;
        this.objectMapper = objectMapper;
    }

    @Operation(
            summary = "Create a process instance",
            description = "Creates a Camunda process instance using either processDefinitionId or processDefinitionKey"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Process instance created successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "object"),
                            examples = @ExampleObject(
                                    name = "ProcessInstanceCreated",
                                    value = """
                                            {
                                              "bpmnProcessId": "order-process",
                                              "processDefinitionKey": 2251799816087191,
                                              "processInstanceKey": 2251799816115600,
                                              "tags": [],
                                              "tenantId": "<default>",
                                              "variables": "{\\"discount\\":0.15,\\"order\\":{\\"customerType\\":\\"VIP\\",\\"total\\":1000,\\"items\\":[{\\"category\\":\\"ELECTRONICS\\",\\"quantity\\":1},{\\"category\\":\\"ELECTRONICS\\",\\"quantity\\":1}]}}",
                                              "variablesAsMap": {
                                                "discount": 0.15,
                                                "order": {
                                                  "customerType": "VIP",
                                                  "total": 1000,
                                                  "items": [
                                                    {
                                                      "category": "ELECTRONICS",
                                                      "quantity": 1
                                                    },
                                                    {
                                                      "category": "ELECTRONICS",
                                                      "quantity": 1
                                                    }
                                                  ]
                                                }
                                              },
                                              "version": 9
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Missing required fields in request body",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "Either processDefinitionId or processDefinitionKey must be provided.")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error creating process instance",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "Error creating process instance: <reason>")
                    )
            )
    })
    @PostMapping(path = "/process-instances/order-process", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createOrderProcessInstance(
            @RequestBody(
                    description = "Process creation request",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OrderProcessDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "CreateUsingProcessDefinitionId",
                                            value = """
                                                    {
                                                      "processDefinitionId": "order-process",
                                                      "variables": {
                                                        "order": {
                                                          "customerType": "VIP",
                                                          "total": 1000.00,
                                                          "items": [
                                                            {
                                                              "category": "ELECTRONICS",
                                                              "quantity": 1
                                                            },
                                                            {
                                                              "category": "ELECTRONICS",
                                                              "quantity": 1
                                                            }
                                                          ]
                                                        }
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "CreateUsingProcessDefinitionKey",
                                            value = """
                                                    {
                                                      "processDefinitionKey": "2251799813685249",
                                                      "variables": {
                                                        "order": {
                                                          "customerType": "REGULAR",
                                                          "total": 125.75,
                                                          "items": [
                                                            {
                                                              "category": "ELECTRONICS",
                                                              "quantity": 1
                                                            },
                                                            {
                                                              "category": "ELECTRONICS",
                                                              "quantity": 1
                                                            },
                                                            {
                                                              "category": "ELECTRONICS",
                                                              "quantity": 1
                                                            },
                                                            {
                                                              "category": "ELECTRONICS",
                                                              "quantity": 1
                                                            }
                                                          ]
                                                        }
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    ))
            @org.springframework.web.bind.annotation.RequestBody OrderProcessDTO orderProcessDTO) {
        try {
            LOGGER.info("Received process instance request for id='{}' key='{}'", orderProcessDTO.getProcessDefinitionId(), orderProcessDTO.getProcessDefinitionKey());
            ProcessInstanceResult instanceResult = businessProcessService.createOrderProcessInstance(orderProcessDTO);
            Map<String, Object> variablesAsMap = instanceResult.getVariablesAsMap();
            OrderDTO resultDTO = objectMapper.convertValue(variablesAsMap.get("order"), OrderDTO.class);
            resultDTO.setDiscount((Double) variablesAsMap.get("discount"));
            resultDTO.setCouponCode((String) variablesAsMap.get("couponCode"));
            return ResponseEntity.ok(resultDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating process instance: " + e.getMessage());
        }
    }
}
