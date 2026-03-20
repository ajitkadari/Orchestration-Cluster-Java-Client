# Orchestration-Cluster-Java-Client

A Spring Boot REST API client for interacting with a **Camunda 8 SaaS** (Orchestration Cluster) using the official [Camunda Java Client](https://docs.camunda.io/docs/apis-tools/java-client/). It exposes REST endpoints for searching and evaluating DMN decision definitions.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
  - [Environment Variables](#environment-variables)
  - [Using direnv](#using-direnv)
- [Running the Application](#running-the-application)
- [REST API Endpoints](#rest-api-endpoints)
  - [Health Check](#health-check)
  - [Search Decision Definitions](#search-decision-definitions)
  - [Evaluate a Decision Definition](#evaluate-a-decision-definition)
- [Request & Response Models](#request--response-models)
- [Running Tests](#running-tests)
- [Building a JAR](#building-a-jar)

---

## Overview

This application acts as a Java-based HTTP client/proxy for the Camunda 8 Orchestration Cluster REST API. It is useful for:

- Searching deployed DMN decision definitions by name or ID.
- Evaluating decision definitions (DMN) with input variables and returning the result.
- Serving as a reference for integrating the Camunda Java Client (`camunda-client-java`) into a Spring Boot application.

---

## Tech Stack

| Technology | Version |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.3 |
| Camunda Java Client | 8.8.16 |
| Maven | (via wrapper `mvnw`) |

---

## Project Structure

```
src/
└── main/
    ├── java/org/camunda/consulting/
    │   ├── OrchestrationClusterClientApplication.java  # Spring Boot entry point
    │   ├── CamundaClientConfiguration.java             # CamundaClient bean configuration
    │   ├── DecisionDefinitionController.java           # REST controller (search + evaluate)
    │   ├── NTdecisionDTO.java                          # Request DTO for evaluation endpoint
    │   └── DecisionVariables.java                      # Nested variables DTO (team, state)
    └── resources/
        └── application.yaml                            # App config (reads from env vars)
```

---

## Prerequisites

- **Java 21** or later
- **Maven 3.x** (or use the included `./mvnw` wrapper)
- A **Camunda 8 SaaS** account with:
  - A running cluster
  - An M2M API client (Client ID + Client Secret)

---

## Configuration

### Environment Variables

The application reads the following **environment variables** at startup. These must be set before running the application.

| Environment Variable    | Description                                   | Example Value                          |
|-------------------------|-----------------------------------------------|----------------------------------------|
| `CAMUNDA_CLUSTER_ID`    | The UUID of your Camunda SaaS cluster         | `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |
| `CAMUNDA_CLUSTER_REGION`| The region where your cluster is deployed     | `cle-1`                                |
| `CAMUNDA_CLIENT_ID`     | OAuth M2M application Client ID               | `your-client-id`                       |
| `CAMUNDA_CLIENT_SECRET` | OAuth M2M application Client Secret           | `your-client-secret`                   |

These map to the following entries in `application.yaml`:

```yaml
camunda:
  cluster:
    id: ${CAMUNDA_CLUSTER_ID}
    region: ${CAMUNDA_CLUSTER_REGION}
  client:
    id: ${CAMUNDA_CLIENT_ID}
    secret: ${CAMUNDA_CLIENT_SECRET}
```

### Using direnv

The recommended way to manage environment variables locally is with [direnv](https://direnv.net/).

1. Install direnv:
   ```bash
   brew install direnv
   ```
2. Add the hook to your shell (e.g., `~/.zshrc`):
   ```bash
   eval "$(direnv hook zsh)"
   ```
3. Create a `.envrc` file in the project root:
   ```bash
   export CAMUNDA_CLUSTER_ID=your-cluster-id
   export CAMUNDA_CLUSTER_REGION=your-region
   export CAMUNDA_CLIENT_ID=your-client-id
   export CAMUNDA_CLIENT_SECRET=your-client-secret
   ```
4. Allow direnv to load it:
   ```bash
   direnv allow .
   ```

> **Note:** If launching from an IDE (e.g., IntelliJ), the IDE may not inherit the direnv environment. Run the application from the terminal using `./mvnw spring-boot:run` to ensure environment variables are correctly loaded.

---

## Running the Application

```bash
./mvnw clean spring-boot:run
```

The application starts on **http://localhost:8080** by default.

---

## REST API Endpoints

All endpoints are available under both `/decision-definitions` and `/v2/decision-definitions` prefixes.

### Health Check

```
GET /decision-definitions/health
```

**Response:**
```
DecisionDefinitionController is up
```

---

### Search Decision Definitions

#### Search All

```
GET /v2/decision-definitions/search
```

Returns a list of all deployed decision definitions in the cluster.

---

#### Search by Name

```
GET /v2/decision-definitions/search/by-name/{name}
```

| Path Parameter | Description                        |
|----------------|------------------------------------|
| `name`         | The name of the decision definition |

---

#### Search by Decision Definition ID

```
GET /v2/decision-definitions/search/by-id/{id}
```

| Path Parameter | Description                                  |
|----------------|----------------------------------------------|
| `id`           | The DMN decision definition ID (not the key) |

---

### Evaluate a Decision Definition

```
POST /v2/decision-definitions/evaluation
Content-Type: application/json
```

Evaluates a DMN decision with the provided input variables.

**Request Body:**

```json
{
  "decisionDefinitionId": "myDecisionId",
  "decisionDefinitionKey": "",
  "decisionVariables": {
    "team": "engineering",
    "state": "active"
  }
}
```

| Field                  | Type   | Required | Description                                                                 |
|------------------------|--------|----------|-----------------------------------------------------------------------------|
| `decisionDefinitionId` | String | Either/Or| The DMN decision ID (takes priority over `decisionDefinitionKey`)           |
| `decisionDefinitionKey`| String | Either/Or| The numeric cluster key or DMN ID (used if `decisionDefinitionId` is blank) |
| `decisionVariables`    | Object | Optional | Input variables to pass to the decision engine                              |
| `decisionVariables.team` | String | Optional | Team input variable                                                       |
| `decisionVariables.state`| String | Optional| State input variable                                                       |

> **Note:** Either `decisionDefinitionId` or `decisionDefinitionKey` must be provided.

**Success Response:** `200 OK` with the decision evaluation result from Camunda.

**Error Response:** `400 Bad Request` if neither identifier is provided, or `500 Internal Server Error` on evaluation failure.

---

## Request & Response Models

### `NTdecisionDTO`

```json
{
  "decisionDefinitionId": "string",
  "decisionDefinitionKey": "string",
  "decisionVariables": {
    "team": "string",
    "state": "string"
  }
}
```

---

## Running Tests

```bash
./mvnw test
```

> Tests use Mockito with the inline mock maker configured as a Java agent in the Maven Surefire plugin (`pom.xml`) to ensure compatibility with JDK 21+.

---

## Building a JAR

```bash
./mvnw clean package
```

The executable JAR is produced at:

```
target/orchestration-cluster-java-client-0.0.1-SNAPSHOT.jar
```

Run it directly:

```bash
java -jar target/orchestration-cluster-java-client-0.0.1-SNAPSHOT.jar
```

> Ensure the required environment variables are exported in your shell before running the JAR.
