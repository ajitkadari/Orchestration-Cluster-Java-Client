# Orchestration Cluster Java Client

**Last Updated:** April 2026

A Spring Boot application that provides **REST and SOAP API endpoints** for interacting with **Camunda 8 SaaS** (Orchestration Cluster) using the official [Camunda Java Client](https://docs.camunda.io/docs/apis-tools/java-client/). It exposes endpoints for topology retrieval, decision definition lookup/search, DMN evaluation, and order process instance creation.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
  - [Environment Variables](#environment-variables)
  - [Network Access Requirements](#network-access-requirements)
  - [Setting Environment Variables](#setting-environment-variables)
    - [macOS / Linux](#macos--linux)
    - [Windows](#windows)
  - [Using direnv (macOS / Linux)](#using-direnv-macos--linux)
- [Running the Application](#running-the-application)
- [Swagger UI / OpenAPI Docs](#swagger-ui--openapi-docs)
- [REST API Endpoints](#rest-api-endpoints)
  - [Get Cluster Topology](#get-cluster-topology)
  - [Get a Decision Definition](#get-a-decision-definition)
  - [Get Decision Definition XML](#get-decision-definition-xml)
  - [Search Decision Definitions](#search-decision-definitions)
  - [Evaluate a Decision Definition](#evaluate-a-decision-definition)
  - [Create Order Process Instance](#create-order-process-instance)
- [SOAP Endpoint](#soap-endpoint)
  - [WSDL Access](#wsdl-access)
  - [SOAP Request Example](#soap-request-example)
  - [SOAP Response Example](#soap-response-example)
- [Request & Response Models](#request--response-models)
- [Running Tests](#running-tests)
- [Building a JAR](#building-a-jar)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [Publishing to an Internal Organization Repository](#publishing-to-an-internal-organization-repository)
- [License](#license)

---

## Overview

This application acts as a **Java-based HTTP and SOAP client/proxy** for the Camunda 8 Orchestration Cluster. It is useful for:

- **Retrieving cluster topology and decision definitions** by decision definition key.
- **Searching deployed DMN decision definitions** using filter/page criteria.
- **Evaluating decision definitions** (DMN) with input variables and returning the result.
- **Exposing SOAP/WSDL endpoints** for enterprise integrations requiring SOAP protocol.
- Serving as a **reference implementation** for integrating the Camunda Java Client (`camunda-client-java`) into a Spring Boot application.

---

## Tech Stack

| Technology | Version |
| --- | --- |
| Java | 25 |
| Spring Boot | 4.0.5 |
| Spring Framework | 7.0.6 (managed by Spring Boot) |
| Spring Web MVC | via `spring-boot-starter-web` |
| Spring Web Services (SOAP) | via `spring-boot-starter-web-services` |
| Bean Validation | via `spring-boot-starter-validation` (Hibernate Validator 9.0.1.Final, transitively resolved) |
| Camunda Java Client | 8.8.21 |
| Jackson (application code) | 3.x (`tools.jackson`, via `spring-boot-starter-web`) |
| Jackson (springdoc transitive) | 2.21.x (`com.fasterxml.jackson`, via `springdoc-openapi-starter-webmvc-ui`) |
| OpenAPI + Swagger UI | `springdoc-openapi-starter-webmvc-ui:3.0.2` |
| Testing | `spring-boot-starter-test` (versions managed transitively by Spring Boot) |
| Maven | 3.x (via wrapper `mvnw`) |
| WSDL4J | 1.6.3 |

> Note: `hibernate-validator` is not directly version-pinned in `pom.xml`; it is brought in transitively by `spring-boot-starter-validation` and version-managed by `spring-boot-starter-parent`.
>
> Note: This project intentionally has both Jackson namespaces on the classpath: application SOAP code imports `tools.jackson` (3.x), while springdoc 3.0.2 still uses `com.fasterxml.jackson` (2.x) transitively.

---

## Project Structure

```
src/
├── main/
│   ├── java/org/camunda/consulting/
│   │   ├── OrchestrationApiClientApplication.java         # Spring Boot entry point
│   │   ├── config/
│   │   │   ├── CamundaClientConfiguration.java            # CamundaClient bean configuration
│   │   │   └── OpenApiConfiguration.java                  # Swagger / OpenAPI configuration
│   │   ├── dto/
│   │   │   ├── DecisionDTO.java                           # Decision evaluation request DTO
│   │   │   ├── OrderProcessDTO.java                       # Process creation request DTO
│   │   │   ├── OrderDTO.java                              # Order payload DTO used under variables.order
│   │   │   └── ItemDTO.java                               # Item payload DTO used in OrderDTO.items
│   │   ├── enumeration/
│   │   │   ├── CustomerType.java                          # VIP/REGULAR (case-insensitive)
│   │   │   └── ItemCategory.java                          # ELECTRONICS/ALCOHOL/GROCERY/CLOTHING (case-insensitive)
│   │   ├── service/
│   │   │   ├── DecisionService.java                       # Decision/topology/search/evaluation business logic
│   │   │   └── BusinessProcessService.java                # Order process instance creation business logic
│   │   ├── rest/
│   │   │   ├── DecisionController.java                    # REST controller for decision/topology APIs
│   │   │   └── BusinessProcessController.java             # REST controller for order process instance API
│   │   └── soap/
│   │       ├── SoapWebServiceConfig.java                  # SOAP servlet + WSDL configuration
│   │       ├── DecisionEvaluationSoapEndpoint.java        # SOAP endpoint implementation
│   │       └── model/
│   │           ├── EvaluateDecisionRequest.java           # SOAP request model
│   │           ├── EvaluateDecisionResponse.java          # SOAP response model
│   │           ├── SoapDecisionVariables.java             # SOAP variables wrapper (list of entries)
│   │           └── SoapVariableEntry.java                 # SOAP key/value entry (value field is Object in Java model)
│   │
│   └── resources/
│       ├── application.yaml                               # App config (reads from env vars)
│       └── decision-evaluation.xsd                        # SOAP schema for WSDL generation
│
└── test/
    └── java/org/camunda/consulting/
        ├── OrchestrationApiClientApplicationTests.java     # Lightweight smoke test class
        ├── rest/
        │   ├── DecisionControllerTest.java                 # REST endpoint tests for DecisionController APIs
        │   └── BusinessProcessControllerTest.java          # REST endpoint tests for order process API
        └── soap/
            └── DecisionEvaluationSoapEndpointTest.java    # SOAP endpoint tests
```

---

## Prerequisites

- **Java 25** or later
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
| `CAMUNDA_CLUSTER_ID`    | The UUID of your Camunda SaaS cluster         | `your-cluster-id`                      |
| `CAMUNDA_CLUSTER_REGION`| The region where your cluster is deployed     | `your-cluster-region`                  |
| `CAMUNDA_CLIENT_ID`     | OAuth M2M application Client ID               | `your-client-id`                       |
| `CAMUNDA_CLIENT_SECRET` | OAuth M2M application Client Secret           | `your-client-secret`                   |

These map to the following entries in `application.yaml`:

```yaml
camunda:
  cluster:
    # Loaded from environment variables (for example via direnv/.envrc or shell export)
    id: ${CAMUNDA_CLUSTER_ID}
    region: ${CAMUNDA_CLUSTER_REGION}
  client:
    # Loaded from environment variables (for example via direnv/.envrc or shell export)
    id: ${CAMUNDA_CLIENT_ID}
    secret: ${CAMUNDA_CLIENT_SECRET}
```

---

### Network Access Requirements

To access Camunda 8 SaaS Orchestration APIs, your network must allow outbound access to the following endpoints:

- `ZEEBE_ADDRESS`
  - To allow outbound access to a single cluster
    - Format: `{CAMUNDA_CLUSTER_ID}.{CAMUNDA_CLUSTER_REGION}.zeebe.camunda.io:443`
  - To allow outbound access to all clusters
    - Format: `*.zeebe.camunda.io:443`
- `CAMUNDA_OAUTH_URL`
  - Value: `https://login.cloud.camunda.io/oauth/token`
  - This endpoint is used by the API client to obtain OAuth token before invoking Camunda 8 SaaS API endpoints.

---

### Setting Environment Variables

#### macOS / Linux

**Option 1 — Export in the current terminal session (temporary):**

```bash
export CAMUNDA_CLUSTER_ID=your-cluster-id
export CAMUNDA_CLUSTER_REGION=your-cluster-region
export CAMUNDA_CLIENT_ID=your-client-id
export CAMUNDA_CLIENT_SECRET=your-client-secret
```

These values are lost when the terminal session ends.

**Option 2 — Persist in your shell profile (permanent):**

Add the `export` lines above to your shell profile file (`~/.zshrc` for Zsh, `~/.bashrc` or `~/.bash_profile` for Bash), then reload it:

```bash
source ~/.zshrc   # or: source ~/.bashrc
```

**Option 3 — Pass inline when starting the app:**

```bash
CAMUNDA_CLUSTER_ID=your-cluster-id \
CAMUNDA_CLUSTER_REGION=your-cluster-region \
CAMUNDA_CLIENT_ID=your-client-id \
CAMUNDA_CLIENT_SECRET=your-client-secret \
./mvnw clean spring-boot:run
```

---

#### Windows

**Option 1 — Set in the current Command Prompt session (temporary):**

```cmd
set CAMUNDA_CLUSTER_ID=your-cluster-id
set CAMUNDA_CLUSTER_REGION=your-cluster-region
set CAMUNDA_CLIENT_ID=your-client-id
set CAMUNDA_CLIENT_SECRET=your-client-secret
```

**Option 2 — Set in the current PowerShell session (temporary):**

```powershell
$env:CAMUNDA_CLUSTER_ID     = "your-cluster-id"
$env:CAMUNDA_CLUSTER_REGION = "your-cluster-region"
$env:CAMUNDA_CLIENT_ID      = "your-client-id"
$env:CAMUNDA_CLIENT_SECRET  = "your-client-secret"
```

**Option 3 — Persist as User environment variables (permanent, GUI):**

1. Open **Settings → System → About → Advanced system settings → Environment Variables**.
2. Under **User variables**, click **New** and add each variable name and value.
3. Click **OK**, then restart any open terminals or your IDE for the changes to take effect.

**Option 4 — Persist via PowerShell (permanent, scripted):**

```powershell
[System.Environment]::SetEnvironmentVariable("CAMUNDA_CLUSTER_ID",     "your-cluster-id",              "User")
[System.Environment]::SetEnvironmentVariable("CAMUNDA_CLUSTER_REGION", "your-cluster-region",         "User")
[System.Environment]::SetEnvironmentVariable("CAMUNDA_CLIENT_ID",      "your-client-id",              "User")
[System.Environment]::SetEnvironmentVariable("CAMUNDA_CLIENT_SECRET",  "your-client-secret",          "User")
```

Then run the application from Command Prompt or PowerShell:

```cmd
mvnw.cmd clean spring-boot:run
```

> **Note (all platforms):** If launching from an IDE (e.g., IntelliJ), the IDE may not inherit shell-level environment variables. Either configure them in the IDE's **Run/Debug Configuration → Environment variables** field, or start the application from the terminal to ensure they are correctly loaded.

---

### Using direnv (macOS / Linux)

The recommended way to manage environment variables locally on macOS/Linux is with [direnv](https://direnv.net/). It automatically loads and unloads variables when you enter/leave the project directory.

1. **Install direnv:**
   ```bash
   brew install direnv
   ```

2. **Add the hook to your shell** (e.g., `~/.zshrc`):
   ```bash
   eval "$(direnv hook zsh)"
   ```

3. **Create a `.envrc` file** in the project root:
   ```bash
   export CAMUNDA_CLUSTER_ID=your-cluster-id
   export CAMUNDA_CLUSTER_REGION=your-cluster-region
   export CAMUNDA_CLIENT_ID=your-client-id
   export CAMUNDA_CLIENT_SECRET=your-client-secret
   ```

4. **Allow direnv to load it:**
   ```bash
   direnv allow .
   ```

> **Note:** direnv is not available natively on Windows. Windows users should use one of the permanent variable options described above.

---

## Running the Application

### Using Maven

```bash
cd /path/to/your/project
./mvnw clean spring-boot:run
```

The application starts on **http://localhost:8080** by default.

---

## Swagger UI / OpenAPI Docs

Once the application is running, interactive API documentation is available via **Swagger UI** (provided by [springdoc-openapi](https://springdoc.org/)):

| URL | Description |
|-----|-------------|
| `http://localhost:8080/swagger-ui/index.html` | Interactive Swagger UI (canonical URL) |
| `http://localhost:8080/swagger-ui.html` | Interactive Swagger UI |
| `http://localhost:8080/v3/api-docs` | OpenAPI spec (JSON) |
| `http://localhost:8080/v3/api-docs.yaml` | OpenAPI spec (YAML) |

The Swagger UI lets you explore and test the REST endpoints directly from the browser. SOAP operations are exposed separately through the generated WSDL at `http://localhost:8080/ws/decisionEvaluation.wsdl`.

---

## REST API Endpoints

All REST endpoints are available under the `/api/camunda` prefix.

---

### Get Cluster Topology

```
GET /api/camunda/topology
```

Returns Camunda cluster topology information.

---

### Get a Decision Definition

```
GET /api/camunda/decision-definitions/{decisionDefinitionKey}
```

| Path Parameter | Description |
|----------------|-------------|
| `decisionDefinitionKey` | Camunda decision definition key |

Returns a decision definition by key.

---

### Get Decision Definition XML

```
GET /api/camunda/decision-definitions/{decisionDefinitionKey}/xml
```

| Path Parameter | Description |
|----------------|-------------|
| `decisionDefinitionKey` | Camunda decision definition key |

Returns decision definition XML by key.

---

### Search Decision Definitions

```
POST /api/camunda/decision-definitions/search
Content-Type: application/json
```

Searches decision definitions using optional `page`, `sort`, and `filter` fields.

Current implementation applies these request fields:
- `page.from`, `page.limit`
- `filter.decisionDefinitionId`, `filter.name`, `filter.decisionDefinitionKey`

The example below matches the OpenAPI sample; fields outside the list above are currently accepted in the payload but not applied by service-side filtering logic.

**Example Request:**
```json
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
```

---

### Evaluate a Decision Definition

```
POST /api/camunda/decision-definitions/evaluation
Content-Type: application/json
```

Evaluates a DMN decision with the provided input variables.

**Request Body:**

```json
{
  "decisionDefinitionId": "1234-5678",
  "variables": {}
}
```

Alternative request body:

```json
{
  "decisionDefinitionKey": "12345",
  "variables": {}
}
```

| Field                  | Type   | Required | Description                                                               |
|------------------------|--------|----------|---------------------------------------------------------------------------|
| `decisionDefinitionId` | String | Either/Or| The DMN decision ID (takes priority over `decisionDefinitionKey`)         |
| `decisionDefinitionKey`| String | Either/Or| Numeric decision definition key (or DMN ID fallback if non-numeric) when `decisionDefinitionId` is blank |
| `variables`    | Object (`Map<String, Object>`) | Optional | Name/value pairs passed to the decision engine; keys must be strings      |

> **Note:** Either `decisionDefinitionId` or `decisionDefinitionKey` must be provided.

**Success Response:** `200 OK` with the decision evaluation result from Camunda.

**Error Responses:**
- `400 Bad Request`: Missing both ID and key, or invalid request format.
- `500 Internal Server Error`: Error during decision evaluation.

---

### Create Order Process Instance

```
POST /api/camunda/process-instances/order-process
Content-Type: application/json
```

Creates an order process instance using either `processDefinitionId` or `processDefinitionKey`.

**Example Request (ID-based):**

```json
{
  "processDefinitionId": "order-process",
  "version": 1,
  "tenantId": "customer-service",
  "variables": {
    "order": {
      "customerType": "VIP",
      "total": 250.50,
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
```

**Example Request (Key-based):**

```json
{
  "processDefinitionKey": "2251799813685249",
  "version": null,
  "tenantId": null,
  "variables": {
    "order": {
      "customerType": "REGULAR",
      "total": 125.75,
      "items": [
        {
          "category": "ELECTRONICS",
          "quantity": 3
        }
      ]
    }
  }
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `processDefinitionId` | String | Either/Or | BPMN process ID to start by ID |
| `processDefinitionKey` | String | Either/Or | Numeric process definition key to start by key |
| `version` | Integer | Optional | Specific version when `processDefinitionId` is used |
| `tenantId` | String | Optional | Tenant identifier for multi-tenant clusters |
| `variables` | Object (`Map<String, Object>`) | Optional | Process variables sent to Camunda |
| `variables.order.customerType` | String | Optional | Supported values for `OrderDTO` are `VIP` and `REGULAR` (case-insensitive) |
| `variables.order.items[*].category` | String | Optional | Supported values are `ELECTRONICS`, `ALCOHOL`, `GROCERY`, `CLOTHING` (case-insensitive) |

> **Note:** Either `processDefinitionId` or `processDefinitionKey` must be provided.

**Error Responses:**
- `400 Bad Request`: Missing both ID and key.
- `500 Internal Server Error`: Error during process instance creation.

**Success Response Example (200 OK):**

```json
{
  "processDefinitionId": "my-process-model-1",
  "processDefinitionVersion": 3,
  "tenantId": "<default>",
  "variables": {},
  "processDefinitionKey": "2251799813686749",
  "processInstanceKey": "2251799813690746",
  "tags": [
    "high-touch",
    "remediation"
  ]
}
```

| Response Field | Type | Description |
|---|---|---|
| `processDefinitionId` | String | The process definition ID that was started |
| `processDefinitionVersion` | Integer | The version of the process definition |
| `tenantId` | String | The tenant context |
| `variables` | Object | Process variables returned from Camunda |
| `processDefinitionKey` | String | Numeric key of the process definition |
| `processInstanceKey` | String | Unique key of the created process instance |
| `tags` | Array | Tags associated with the process instance |

---

## SOAP Endpoint

SOAP is exposed under `/ws/*`.

### WSDL Access

| URL | Description |
|-----|-------------|
| `http://localhost:8080/ws/decisionEvaluation.wsdl` | Generated WSDL for decision evaluation |

The WSDL can be imported into SOAP clients like **SoapUI**, **Postman**, or **Insomnia** for testing.

---

### SOAP Operation

- **Request element:** `evaluateDecisionRequest`
- **Response element:** `evaluateDecisionResponse`
- **Namespace:** `http://camunda.org/consulting/decision-evaluation`
- **Port:** `/ws`

---

### SOAP Request Example

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:dec="http://camunda.org/consulting/decision-evaluation">
  <soapenv:Header/>
  <soapenv:Body>
    <dec:evaluateDecisionRequest>
      <dec:decisionDefinitionId>myDecisionId</dec:decisionDefinitionId>
      <dec:variables>
        <dec:entry>
          <dec:key>team</dec:key>
          <dec:value>East Regional</dec:value>
        </dec:entry>
        <dec:entry>
          <dec:key>state</dec:key>
          <dec:value>Alabama</dec:value>
        </dec:entry>
      </dec:variables>
    </dec:evaluateDecisionRequest>
  </soapenv:Body>
</soapenv:Envelope>
```

---

### SOAP Response Example

**Success Response:**
```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
  <SOAP-ENV:Body>
    <ns2:evaluateDecisionResponse xmlns:ns2="http://camunda.org/consulting/decision-evaluation">
      <ns2:success>true</ns2:success>
      <ns2:result>{"evaluationResult":"approved","executionId":"exec-123"}</ns2:result>
    </ns2:evaluateDecisionResponse>
  </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

**Error Response:**
```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
  <SOAP-ENV:Body>
    <ns2:evaluateDecisionResponse xmlns:ns2="http://camunda.org/consulting/decision-evaluation">
      <ns2:success>false</ns2:success>
      <ns2:errorMessage>Either decisionDefinitionId or decisionDefinitionKey must be provided.</ns2:errorMessage>
    </ns2:evaluateDecisionResponse>
  </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

---

## Request & Response Models

### `DecisionDTO`

Request model for decision evaluation.

`variables` is represented in Java as `Map<String, Object>`, so each variable name (map key) must be a string.
Use a standard JSON object of key-value pairs (for example, `"key": "value"`).

```json
{
  "decisionDefinitionId": "string",
  "decisionDefinitionKey": "string",
  "variables": {
    "firstInputKey": "firstInputValue",
    "secondInputKey": "secondInputValue"
  }
}
```

### `OrderProcessDTO`

Request model for `POST /api/camunda/process-instances/order-process`.

```json
{
  "processDefinitionId": "order-process",
  "variables": {
    "order": {
      "customerType": "VIP",
      "total": 250.50,
      "items": [
        {
          "category": "ELECTRONICS",
          "quantity": 2
        }
      ]
    }
  }
}
```

```json
{
  "processDefinitionKey": "2251799813686749",
  "variables": {
    "order": {
      "customerType": "REGULAR",
      "total": 150.78,
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
```

`variables` is still a generic `Map<String, Object>` in Java. The documented `variables.order` structure above follows the API examples and is recommended for order-process payloads.

### SOAP Models

`SoapVariableEntry` in Java uses `value: Object`, and the current SOAP XSD (`decision-evaluation.xsd`) defines `<value>` as `xsd:anyType` to allow typed SOAP values.

**EvaluateDecisionRequest**
```xml
<dec:evaluateDecisionRequest>
  <dec:decisionDefinitionId>string</dec:decisionDefinitionId>
  <dec:decisionDefinitionKey>string</dec:decisionDefinitionKey>
  <dec:variables>
    <dec:entry>
      <dec:key>string</dec:key>
      <dec:value><!-- xsd:anyType: string | number | boolean | etc. --></dec:value>
    </dec:entry>
    <!-- repeat <entry> for each variable -->
  </dec:variables>
</dec:evaluateDecisionRequest>
```

**EvaluateDecisionResponse**
```xml
<dec:evaluateDecisionResponse>
  <dec:success>boolean</dec:success>
  <dec:result>string (JSON)</dec:result>
  <dec:errorMessage>string</dec:errorMessage>
</dec:evaluateDecisionResponse>
```

---

## Running Tests

### Current Test Suite

```bash
cd /path/to/your/project
./mvnw test
```

The current automated test suite is primarily unit-focused:

- `DecisionControllerTest` uses Mockito + MockMvc to exercise `DecisionController` REST mappings and response handling.
- `BusinessProcessControllerTest` uses Mockito + MockMvc to validate order process instance creation endpoint behavior.
- `DecisionEvaluationSoapEndpointTest` verifies the SOAP endpoint's success and error payload behavior.
- `OrchestrationApiClientApplicationTests` is a lightweight smoke test class; it does **not** bootstrap the full Spring `ApplicationContext`.

### Test Coverage

Tests include:
- **REST endpoint tests** for topology, get by key, get XML, and search (`/api/camunda/...`)
- **REST endpoint tests** for decision evaluation (`/api/camunda/decision-definitions/evaluation`)
- **REST endpoint tests** for order process instance creation (`/api/camunda/process-instances/order-process`)
- **Error handling tests** for missing identifiers and runtime exceptions
- **SOAP endpoint tests** for success and error scenarios

### Running Specific Tests

```bash
./mvnw test -Dtest=DecisionControllerTest
./mvnw test -Dtest=BusinessProcessControllerTest
./mvnw test -Dtest=DecisionEvaluationSoapEndpointTest
./mvnw test -Dtest=OrchestrationApiClientApplicationTests
```

---

## Building a JAR

### Full Build (with Tests)

```bash
cd /path/to/your/project
./mvnw clean package
```

### Fast Build (Skip Tests)

```bash
./mvnw clean package -DskipTests
```

This creates a JAR at:
```
target/orchestration-cluster-java-client-0.0.1-SNAPSHOT.jar
```

### Run the JAR

```bash
java -jar target/orchestration-cluster-java-client-0.0.1-SNAPSHOT.jar
```

---

## Maven Build Phases

| Command | Description |
|---------|-------------|
| `./mvnw clean` | Removes `target/` directory |
| `./mvnw compile` | Compiles main source code |
| `./mvnw test` | Runs unit and integration tests |
| `./mvnw package` | Builds JAR artifact |
| `./mvnw clean spring-boot:run` | Cleans and runs the app in dev mode |
| `./mvnw dependency:tree` | Shows dependency tree |
| `./mvnw -DskipTests clean package` | Skips tests during build |

---

## Troubleshooting

### Application fails to start: Missing environment variables

**Error:**
```
Could not resolve placeholder 'CAMUNDA_CLUSTER_ID' in value "${CAMUNDA_CLUSTER_ID}"
```

**Solution:**
Ensure all required environment variables are set before starting:
```bash
export CAMUNDA_CLUSTER_ID=your-cluster-id
export CAMUNDA_CLUSTER_REGION=your-cluster-region
export CAMUNDA_CLIENT_ID=your-client-id
export CAMUNDA_CLIENT_SECRET=your-client-secret
./mvnw spring-boot:run
```

Or use direnv (see [Using direnv](#using-direnv-macos--linux) section).

### Swagger UI shows "Failed to fetch OpenAPI spec"

**Solution:**
- Ensure the app is running on port 8080.
- Check browser console for CORS errors.
- Verify `/v3/api-docs` is accessible: `curl http://localhost:8080/v3/api-docs`

### WSDL not accessible

**Error:**
```
HTTP 404 - Not Found for WSDL
```

**Solution:**
- Verify the app is running.
- Ensure SOAP servlet is mapped to `/ws/*`.
- Check logs for SOAP configuration errors.
- Try: `curl http://localhost:8080/ws/decisionEvaluation.wsdl`

---

## Contributing

Contributions are welcome! Please:

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/my-feature`).
3. Commit your changes (`git commit -m 'Add my feature'`).
4. Push to the branch (`git push origin feature/my-feature`).
5. Open a Pull Request.

---

## Publishing to an Internal Organization Repository

Use these steps if this public repository needs to be copied into your organization's internal Git hosting platform (for example GitHub Enterprise, GitLab, or Bitbucket).

### Prerequisites

- You have permission to create repositories in the internal organization.
- You can authenticate to the internal Git host (SSH key or HTTPS + token).
- You know the internal repository URL.

### Option 1: Push this local repository to a new internal repository

1. Create an empty repository in your internal organization (do not initialize with README/license/gitignore).
2. In this local project, add the internal remote:

```bash
git remote add internal git@<internal-git-host>:<org>/<repo>.git
```

3. Push your main branch to the internal remote:

```bash
git push -u internal main
```

4. Push tags (if any):

```bash
git push internal --tags
```

### Option 2: Mirror all refs to an existing internal repository

Use this when you need a full mirror (all branches, tags, and refs).

```bash
git clone --mirror https://github.com/<public-org>/<public-repo>.git
cd <public-repo>.git
git remote set-url --push origin git@<internal-git-host>:<org>/<repo>.git
git push --mirror
```

### Verify

```bash
git remote -v
git ls-remote --heads internal
git ls-remote --tags internal
```

If your default branch is not `main`, replace it with your branch name in the commands above.

---

## License

This project currently does not include a `LICENSE` file in the repository. Add one (for example Apache 2.0) before publishing or sharing externally.

---

## Additional Resources

- [Camunda 8 Documentation](https://docs.camunda.io/)
- [Camunda Java Client Documentation](https://docs.camunda.io/docs/apis-tools/java-client/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Web Services Documentation](https://spring.io/projects/spring-ws)
- [OpenAPI 3.0 Specification](https://spec.openapis.org/oas/v3.0.3)

