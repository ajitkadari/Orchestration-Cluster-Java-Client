package org.camunda.consulting;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Orchestration Cluster Java Client API")
                        .version("0.0.1-SNAPSHOT")
                        .description("REST API for interacting with Camunda 8 Orchestration Cluster APIs via the Camunda Java Client")
                        .contact(new Contact()
                                .name("Camunda Consulting")
                                .url("https://camunda.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}

