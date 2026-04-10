package org.camunda.consulting;

import org.junit.jupiter.api.Test;

/**
 * Application-level smoke tests.
 * REST endpoint tests have been moved to:
 *   org.camunda.consulting.rest.DecisionControllerTest
 * SOAP endpoint tests are in:
 *   org.camunda.consulting.soap.DecisionEvaluationSoapEndpointTest
 */
class OrchestrationApiClientApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that the test framework bootstraps correctly.
        // Full Spring context startup is covered by integration tests.
    }
}
