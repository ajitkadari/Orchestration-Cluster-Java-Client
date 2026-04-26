package org.camunda.consulting.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.camunda.client.CamundaClient;

/**
 * Spring configuration for Camunda Client.
 * Creates a managed CamundaClient bean using CamundaClientCloudBuilder,
 * reading credentials from application.yaml.
 */
@Configuration
public class CamundaClientConfiguration {

    /** Cluster ID – e.g. camunda.cluster.id in application.yaml */
    @Value("${camunda.cluster.id}")
    private String clusterId;

    /** Region – e.g. camunda.cluster.region in application.yaml */
    @Value("${camunda.cluster.region}")
    private String region;

    /** OAuth Client ID – e.g. camunda.client.id in application.yaml */
    @Value("${camunda.client.id}")
    private String clientId;

    /** OAuth Client Secret – e.g. camunda.client.secret in application.yaml */
    @Value("${camunda.client.secret}")
    private String clientSecret;

    /**
     * Build a CamundaClient using CamundaClientCloudBuilder.
     *
     * The builder uses a mandatory step pattern – methods MUST be called in order:
     *   Step 1: .withClusterId(String)    – Camunda SaaS cluster UUID
     *   Step 2: .withClientId(String)     – M2M application client ID
     *   Step 3: .withClientSecret(String) – M2M application client secret
     *   Step 4: .withRegion(String)       – cluster region (e.g. "cle-1")  [optional]
     *           .build()                  – produces the CamundaClient
     */
    @Bean
    public CamundaClient camundaClient() {
        return CamundaClient.newCloudClientBuilder()
                .withClusterId(clusterId)
                .withClientId(clientId)
                .withClientSecret(clientSecret)
                .withRegion(region)
                .build();
    }
}

