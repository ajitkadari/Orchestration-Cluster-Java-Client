package org.camunda.consulting.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/**
 * OpenAPI 3.0 configuration for Swagger UI documentation.
 * Configures metadata, servers, contact info, and external documentation links
 * for REST API endpoints.
 */
@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Orchestration Cluster Java Client API")
                        .version("0.0.1-SNAPSHOT")
                        .description("REST and SOAP API client for interacting with Camunda 8 Orchestration Cluster. " +
                                "This application provides endpoints to search and evaluate DMN decision definitions, " +
                                "and create order process instances " +
                                "using the official Camunda Java Client library.")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Camunda 8 Documentation")
                        .url("https://docs.camunda.io/"));
    }
}

